const DEFAULT_POLL_INTERVAL_MS = 1200;
const DEFAULT_OPERATION_TIMEOUT_MS = 90000;

const newRequestId = () => {
	if (window.crypto?.randomUUID) return window.crypto.randomUUID();
	return `vtl-${Date.now()}-${Math.random().toString(16).slice(2)}`;
};

const sleep = (milliseconds) => new Promise((resolve) => setTimeout(resolve, milliseconds));

const trimTrailingSlash = (value) => value?.replace(/\/+$/, '');

const appendQuery = (path, params) => {
	const search = new URLSearchParams();
	Object.entries(params || {}).forEach(([key, value]) => {
		if (value !== undefined && value !== null && value !== '') search.set(key, String(value));
	});
	const query = search.toString();
	return query ? `${path}?${query}` : path;
};

export class EchoesPlatformError extends Error {
	constructor(message, status, problem, correlationId) {
		super(message);
		this.name = 'EchoesPlatformError';
		this.status = status;
		this.problem = problem;
		this.correlationId = correlationId || problem?.correlationId;
	}
}

export default class EchoesPlatformClient {
	constructor({ baseUrl, getAccessToken }) {
		this.baseUrl = trimTrailingSlash(baseUrl);
		this.getAccessToken = getAccessToken;
	}

	ensureConfigured() {
		if (!this.baseUrl) {
			throw new Error('Common Platform API URL is not configured');
		}
	}

	async request(path, { method = 'GET', body, idempotencyKey, accept = 'application/json' } = {}) {
		this.ensureConfigured();
		const accessToken = await this.getAccessToken();
		const headers = {
			Accept: accept,
			Authorization: `Bearer ${accessToken}`,
			'X-Correlation-ID': newRequestId(),
		};

		if (body !== undefined) headers['Content-Type'] = 'application/json';
		if (idempotencyKey) headers['Idempotency-Key'] = idempotencyKey;

		const response = await fetch(`${this.baseUrl}${path}`, {
			method,
			headers,
			body: body === undefined ? undefined : JSON.stringify(body),
		});

		const contentType = response.headers.get('content-type') || '';
		const payload = contentType.includes('json') ? await response.json() : await response.text();
		const correlationId = response.headers.get('x-correlation-id');

		if (!response.ok) {
			const detail = payload?.detail || payload?.title || `${response.status} ${response.statusText}`;
			throw new EchoesPlatformError(detail, response.status, payload, correlationId);
		}

		return payload;
	}

	listMyHdts({ search, status = 'REGISTERED', limit = 100, offset = 0 } = {}) {
		return this.request(appendQuery('/platform/v1/hdts', {
			scope: 'MINE',
			status,
			search,
			limit,
			offset,
		}));
	}

	async findMyHdtByHeritageEntityUri(heritageEntityUri) {
		if (!heritageEntityUri) return null;
		const collection = await this.listMyHdts({ search: heritageEntityUri, limit: 100 });
		return (collection.items || []).find((hdt) => hdt.heritageEntityUri === heritageEntityUri) || null;
	}

	getHdt(hdtId) {
		return this.request(`/platform/v1/hdts/${encodeURIComponent(hdtId)}`);
	}

	createHdt({ heritageEntityUri, projectUri, name, description }, { idempotencyKey } = {}) {
		return this.request('/platform/v1/hdts', {
			method: 'POST',
			idempotencyKey: idempotencyKey || newRequestId(),
			body: {
				heritageEntityUri,
				projectUri,
				name: name || undefined,
				description: description || undefined,
			},
		});
	}

	getOperation(operationId) {
		return this.request(`/platform/v1/operations/${encodeURIComponent(operationId)}`);
	}

	listOperations({ type, status, limit = 10, offset = 0 } = {}) {
		return this.request(appendQuery('/platform/v1/operations', {
			type,
			status,
			limit,
			offset,
		}));
	}

	async waitForOperation(operationId, {
		pollIntervalMs = DEFAULT_POLL_INTERVAL_MS,
		timeoutMs = DEFAULT_OPERATION_TIMEOUT_MS,
		onUpdate,
	} = {}) {
		const deadline = Date.now() + timeoutMs;

		while (Date.now() < deadline) {
			const operation = await this.getOperation(operationId);
			if (onUpdate) onUpdate(operation);
			if (operation.status === 'SUCCEEDED') return operation;
			if (operation.status === 'FAILED') {
				throw new EchoesPlatformError(
					operation.failure?.detail || operation.failure?.message || 'ECHOES operation failed',
					422,
					operation.failure,
					operation.failure?.correlationId,
				);
			}
			if (operation.status === 'RECONCILIATION_REQUIRED') {
				throw new EchoesPlatformError(
					'ECHOES could not determine the final provider outcome. Reconciliation is required.',
					409,
					operation,
					operation.failure?.correlationId,
				);
			}
			await sleep(pollIntervalMs);
		}

		throw new Error('Timed out while waiting for the ECHOES operation');
	}

	registerExternalAsset({ uri, fileName, mediaType }) {
		return this.request('/platform/v1/spaces/me/assets/references', {
			method: 'POST',
			body: { uri, fileName, mediaType },
		});
	}

	listMyAssets() {
		return this.request('/platform/v1/spaces/me/assets');
	}

	getAsset(assetId) {
		return this.request(`/platform/v1/assets/${encodeURIComponent(assetId)}`);
	}

	async getManagedAssetContent(assetId) {
		this.ensureConfigured();
		const accessToken = await this.getAccessToken();
		const response = await fetch(`${this.baseUrl}/platform/v1/assets/${encodeURIComponent(assetId)}/content`, {
			headers: {
				Authorization: `Bearer ${accessToken}`,
				'X-Correlation-ID': newRequestId(),
			},
		});
		if (!response.ok) {
			const contentType = response.headers.get('content-type') || '';
			const payload = contentType.includes('json') ? await response.json() : await response.text();
			throw new EchoesPlatformError(
				payload?.detail || payload?.title || `${response.status} ${response.statusText}`,
				response.status,
				payload,
				response.headers.get('x-correlation-id'),
			);
		}
		return response.blob();
	}

	initiateManagedAssetUpload({ fileName, contentType, contentLength, sha256 }) {
		return this.request('/platform/v1/spaces/me/assets/uploads', {
			method: 'POST',
			body: { fileName, contentType, contentLength, sha256 },
		});
	}

	async uploadBinary({ uploadUri, requiredHeaders, content }) {
		const response = await fetch(uploadUri, {
			method: 'PUT',
			headers: requiredHeaders || {},
			body: content,
		});
		if (!response.ok) {
			const detail = await response.text();
			throw new EchoesPlatformError(
				detail || `Upload failed with ${response.status} ${response.statusText}`,
				response.status,
				null,
				null,
			);
		}
	}

	completeManagedAssetUpload(uploadId) {
		return this.request(`/platform/v1/assets/uploads/${encodeURIComponent(uploadId)}/complete`, {
			method: 'POST',
		});
	}

	executeSemanticQuery(query) {
		return this.request('/platform/v1/semantic-queries', {
			method: 'POST',
			accept: 'application/sparql-results+json',
			body: { query },
		});
	}

	enrichWithDigitalObject({ hdtId, assetId, label, enrichmentType = 'DIGITAL_OBJECT' }, { idempotencyKey } = {}) {
		return this.request(`/platform/v1/hdts/${encodeURIComponent(hdtId)}/enrichments`, {
			method: 'POST',
			idempotencyKey: idempotencyKey || newRequestId(),
			body: {
				semanticProfile: 'https://culturalheritagecloud.eu/profiles/hdto-enrichment/0.2',
				semanticPayload: {
					assetId,
					subjectAssetId: assetId,
					mapping: {
						profile: 'https://culturalheritagecloud.eu/mappings/hdto-digital-object/0.1',
					},
				},
				enrichmentType,
				metadata: label ? { label } : undefined,
			},
		});
	}
}

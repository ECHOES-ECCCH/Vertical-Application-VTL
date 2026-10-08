import api from '@/services/api';

const HDTO_HP29 = 'http://isl.ics.forth.gr/ontology/echoes/HP29_has_digital_object_part';

const isAbsoluteHttpUri = (value) => /^https?:\/\//i.test(value || '');

const stableHash = (value) => {
	let left = 2166136261;
	let right = 2246822519;
	for (let index = 0; index < value.length; index += 1) {
		const code = value.charCodeAt(index);
		left = Math.imul(left ^ code, 16777619);
		right = Math.imul(right ^ code, 3266489917);
	}
	return `${(left >>> 0).toString(16).padStart(8, '0')}${(right >>> 0).toString(16).padStart(8, '0')}`;
};

const idempotencyKey = (scope, payload) => `vtl:${scope}:${stableHash(JSON.stringify(payload))}`;

const sha256Hex = async (content) => {
	if (!window.crypto?.subtle) throw new Error('Web Crypto API is required to create an ECHOES snapshot');
	const bytes = content instanceof ArrayBuffer ? content : await content.arrayBuffer();
	const digest = await window.crypto.subtle.digest('SHA-256', bytes);
	return Array.from(new Uint8Array(digest))
		.map((byte) => byte.toString(16).padStart(2, '0'))
		.join('');
};

const assetIdFromCanonicalUri = (uri) => {
	try {
		const path = new URL(uri).pathname.split('/').filter(Boolean);
		return path[path.length - 1] || null;
	}
	catch (error) {
		return null;
	}
};

const sparqlIri = (uri) => {
	if (!isAbsoluteHttpUri(uri) || uri.includes('>')) throw new Error(`Unsafe semantic IRI: ${uri}`);
	return `<${uri}>`;
};

export default class VtlEchoesIntegrationService {
	constructor({ platformClient, reactAppApi, projectUri }) {
		this.platformClient = platformClient;
		this.reactAppApi = reactAppApi?.replace(/\/+$/, '');
		this.projectUri = projectUri;
	}

	async loadContext(transcriptionId) {
		const transcriptionResponse = await api.get(`/transcriptions/${transcriptionId}`, { params: { perpage: 100 } });
		const transcription = transcriptionResponse.data;
		const createDefaults = this.createHdtDefaults(transcription, transcriptionId);
		const [hdtCollection, matchingHdt, operationCollection] = await Promise.all([
			this.platformClient.listMyHdts(),
			this.platformClient.findMyHdtByHeritageEntityUri(createDefaults.heritageEntityUri),
			this.platformClient.listOperations({ limit: 8 }),
		]);

		return {
			transcription,
			hdts: hdtCollection.items || [],
			matchingHdt,
			operations: operationCollection.items || [],
			resources: this.resourceCandidates(transcription),
			createDefaults,
		};
	}

	createHdtDefaults(transcription, transcriptionId) {
		const configuredExternalUri = transcription?.externalUrl || transcription?.metadata?.externalUrl;
		const fallbackUri = new URL(`/transcription/${transcriptionId}/`, window.location.origin).toString();
		return {
			heritageEntityUri: isAbsoluteHttpUri(configuredExternalUri) ? configuredExternalUri : fallbackUri,
			projectUri: this.projectUri || '',
			name: transcription?.analyticInfo?.title || `VTL transcription ${transcriptionId}`,
			description: `HDT created from VTL transcription ${transcriptionId}`,
		};
	}

	resourceCandidates(transcription) {
		return (transcription?.pages?.list || []).flatMap((page, index) => {
			const pageNumber = page.pageNo || page.pageNumber || index + 1;
			const label = page.path || `Page ${pageNumber}`;
			const fileBaseName = label.replace(/\.[^/.]+$/, '');
			const resources = [];

			if (page.thumbnailUrl) {
				resources.push({
					id: `iiif-${page.id}`,
					pageId: page.id,
					kind: 'IIIF',
					label: `${label} — IIIF info`,
					uri: page.thumbnailUrl.replace(/\/full\/.*/, '/info.json'),
					fileName: `${fileBaseName}.iiif.json`,
					mediaType: 'application/json',
					enrichmentType: 'DIGITAL_OBJECT',
					snapshotSupported: false,
				});
			}

			if (this.reactAppApi && page.id !== undefined) {
				const vtlApiPath = `/pages/${page.id}/content?type=hocr`;
				resources.push({
					id: `hocr-${page.id}`,
					pageId: page.id,
					kind: 'hOCR',
					label: `${label} — hOCR`,
					uri: `${this.reactAppApi}${vtlApiPath}`,
					vtlApiPath,
					fileName: `${fileBaseName}.hocr.html`,
					mediaType: 'text/html',
					enrichmentType: 'OCR',
					snapshotSupported: true,
				});
			}

			return resources;
		});
	}

	async createHdt(command, { onOperationUpdate } = {}) {
		const accepted = await this.platformClient.createHdt(command, {
			idempotencyKey: idempotencyKey('hdt:create', command),
		});
		const operation = await this.platformClient.waitForOperation(accepted.operationId, {
			onUpdate: onOperationUpdate,
		});
		const discovered = await this.platformClient.findMyHdtByHeritageEntityUri(command.heritageEntityUri);
		return {
			hdt: discovered || {
				hdtId: accepted.hdtId,
				canonicalUri: accepted.canonicalUri,
				name: command.name,
				heritageEntityUri: command.heritageEntityUri,
				projectUri: command.projectUri,
				registrationState: 'REGISTERED',
			},
			operation,
		};
	}

	async loadHdtState(hdt) {
		if (!hdt?.hdtId) return null;
		const metadata = await this.platformClient.getHdt(hdt.hdtId);
		let semanticState;
		try {
			semanticState = await this.inspectHdtDigitalObjects(hdt);
		}
		catch (error) {
			semanticState = { assetUris: [], assets: [], error };
		}
		return {
			...hdt,
			...metadata,
			semanticState,
		};
	}

	async inspectHdtDigitalObjects(hdt) {
		if (!hdt?.canonicalUri) return { assetUris: [], assets: [] };
		const query = `SELECT DISTINCT ?asset WHERE {\n  ${sparqlIri(hdt.canonicalUri)} ${sparqlIri(HDTO_HP29)} ?asset .\n} ORDER BY ?asset LIMIT 100`;
		const result = await this.platformClient.executeSemanticQuery(query);
		const assetUris = (result?.results?.bindings || [])
			.map((binding) => binding.asset?.value)
			.filter(Boolean);
		const assetIds = [...new Set(assetUris.map(assetIdFromCanonicalUri).filter(Boolean))];
		const assets = await Promise.all(assetIds.map((assetId) => this.platformClient.getAsset(assetId)));
		return { query, assetUris, assets };
	}

	async loadRecentOperations() {
		const collection = await this.platformClient.listOperations({ limit: 8 });
		return collection.items || [];
	}

	async enrichAsset(hdtId, resource, asset, { onOperationUpdate } = {}) {
		const command = {
			hdtId,
			assetId: asset.assetId,
			label: `VTL ${resource.kind}: ${resource.label}`,
			enrichmentType: resource.enrichmentType,
		};
		const accepted = await this.platformClient.enrichWithDigitalObject(command, {
			idempotencyKey: idempotencyKey('hdt:enrich', command),
		});
		const operation = await this.platformClient.waitForOperation(accepted.operationId, {
			onUpdate: onOperationUpdate,
		});
		return operation;
	}

	async addReferencedResourceToHdt(hdt, resource, { onOperationUpdate } = {}) {
		const asset = await this.platformClient.registerExternalAsset({
			uri: resource.uri,
			fileName: resource.fileName,
			mediaType: resource.mediaType,
		});
		const operation = await this.enrichAsset(hdt.hdtId, resource, asset, { onOperationUpdate });
		const verification = await this.inspectHdtDigitalObjects(hdt);
		return { asset, operation, verification };
	}

	async loadVtlSnapshot(resource) {
		if (!resource.snapshotSupported || !resource.vtlApiPath) {
			throw new Error('This VTL resource does not expose a browser-readable snapshot source');
		}
		const response = await api.get(resource.vtlApiPath, {
			responseType: 'arraybuffer',
			headers: { Accept: resource.mediaType },
		});
		return new Blob([response.data], { type: resource.mediaType });
	}

	async storeSnapshotResourceInHdt(hdt, resource, { onOperationUpdate } = {}) {
		const content = await this.loadVtlSnapshot(resource);
		const sha256 = await sha256Hex(content);
		const initiated = await this.platformClient.initiateManagedAssetUpload({
			fileName: resource.fileName,
			contentType: resource.mediaType,
			contentLength: content.size,
			sha256,
		});
		await this.platformClient.uploadBinary({
			uploadUri: initiated.uploadUri,
			requiredHeaders: initiated.requiredHeaders,
			content,
		});
		await this.platformClient.completeManagedAssetUpload(initiated.uploadId);
		const asset = await this.platformClient.getAsset(initiated.assetId);
		const operation = await this.enrichAsset(hdt.hdtId, resource, asset, { onOperationUpdate });
		const verification = await this.inspectHdtDigitalObjects(hdt);
		return { asset, operation, verification };
	}
}

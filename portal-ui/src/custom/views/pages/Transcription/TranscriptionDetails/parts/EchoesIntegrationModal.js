import React, { useEffect, useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { Form, Modal } from 'react-bootstrap';
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';

import Button from '@Elements/Button/Button';
import EchoesPlatformClient from '@/services/echoes/echoesPlatformClient';
import { getEchoesAccessToken } from '@/services/echoes/echoesTokenProvider';
import VtlEchoesIntegrationService from '@/services/echoes/vtlEchoesIntegrationService';

import styles from './EchoesIntegrationModal.module.sass';

const formatDate = (value) => value ? new Date(value).toLocaleString() : '—';

const EchoesIntegrationModal = ({ show, onHide, transcriptionId }) => {
	const { t } = useTranslation();
	const config = useSelector((state) => state.app.config);
	const [loading, setLoading] = useState(false);
	const [stateLoading, setStateLoading] = useState(false);
	const [error, setError] = useState(null);
	const [status, setStatus] = useState('');
	const [hdts, setHdts] = useState([]);
	const [resources, setResources] = useState([]);
	const [operations, setOperations] = useState([]);
	const [currentOperation, setCurrentOperation] = useState(null);
	const [selectedHdtId, setSelectedHdtId] = useState('');
	const [autoMatchedHdtId, setAutoMatchedHdtId] = useState('');
	const [selectedResourceId, setSelectedResourceId] = useState('');
	const [assetMode, setAssetMode] = useState('REFERENCE');
	const [showCreate, setShowCreate] = useState(false);
	const [createForm, setCreateForm] = useState({
		heritageEntityUri: '',
		projectUri: '',
		name: '',
		description: '',
	});
	const [hdtState, setHdtState] = useState(null);
	const [lastAsset, setLastAsset] = useState(null);

	const platformClient = useMemo(() => new EchoesPlatformClient({
		baseUrl: config.commonPlatformApi,
		getAccessToken: getEchoesAccessToken,
	}), [config.commonPlatformApi]);

	const integration = useMemo(() => new VtlEchoesIntegrationService({
		platformClient,
		reactAppApi: config.reactAppApi,
		projectUri: config.commonPlatformProjectUri,
	}), [platformClient, config.reactAppApi, config.commonPlatformProjectUri]);

	const selectedHdt = useMemo(
		() => hdts.find((hdt) => hdt.hdtId === selectedHdtId) || null,
		[hdts, selectedHdtId],
	);
	const selectedResource = resources.find((resource) => resource.id === selectedResourceId);
	const semanticAssets = hdtState?.semanticState?.assets || [];
	const snapshotEnabled = Boolean(config.commonPlatformManagedUploadEnabled && selectedResource?.snapshotSupported);

	const clearFeedback = () => {
		setError(null);
		setStatus('');
	};

	const showError = (operationError) => {
		setStatus('');
		setError({
			message: operationError?.message || t('echoesIntegration.status.failed'),
			status: operationError?.status,
			reason: operationError?.problem?.reason,
			correlationId: operationError?.correlationId || operationError?.problem?.correlationId,
		});
	};

	const refreshOperations = async () => {
		try {
			setOperations(await integration.loadRecentOperations());
		}
		catch (operationError) {
			// Operation history is supporting information; it must not make the main workflow fail.
			console.log(operationError); // eslint-disable-line
		}
	};

	const refreshHdtState = async (hdt = selectedHdt) => {
		if (!hdt) {
			setHdtState(null);
			return;
		}
		setStateLoading(true);
		try {
			setHdtState(await integration.loadHdtState(hdt));
		}
		catch (stateError) {
			showError(stateError);
		}
		finally {
			setStateLoading(false);
		}
	};

	const load = async () => {
		setLoading(true);
		clearFeedback();
		setStatus(t('echoesIntegration.status.loading'));
		setLastAsset(null);
		setCurrentOperation(null);
		try {
			const context = await integration.loadContext(transcriptionId);
			setHdts(context.hdts);
			setResources(context.resources);
			setOperations(context.operations);
			setCreateForm(context.createDefaults);
			setAutoMatchedHdtId(context.matchingHdt?.hdtId || '');
			setSelectedHdtId((current) => {
				if (current && context.hdts.some((hdt) => hdt.hdtId === current)) return current;
				return context.matchingHdt?.hdtId || context.hdts[0]?.hdtId || '';
			});
			setSelectedResourceId((current) => current || context.resources[0]?.id || '');
			setStatus(context.matchingHdt
				? t('echoesIntegration.status.hdtAutoMatched')
				: t('echoesIntegration.status.noHdtMatch'));
		}
		catch (loadError) {
			showError(loadError);
		}
		finally {
			setLoading(false);
		}
	};

	useEffect(() => {
		if (show) load();
	}, [show]); // eslint-disable-line react-hooks/exhaustive-deps

	useEffect(() => {
		if (show && selectedHdt) refreshHdtState(selectedHdt);
		else setHdtState(null);
	}, [show, selectedHdtId]); // eslint-disable-line react-hooks/exhaustive-deps

	useEffect(() => {
		if (assetMode === 'SNAPSHOT' && !snapshotEnabled) setAssetMode('REFERENCE');
	}, [selectedResourceId, config.commonPlatformManagedUploadEnabled]); // eslint-disable-line react-hooks/exhaustive-deps

	const createHdt = async () => {
		if (!createForm.heritageEntityUri || !createForm.projectUri) return;
		setLoading(true);
		clearFeedback();
		setLastAsset(null);
		setCurrentOperation(null);
		setStatus(t('echoesIntegration.status.creatingHdt'));
		try {
			const result = await integration.createHdt(createForm, { onOperationUpdate: setCurrentOperation });
			const hdt = result.hdt;
			setHdts((current) => [hdt, ...current.filter((item) => item.hdtId !== hdt.hdtId)]);
			setSelectedHdtId(hdt.hdtId);
			setAutoMatchedHdtId(hdt.hdtId);
			setShowCreate(false);
			setCurrentOperation(result.operation);
			setStatus(t('echoesIntegration.status.hdtCreated'));
			await refreshOperations();
		}
		catch (createError) {
			showError(createError);
		}
		finally {
			setLoading(false);
		}
	};

	const addResource = async () => {
		if (!selectedHdt || !selectedResource) return;
		setLoading(true);
		clearFeedback();
		setLastAsset(null);
		setCurrentOperation(null);
		setStatus(assetMode === 'SNAPSHOT'
			? t('echoesIntegration.status.storingSnapshot')
			: t('echoesIntegration.status.addingResource'));
		try {
			const options = { onOperationUpdate: setCurrentOperation };
			const result = assetMode === 'SNAPSHOT'
				? await integration.storeSnapshotResourceInHdt(selectedHdt, selectedResource, options)
				: await integration.addReferencedResourceToHdt(selectedHdt, selectedResource, options);
			setLastAsset(result.asset);
			setCurrentOperation(result.operation);
			setHdtState((current) => current ? {
				...current,
				semanticState: result.verification,
			} : current);
			setStatus(assetMode === 'SNAPSHOT'
				? t('echoesIntegration.status.snapshotStored')
				: t('echoesIntegration.status.resourceAdded'));
			await refreshOperations();
		}
		catch (addError) {
			showError(addError);
		}
		finally {
			setLoading(false);
		}
	};

	const verifySemanticState = async () => {
		if (!selectedHdt) return;
		setStateLoading(true);
		clearFeedback();
		setStatus(t('echoesIntegration.status.verifying'));
		try {
			const semanticState = await integration.inspectHdtDigitalObjects(selectedHdt);
			setHdtState((current) => ({ ...(current || selectedHdt), semanticState }));
			setStatus(t('echoesIntegration.status.verified', { count: semanticState.assets.length }));
		}
		catch (verificationError) {
			showError(verificationError);
		}
		finally {
			setStateLoading(false);
		}
	};

	const openAssetContent = async (asset) => {
		try {
			if (asset.managementMode === 'REFERENCED') {
				const contentUrl = asset.links?.content || asset.externalUri;
				if (contentUrl) window.open(contentUrl, '_blank', 'noopener,noreferrer');
				return;
			}
			const blob = await platformClient.getManagedAssetContent(asset.assetId);
			const contentUrl = URL.createObjectURL(blob);
			window.open(contentUrl, '_blank', 'noopener,noreferrer');
			window.setTimeout(() => URL.revokeObjectURL(contentUrl), 60000);
		}
		catch (contentError) {
			showError(contentError);
		}
	};

	return (
		<Modal show={show} onHide={onHide} dialogClassName='modal-dialog-centered' size='xl'>
			<Modal.Header closeButton>
				<Modal.Title>{t('echoesIntegration.title')}</Modal.Title>
			</Modal.Header>
			<Modal.Body>
				<div className={styles.echoes}>
					<p className={styles.echoes__intro}>{t('echoesIntegration.intro')}</p>

					{!config.commonPlatformApi && (
						<div className={`${styles.echoes__status} ${styles.echoes__statusError}`}>
							{t('echoesIntegration.missingApi')}
						</div>
					)}

					<div className={styles.echoes__section}>
						<div className={styles.echoes__sectionHeader}>
							<div className={styles.echoes__sectionTitle}>{t('echoesIntegration.hdtSection')}</div>
							{selectedHdtId && selectedHdtId === autoMatchedHdtId && (
								<span className={styles.echoes__badge}>{t('echoesIntegration.autoMatched')}</span>
							)}
						</div>
						<div className={styles.echoes__row}>
							<Form.Group className={styles.echoes__grow}>
								<Form.Label>{t('echoesIntegration.hdt')}</Form.Label>
								<Form.Control
									as='select'
									value={selectedHdtId}
									onChange={(event) => setSelectedHdtId(event.target.value)}
									disabled={loading}
								>
									<option value=''>{t('echoesIntegration.selectHdt')}</option>
									{hdts.map((hdt) => (
										<option key={hdt.hdtId} value={hdt.hdtId}>
											{hdt.name || hdt.hdtId}
										</option>
									))}
								</Form.Control>
							</Form.Group>
							<Button variant='secondary' onClick={load} disabled={loading}>
								{t('echoesIntegration.refresh')}
							</Button>
							<Button variant='secondary' onClick={() => setShowCreate((value) => !value)} disabled={loading}>
								{t('echoesIntegration.createHdt')}
							</Button>
						</div>

						{selectedHdt && (
							<div className={styles.echoes__summary}>
								<div className={styles.echoes__summaryLine}><strong>HDT ID:</strong> {selectedHdt.hdtId}</div>
								<div className={styles.echoes__summaryLine}><strong>{t('echoesIntegration.heritageEntityUri')}:</strong> {selectedHdt.heritageEntityUri}</div>
								<div className={styles.echoes__summaryLine}><strong>{t('echoesIntegration.canonicalUri')}:</strong> {selectedHdt.canonicalUri}</div>
								{hdtState && (
									<div className={styles.echoes__facts}>
										<span><strong>{t('echoesIntegration.registration')}:</strong> {hdtState.registrationState}</span>
										<span><strong>{t('echoesIntegration.contentRevision')}:</strong> {hdtState.contentRevision}</span>
										<span><strong>{t('echoesIntegration.synchronisation')}:</strong> {hdtState.synchronisationState}</span>
									</div>
								)}
							</div>
						)}

						{showCreate && (
							<div className={styles.echoes__summary}>
								<Form.Group>
									<Form.Label>{t('echoesIntegration.hdtName')}</Form.Label>
									<Form.Control value={createForm.name} onChange={(event) => setCreateForm({ ...createForm, name: event.target.value })} />
								</Form.Group>
								<Form.Group>
									<Form.Label>{t('echoesIntegration.heritageEntityUri')}</Form.Label>
									<Form.Control value={createForm.heritageEntityUri} onChange={(event) => setCreateForm({ ...createForm, heritageEntityUri: event.target.value })} />
								</Form.Group>
								<Form.Group>
									<Form.Label>{t('echoesIntegration.projectUri')}</Form.Label>
									<Form.Control value={createForm.projectUri} onChange={(event) => setCreateForm({ ...createForm, projectUri: event.target.value })} />
								</Form.Group>
								<div className={styles.echoes__actions}>
									<Button variant='primary' onClick={createHdt} disabled={loading || !createForm.heritageEntityUri || !createForm.projectUri}>
										{t('echoesIntegration.create')}
									</Button>
								</div>
							</div>
						)}
					</div>

					<div className={styles.echoes__section}>
						<div className={styles.echoes__sectionTitle}>{t('echoesIntegration.resourceSection')}</div>
						<Form.Group>
							<Form.Label>{t('echoesIntegration.resource')}</Form.Label>
							<Form.Control
								as='select'
								value={selectedResourceId}
								onChange={(event) => setSelectedResourceId(event.target.value)}
								disabled={loading}
							>
								<option value=''>{t('echoesIntegration.selectResource')}</option>
								{resources.map((resource) => (
									<option key={resource.id} value={resource.id}>{resource.label}</option>
								))}
							</Form.Control>
						</Form.Group>
						{selectedResource && <div className={styles.echoes__hint}>{selectedResource.uri}</div>}

						<div className={styles.echoes__modeSelector}>
							<Form.Check
								type='radio'
								id='echoes-asset-reference'
								name='echoes-asset-mode'
								checked={assetMode === 'REFERENCE'}
								onChange={() => setAssetMode('REFERENCE')}
								label={t('echoesIntegration.referenceMode')}
							/>
							<div className={styles.echoes__hint}>{t('echoesIntegration.referenceModeHint')}</div>
							<Form.Check
								type='radio'
								id='echoes-asset-snapshot'
								name='echoes-asset-mode'
								checked={assetMode === 'SNAPSHOT'}
								onChange={() => setAssetMode('SNAPSHOT')}
								disabled={!snapshotEnabled}
								label={t('echoesIntegration.snapshotMode')}
							/>
							<div className={styles.echoes__hint}>
								{!config.commonPlatformManagedUploadEnabled
									? t('echoesIntegration.snapshotDisabled')
									: selectedResource?.snapshotSupported
										? t('echoesIntegration.snapshotModeHint')
										: t('echoesIntegration.snapshotUnavailable')}
							</div>
						</div>

						<div className={styles.echoes__actions}>
							<Button variant='primary' onClick={addResource} disabled={loading || !selectedHdt || !selectedResource}>
								{assetMode === 'SNAPSHOT' ? t('echoesIntegration.storeSnapshot') : t('echoesIntegration.addToHdt')}
							</Button>
						</div>
					</div>

					<div className={styles.echoes__section}>
						<div className={styles.echoes__sectionHeader}>
							<div className={styles.echoes__sectionTitle}>{t('echoesIntegration.platformStateSection')}</div>
							<Button variant='secondary' onClick={verifySemanticState} disabled={stateLoading || !selectedHdt}>
								{t('echoesIntegration.verify')}
							</Button>
						</div>
						<p className={styles.echoes__hint}>{t('echoesIntegration.platformStateHint')}</p>
						{stateLoading && <div className={styles.echoes__hint}>{t('echoesIntegration.status.verifying')}</div>}
						{hdtState?.semanticState?.error && (
							<div className={`${styles.echoes__status} ${styles.echoes__statusWarning}`}>
								{t('echoesIntegration.semanticQueryUnavailable')}: {hdtState.semanticState.error.message}
							</div>
						)}
						{!stateLoading && !hdtState?.semanticState?.error && semanticAssets.length === 0 && (
							<div className={styles.echoes__empty}>{t('echoesIntegration.noSemanticAssets')}</div>
						)}
						{semanticAssets.map((asset) => (
							<div className={styles.echoes__asset} key={asset.assetId}>
								<div className={styles.echoes__assetMain}>
									<strong>{asset.fileName || asset.assetId}</strong>
									<div className={styles.echoes__hint}>{asset.canonicalUri}</div>
									<div className={styles.echoes__facts}>
										<span>{asset.managementMode}</span>
										<span>{asset.mediaType || '—'}</span>
										<span>{asset.status}</span>
									</div>
								</div>
								<Button variant='secondary' onClick={() => openAssetContent(asset)}>
									{t('echoesIntegration.openContent')}
								</Button>
							</div>
						))}
					</div>

					<div className={styles.echoes__section}>
						<div className={styles.echoes__sectionHeader}>
							<div className={styles.echoes__sectionTitle}>{t('echoesIntegration.operationsSection')}</div>
							<Button variant='secondary' onClick={refreshOperations} disabled={loading}>
								{t('echoesIntegration.refresh')}
							</Button>
						</div>
						<p className={styles.echoes__hint}>{t('echoesIntegration.operationsHint')}</p>
						{currentOperation && (
							<div className={`${styles.echoes__operation} ${styles.echoes__operationCurrent}`}>
								<strong>{currentOperation.operationType}</strong>
								<span>{currentOperation.status}</span>
								<code>{currentOperation.operationId}</code>
							</div>
						)}
						<div className={styles.echoes__operations}>
							{operations.map((operation) => (
								<div className={styles.echoes__operation} key={operation.operationId}>
									<div>
										<strong>{operation.operationType}</strong>
										<div className={styles.echoes__hint}>{formatDate(operation.createdAt)}</div>
									</div>
									<span className={styles.echoes__operationStatus}>{operation.status}</span>
									<code>{operation.operationId}</code>
								</div>
							))}
						</div>
					</div>

					{status && (
						<div className={`${styles.echoes__status} ${lastAsset ? styles.echoes__statusSuccess : styles.echoes__statusInfo}`}>
							{status}
						</div>
					)}
					{error && (
						<div className={`${styles.echoes__status} ${styles.echoes__statusError}`}>
							<div>{error.message}</div>
							{error.reason && <div><strong>{t('echoesIntegration.reason')}:</strong> {error.reason}</div>}
							{error.correlationId && <div><strong>{t('echoesIntegration.correlationId')}:</strong> {error.correlationId}</div>}
						</div>
					)}
					{lastAsset && (
						<div className={styles.echoes__summary}>
							<div className={styles.echoes__summaryLine}><strong>{t('echoesIntegration.lastAsset')}:</strong> {lastAsset.assetId}</div>
							<div className={styles.echoes__summaryLine}><strong>{t('echoesIntegration.storage')}:</strong> {lastAsset.managementMode}</div>
						</div>
					)}
				</div>
			</Modal.Body>
			<Modal.Footer>
				<Button variant='secondary' onClick={onHide}>{t('echoesIntegration.close')}</Button>
			</Modal.Footer>
		</Modal>
	);
};

EchoesIntegrationModal.propTypes = {
	show: PropTypes.bool,
	onHide: PropTypes.func,
	transcriptionId: PropTypes.number,
};

export default EchoesIntegrationModal;

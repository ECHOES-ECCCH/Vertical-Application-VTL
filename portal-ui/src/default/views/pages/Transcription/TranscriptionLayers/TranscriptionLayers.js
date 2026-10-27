import React, { useState, useEffect } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { useParams } from "react-router";
import { reverse } from "named-urls";
import { useHistory, Link } from "react-router-dom";
import { Container } from "react-bootstrap";

import api from "@/services/api";
import url from "@/router/urls";

import { appActions } from "@/store/actions/app.actions";
import { alertActions } from "@/store/actions/alert.actions";

import { CheckViewModePermission } from "@/permissions/ViewModes";

import Button from "@Elements/Button/Button";
import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import Alert from "@Elements/Alert/Alert";

import TranscriptionBackLink from "@Pages/Transcription/parts/TranscriptionBackLink";

import SearchTranscriptions from "@Elements/Search/SearchTranscriptions";
import SearchTypeEnum from "@/utils/SearchTypeEnum";

import styles from "@Pages/Transcription/TranscriptionLayers/TranscriptionLayers.module.sass";

import ModalEditLayer from '@Elements/Modal/parts/LayersActions/ModalEditLayer';
import ModalWrapperChildren from "@Elements/Modal/ModalWrapperChildren";

import ModalBaseCancelConfirm from '@Elements/Modal/ModalBaseCancelConfirm';

const TranscriptionLayers = () => {
	const dispatch = useDispatch();
	const { t } = useTranslation();

	const isLoggedIn = useSelector(state => state.auth.credentials.isLoggedIn);
	const isLoggingEnd = useSelector(state => state.auth.credentials.isLoggingEnd);
	const { transcriptionId } = useParams();
	const history = useHistory();
	const [layers, setLayers] = useState([]);
	const [useMetadata, setUseMetadata] = useState(SearchTypeEnum.ALL.toString());
	const [viewMode, setViewMode] = useState(null);

	const [title, setTitle] = useState("");
	const [dataLoaded, setDataLoaded] = useState(false);
	const [summarizeData, setSummarizeData] = useState([]);
	const [searchString, setSearchString] = useState("");
	const [layerToDelete, setLayerToDelete] = useState(null);
	const [layerToEdit, setLayerToEdit] = useState(null);

	const linkToTranscriptionDetails = reverse(url.transcription.details, { transcriptionId: transcriptionId });

	useEffect(() => {
		reloadData();
	}, [isLoggedIn, isLoggingEnd]);

	const reloadData = () => {
		dispatch(appActions.setLoading(true));
		getTranscriptionLayersData();
		getTranscriptionSummarizeData();
	};

	const getTranscriptionLayersData = () => {
		api.get(`/transcriptions/${transcriptionId}/layer`)
			.then(({ data }) => {
				setLayers(data);
			})
			.catch(() => {
				history.push(linkToTranscriptionDetails);
			});
	};

	const getTranscriptionSummarizeData = () => {
		api.get(`/transcriptions/${transcriptionId}`).then(({ data }) => {
			setViewMode(data.viewMode);
			setSummarizeData(data);
			setTitle(data.titleLabel);
			dispatch(appActions.setLoading(false));
			setDataLoaded(true);
		});
	};

	const breadcrumbsList = [
		{ id: 1, label: "home", link: url.home },
		{ id: 2, label: t('breadcrumbs.transcriptions'), link: url.transcriptions },
		{ id: 3, label: title, link: linkToTranscriptionDetails },
		{ id: 4, label: t('breadcrumbs.layers') },
	];

	const putEditedLayer = async (layer) => {
		dispatch(appActions.setLoading(true));
		try {
			await api.put(`/transcriptions/${transcriptionId}/layer/${layer.id}`, {
				...layer,
				transcriptionId: transcriptionId,
			});
		} catch (err) {
			console.log(err); // eslint-disable-line
		} finally {
			setLayerToEdit(null);
			reloadData();
		}
	};

	const deleteLayerConfirmed = async () => {
		dispatch(appActions.setLoading(true));
		dispatch(alertActions.clearAlert());
		await api.delete(`/transcriptions/${transcriptionId}/layer/${layerToDelete.id}`);
		setLayerToDelete(null);
		reloadData();
		dispatch(appActions.setLoading(false));
	};

	return (
		<>
			{/* EDIT LAYER MODAL */}
			<ModalWrapperChildren
				modalVisible={!!layerToEdit}
				setModalVisible={() => setLayerToEdit(null)}
				title={t('modals.editTranscriptionLayer.title')}
			>
				<ModalEditLayer
					handleOnClose={() => setLayerToEdit(null)}
					layer={layerToEdit}
					onEditComplete={putEditedLayer}
				/>
			</ModalWrapperChildren>

			{/* DELETE LAYER MODAL */}
			<ModalBaseCancelConfirm
				modalVisible={!!layerToDelete}
				setModalVisible={() => setLayerToDelete(null)}
				title={t('modals.deleteTranscriptionLayer.title')}
				bodyHTML={t('modals.deleteTranscriptionLayer.bodyHTML')}
				cancelButtonName={t('form.cancel')}
				confirmButtonName={t('modals.deleteTranscriptionLayer.button')}
				onConfirm={deleteLayerConfirmed}
			/>

			{dataLoaded && (
				<main>
					<SearchTranscriptions
						setSearchString={setSearchString}
						searchString={searchString}
						setUseMetadata={setUseMetadata}
					/>
					<Container className={styles["layers"]}>
						<Breadcrumbs breadcrumbsList={breadcrumbsList} />
						<Alert />
						<TranscriptionBackLink transcriptionId={transcriptionId} />
						<div className={styles["layers__nav-wrapper"]}>
							<div className='block__header block__header--space-between'>
								<h1 className='text--title'>{t('breadcrumbs.layers')}</h1>
							</div>
						</div>
						<div className={styles["layers__status"]}>
							<div>
								{t('transcriptions.description.name')} <b>{summarizeData.titleLabel}</b>
							</div>
							<div>
								{t('transcriptions.description.projectAuthor')}
								<b>{summarizeData?.creator?.email || ''}</b>
							</div>
						</div>
						<div className={styles["layers__status"]}>
							{t('transcriptions.description.layersDesc')}
						</div>

						<div className={styles["layers__table--caption"]}>
							{t('breadcrumbs.layers')} ({layers.length})
						</div>
						<div className={`${styles["layers__table--header"]} ${styles["layers__table--row"]}`}>
							<div>{t('transcriptions.layers.no')}</div>
							<div>{t('transcriptions.layers.name')}</div>
							<div>{t('transcriptions.layers.desc')}</div>
							<div />
						</div>

						{layers.length && layers.map((layer, index) => (
							<div key={index} className={`${styles["layers__table--item"]} ${CheckViewModePermission("canOperateOnTranscriptionLayers", viewMode) ? styles["layers__table--row"] : styles["layers__table--row__readOnly"]} ${layers?.length <= 1 ? styles["layers__table--row-one"] : ""}`}>
								<div>{index + 1}</div>
								<div className={styles["layers__table--item-desc"]}>{t('transcriptions.layers.name')}</div>
								<div className={styles["layers__table--item-name"]}>{layer.name}</div>
								<div className={styles["layers__table--item-desc"]}>{t('transcriptions.layers.desc')}</div>
								<div>{layer.description}</div>
								{CheckViewModePermission("canOperateOnTranscriptionLayers", viewMode) &&
									<div className={`${styles["buttons"]} ${styles["layers__table--col-button"]} ${layers?.length <= 1 ? styles["buttonOne"] : ""}`}>
										{layers?.length > 1 &&
											<Button
												variant='secondary'
												onClick={() => setLayerToDelete(layer)}
											>
												{t('transcriptions.layers.delete')}
											</Button>}
										<Button
											variant='primary'
											onClick={() => setLayerToEdit(layer)}
										>
											{t('transcriptions.layers.edit')}
										</Button>
									</div>}
							</div>
						))}

						{CheckViewModePermission("canOperateOnTranscriptionLayers", viewMode) &&
							<div className={styles["layers__form"]}>
								<Link
									disabled={layers.length > 29}
									to={reverse(url.transcription.addLayer, { transcriptionId: transcriptionId })}
								>
									<Button
										variant='primary'
										type='button'
										disabled={layers.length > 29}
									>
										{t('transcriptions.layers.addNew')}
									</Button>
								</Link>
							</div>}
					</Container>
				</main >
			)}
		</>
	);
};

export default TranscriptionLayers;

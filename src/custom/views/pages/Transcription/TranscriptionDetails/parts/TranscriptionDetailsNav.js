import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useDispatch, useSelector } from 'react-redux';
import PropTypes from "prop-types";
import { Dropdown } from 'react-bootstrap';
import { useHistory } from "react-router-dom";
import { reverse } from 'named-urls';
import url from "@/router/urls";
import api from "@/services/api";
import { appActions } from "@/store/actions/app.actions";

import Button from '@Elements/Button/Button';
import Alert from "@Elements/Alert/Alert";
import { CheckViewModePermission } from "@/permissions/ViewModes";

import ModalBaseCancelConfirm from '@Elements/Modal/ModalBaseCancelConfirm';

import styles from "@Pages/Transcription/TranscriptionDetails/TranscriptionDetails.module.sass";

const TranscriptionDetailsNav = ({ viewMode, transcriptionId, title, hidden, creationMode }) => {
	const { t } = useTranslation();
	const history = useHistory();
	const dispatch = useDispatch();
	const reactAppApi = useSelector((state) => state.app.config.reactAppApi);
	const knowledgeBaseProxyUrl = useSelector((state) => state.app.config.knowledgeBaseProxyUrl);
	const echoesUi = useSelector((state) => state.app.config.echoesUi);
	const [showModalUnpublish, setShowModalUnpublish] = useState(false);
	const [showModalPublish, setShowModalPublish] = useState(false);
	const [showModalRemoveTranscription, setShowModalRemoveTranscription] = useState(false);
	const [showModalCreateDigitalTwin, setShowModalCreateDigitalTwin] = useState(false);

	const onEditTranscription = (publish) => {
		const location = reverse(url.transcription.edit, { transcriptionId: parseInt(transcriptionId) });
		let query = "";
		if (publish) {
			query = "?publish=true";
		}
		history.push(location + query);
	};

	const modalCommitCreateDigitalTwin = async () => {
		dispatch(appActions.setLoading(true));
		try {
			console.log("modalCommitCreateDigitalTwin");
			const transcriptionResponse = await api.get(`/transcriptions/${transcriptionId}`, { params: { perpage: 100 } });
			const transcription = transcriptionResponse.data;

			const hdt = {
				name: transcription.analyticInfo.title,
				resources: transcription.pages.list.flatMap((page) => {
					const { boundingBox: { lowerRightX, lowerRightY, upperLeftX, upperLeftY } } = page;
					return [
						{
							name: page.path,
							fileUrl: page.thumbnailUrl.replace(/\/full\/.*/, "/info.json"),
							fileMediaType: "application/json",
							destinationStorage: "EXTERNAL",
							attributes: {
								width: lowerRightX - upperLeftX,
								height: lowerRightY - upperLeftY,
							},
						},
						{
							name: page.path.replace(/(\.[^./]*)?$/, '.hocr'),
							fileUrl: `${reactAppApi}/pages/${page.id}/content?type=hocr`,
							fileMediaType: "text/html",
							destinationStorage: "EXTERNAL",
						},
					];
				}),
			};
			console.log("hdt", hdt);

			const accessTokenResponse = await api.get("/oauth2/accessToken");
			const accessToken = accessTokenResponse.data;

			const hdtResponse = await fetch(knowledgeBaseProxyUrl, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					'Authorization': `Bearer ${accessToken}`,
				},
				body: JSON.stringify(hdt),
			});

			if (!hdtResponse.ok) {
				throw new Error(`Failed to create digital twin: ${hdtResponse.status}`);
			}

			console.log("hdtResponse", hdtResponse);

			const hdtData = await hdtResponse.json();
			const hdtId = hdtData.id;
			const hdtLink = `${echoesUi}/data/${hdtId}`;
			const graphUri = hdtData.graphUri;

			// Open the link in a new tab
			window.open(hdtLink, '_blank');

			await api.put(`/transcriptions/${transcriptionId}/metadata`, {
				'analyticInfo': transcription.analyticInf,
				'monographicInfo': transcription.monographicInfo,
				'profileDesc': {
					...transcription.profileDesc,
					links: [
						...transcription.profileDesc.links,
						hdtLink,
						graphUri,
					],
				},
				'transcriptionInfo': transcription.transcriptionInfo,
			});

			window.location.reload();
		}
		catch (error) {
			console.log(error); // eslint-disable-line
		}
		finally {
			setShowModalCreateDigitalTwin(false);
			dispatch(appActions.setLoading(false));
		}
	};

	const modalCommitPublish = async (publish) => {
		dispatch(appActions.setLoading(true));
		try {
			await api.put(`/transcriptions/${transcriptionId}/hidden`, { "hidden": !publish });
			window.location.reload();
		}
		catch (error) {
			console.log(error); // eslint-disable-line
		}
	};

	const modalCommitRemoveTranscription = async () => {
		dispatch(appActions.setLoading(true));
		try {
			api.delete(`/transcriptions/${transcriptionId}`);
			history.replace(url.transcriptionsUser);
		}
		catch (error) {
			console.log(error); // eslint-disable-line
		}
		finally {
			dispatch(appActions.setLoading(false));
		}
	};

	return (
		<>
			{/* Modal create digital twin */}
			<ModalBaseCancelConfirm
				modalVisible={showModalCreateDigitalTwin}
				setModalVisible={setShowModalCreateDigitalTwin}
				title={t('modals.createDigitalTwin.title')}
				bodyHTML={t('modals.createDigitalTwin.bodyHTML')}
				cancelButtonName={t('form.cancel')}
				confirmButtonName={t('modals.createDigitalTwin.button')}
				onConfirm={modalCommitCreateDigitalTwin}
			/>
			
			{/* Modal remove transcription */}
			<ModalBaseCancelConfirm
				modalVisible={showModalRemoveTranscription}
				setModalVisible={setShowModalRemoveTranscription}
				title={t('modals.deleteTranscription.title')}
				bodyHTML={t('modals.deleteTranscription.bodyHTML')}
				cancelButtonName={t('form.cancel')}
				confirmButtonName={t('modals.deleteTranscription.button')}
				onConfirm={modalCommitRemoveTranscription}
			/>
			
			{/* Modal unpublish transcription */}
			<ModalBaseCancelConfirm
				modalVisible={showModalUnpublish}
				setModalVisible={setShowModalUnpublish}
				title={t('modals.depublishTranscription.title')}
				body={t('modals.depublishTranscription.bodyHTML')}
				cancelButtonName={t('form.cancel')}
				confirmButtonName={t('modals.depublishTranscription.button')}
				onConfirm={() => modalCommitPublish(false)}
			/>

			{/* Modal publish transcription */}
			<ModalBaseCancelConfirm
				modalVisible={showModalPublish}
				setModalVisible={setShowModalPublish}
				title={t('modals.publishTranscription.title')}
				body={t('modals.publishTranscription.bodyHTML')}
				cancelButtonName={t('form.cancel')}
				confirmButtonName={t('modals.publishTranscription.button')}
				onConfirm={() => modalCommitPublish(true)}
			/>

			<div className={styles["transcription__header"]}>
				<div>
					<div className='block__header block__header--space-between'>
						<h1 className={styles["transcription__title"]}>
							<span>{title}</span>
						</h1>
					</div>
					<Alert alertBoxName='titleBox' />
					<div className={styles["transcription__mobileNav"]}>
						{(viewMode === 'admin') &&
							<Dropdown className={styles["transcription__mobileNav--dropdown"]}>
								<Dropdown.Toggle
									variant='dropdown-secondary'
									id='details'
								>
									<i className='icon-hamburger' />
									{t('transcription.details.details')}
									<i className='icon-arrow-down-full' />
								</Dropdown.Toggle>
								<Dropdown.Menu className={styles["transcription__mobileNav--dropdown-menu"]}>
									{CheckViewModePermission("canRemoveTranscription", viewMode) &&
										<Dropdown.Item
											onClick={() => setShowModalCreateDigitalTwin(true)}
											disabled={hidden}
										>
											{t('transcription.details.createDigitalTwin')}
										</Dropdown.Item>}
									{CheckViewModePermission("canRemoveTranscription", viewMode) &&
										<Dropdown.Item
											onClick={() => setShowModalRemoveTranscription(true)}
										>
											{t('transcription.details.remove')}
										</Dropdown.Item>}
									{CheckViewModePermission("canEditTranscription", viewMode) &&
										<Dropdown.Item
											onClick={() => onEditTranscription(false)}
											disabled={ creationMode !== 'web-form'}
										>
											{t('transcription.details.edit')}
										</Dropdown.Item>}
									{CheckViewModePermission("canPublishTranscription", viewMode) && hidden &&
										<Dropdown.Item
											onClick={() => creationMode === 'web-form' ? onEditTranscription(true) : setShowModalPublish(true) }
										>
											{t('transcription.details.public')}
										</Dropdown.Item>}
									{CheckViewModePermission("canUnpublishTranscription", viewMode) && !hidden &&
										<Dropdown.Item
											onClick={() => setShowModalUnpublish(true)}
										>
											{t('transcription.details.reversePublic')}
										</Dropdown.Item>}
								</Dropdown.Menu>
							</Dropdown>}

					</div>
				</div>

				<div className='block__header block__header--space-between block__underlined'>
					<h2 className={styles["transcription__metadata"]}>
						{t('transcription.details.metadata')}
					</h2>
					<div className={styles["transcription__desktopNav"]}>
						<div
							className='btn-group'
							role='group'
						>
							{CheckViewModePermission("canRemoveTranscription", viewMode) &&
								<Button
									variant='secondary'
									className={styles["transcription__link"]}
									onClick={() => setShowModalCreateDigitalTwin(true)}
									disabled={hidden}
								>
									{t('transcription.details.createDigitalTwin')}
								</Button>}

							{CheckViewModePermission("canRemoveTranscription", viewMode) &&
								<Button
									variant='secondary'
									className={styles["transcription__link"]}
									onClick={() => setShowModalRemoveTranscription(true)}
								>
									{t('transcription.details.remove')}
								</Button>}

							{CheckViewModePermission("canEditTranscription", viewMode) &&
								<Button
									variant='primary'
									onClick={() => onEditTranscription(false)}
									disabled={ creationMode !== 'web-form'}
								>
									{t('transcription.details.edit')}
								</Button>}

							{CheckViewModePermission("canPublishTranscription", viewMode) && hidden &&
								<Button
									variant='primary'
									onClick={() => creationMode === 'web-form' ? onEditTranscription(true) : setShowModalPublish(true) }
								>
									{t('transcription.details.public')}
								</Button>}

							{CheckViewModePermission("canUnpublishTranscription", viewMode) && !hidden &&
								<Button
									variant='primary'
									onClick={() => setShowModalUnpublish(true)}
								>
									{t('transcription.details.reversePublic')}
								</Button>}
						</div>
					</div>
				</div>
			</div>
		</>
	);
};

TranscriptionDetailsNav.propTypes = {
	viewMode: PropTypes.string,
	transcriptionId: PropTypes.number,
	title: PropTypes.string,
	hidden: PropTypes.bool,
	creationMode: PropTypes.string,
};

export default TranscriptionDetailsNav;

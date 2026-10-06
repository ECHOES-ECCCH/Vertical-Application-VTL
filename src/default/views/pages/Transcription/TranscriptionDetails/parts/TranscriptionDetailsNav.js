import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useDispatch } from 'react-redux';
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
	const [showModalUnpublish, setShowModalUnpublish] = useState(false);
	const [showModalPublish, setShowModalPublish] = useState(false);
	const [showModalRemoveTranscription, setShowModalRemoveTranscription] = useState(false);

	const onEditTranscription = (publish) => {
		const location = reverse(url.transcription.edit, { transcriptionId: parseInt(transcriptionId) });
		let query = "";
		if (publish) {
			query = "?publish=true";
		}
		history.push(location + query);
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

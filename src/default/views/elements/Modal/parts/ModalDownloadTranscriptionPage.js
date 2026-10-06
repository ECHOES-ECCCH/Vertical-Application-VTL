import React from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import { viewerActions } from "@/store/actions/viewer.actions";

import Button from '@Elements/Button/Button';

import styles from "@Elements/Modal/parts/Styles/ModalDownloadTranscriptionPage.module.sass";

const ModalDownloadTranscriptionPage = ({handleOnClose}) => {
	const { t } = useTranslation();

	const pageId = useSelector((state) => state.app.modalDownloadTranscriptionPage);

	const dispatch = useDispatch();

	const downloadFileFormat = format => {
		dispatch(viewerActions.getFileByFormat(pageId, format));
	};

	return (
		<>
			<Modal.Body>
				<p className={styles["modal-download--body"]} dangerouslySetInnerHTML={ { __html:t('modals.downloadTranscriptionPage.body') } } />
			</Modal.Body>
			<Modal.Footer>
				<div className={styles["modal-download--footer"]}>
					<Button
						variant='primary'
						onClick={ () => { downloadFileFormat("hocr"); } }
					>
						{t('modals.downloadTranscriptionPage.button1')}
					</Button>
					<Button
						variant='primary'
						onClick={ () => { downloadFileFormat("alto"); } }
					>
						{t('modals.downloadTranscriptionPage.button2')}
					</Button>
					<Button
						variant='primary'
						onClick={ () => { downloadFileFormat("plain-text"); } }
					>
						{t('modals.downloadTranscriptionPage.button3')}
					</Button>
					<Button
						variant='primary'
						onClick={ () => { downloadFileFormat("tei"); } }
					>
						{t('modals.downloadTranscriptionPage.button4')}
					</Button>
				</div>
			</Modal.Footer>
		</>
	);
};

ModalDownloadTranscriptionPage.propTypes = {
	handleOnClose: PropTypes.func,
};

export default ModalDownloadTranscriptionPage;
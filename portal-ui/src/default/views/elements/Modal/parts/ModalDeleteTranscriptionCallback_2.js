import React from "react";
import { useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import { appActions } from "@/store/actions/app.actions";

import Button from '@Elements/Button/Button';

const ModalDeleteTranscriptionCallback = ({ handleOnClose }) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	return (
		<>
			<Modal.Body>
				<p dangerouslySetInnerHTML={ { __html:t('modals.deleteTranscription.bodyHTML') } } />
			</Modal.Body>
			<Modal.Footer>
				<Button
					variant='primary'
					onClick={ () => {
						dispatch(appActions.setModalConfirmInfo("TRANSCRIPTION_DELETE_REJECTED"));
						handleOnClose();
					} }
					className='modal__button--cancel'
				>
					{t('form.cancel') }
				</Button>
				<Button
					variant='secondary'
					onClick={ () => { 
						dispatch(appActions.setModalConfirmInfo("TRANSCRIPTION_DELETE_CONFIRMED"));
						handleOnClose();
					} }
					className='modal__button--submit'
				>
					{t('modals.deleteTranscription.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalDeleteTranscriptionCallback.propTypes = {
	handleOnClose: PropTypes.func,
};

export default ModalDeleteTranscriptionCallback;
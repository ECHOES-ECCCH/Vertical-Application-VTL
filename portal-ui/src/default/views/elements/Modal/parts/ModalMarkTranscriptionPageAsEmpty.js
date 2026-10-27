import React from "react";
import { useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import api from "@/services/api";
import { appActions } from "@/store/actions/app.actions";

import Button from '@Elements/Button/Button';

const ModalMarkTranscriptionPageAsEmpty = ({handleOnClose, pageId,  handleOnComplete, handleOnError, handleOnCancel}) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	const markTranscriptionPageAsEmpty = () => {
		dispatch(appActions.setLoading(true));

		api.put(`/pages/${pageId}/empty?empty=true`)
			.then(() => {
				handleOnClose();
				dispatch(appActions.setLoading(false));
				handleOnComplete?.();
			})
			.catch(() => {
				handleOnClose();
				dispatch(appActions.setLoading(false));
				handleOnError?.();
			});
	};
	
	const onCancelClick = () => {
		handleOnClose();
		handleOnCancel?.();
	};

	return (
		<>
			<Modal.Body>
				<p dangerouslySetInnerHTML={ { __html:t('modals.markTranscriptionPageAsEmpty.body') } } />
			</Modal.Body>
			<Modal.Footer>
				<Button
					variant='secondary'
					onClick={ onCancelClick }
					className='modal__button--cancel'
				>
					{t('form.cancel') }
				</Button>
				<Button
					variant='primary'
					onClick={ () => { markTranscriptionPageAsEmpty(); } }
				>
					{t('modals.markTranscriptionPageAsEmpty.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalMarkTranscriptionPageAsEmpty.propTypes = {
	handleOnClose: PropTypes.func,
	pageId: PropTypes.number,
	handleOnComplete: PropTypes.func,
	handleOnError: PropTypes.func,
	handleOnCancel: PropTypes.func,
};

export default ModalMarkTranscriptionPageAsEmpty;

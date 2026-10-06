import React from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useHistory } from "react-router-dom";
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import api from "@/services/api";
import { appActions } from "@/store/actions/app.actions";

import Button from '@Elements/Button/Button';

const ModalDeleteTranscriptionPage = ({handleOnClose}) => {
	const { t } = useTranslation();

	const pageId = useSelector((state) => state.app.modalDeleteTranscriptionPage);
	const history = useHistory();

	const dispatch = useDispatch();

	const deleteTranscriptionPage = () => {
		dispatch(appActions.setLoading(true));

		api.delete(`/pages/${pageId}`)
			.then(() => {
				history.go(0);
				dispatch(appActions.setModalDeleteTranscriptionPage(null));
				dispatch(appActions.setLoading(false));
				handleOnClose();
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
				dispatch(appActions.setModalDeleteTranscriptionPage(null));
				handleOnClose();
			});
	};

	return (
		<>
			<Modal.Body>
				<p dangerouslySetInnerHTML={ { __html:t('modals.deleteTranscriptionPage.body') } } />
			</Modal.Body>
			<Modal.Footer>
				<Button
					variant='secondary'
					onClick={ handleOnClose }
					className='modal__button--cancel'
				>
					{t('form.cancel') }
				</Button>
				<Button
					variant='primary'
					onClick={ () => { deleteTranscriptionPage(); } }
				>
					{t('modals.deleteTranscriptionPage.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalDeleteTranscriptionPage.propTypes = {
	handleOnClose: PropTypes.func,
};

export default ModalDeleteTranscriptionPage;
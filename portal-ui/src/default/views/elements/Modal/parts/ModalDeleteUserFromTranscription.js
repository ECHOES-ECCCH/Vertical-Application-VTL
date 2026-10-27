import React from "react";
import { useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import Button from '@Elements/Button/Button';
import { appActions } from '@/store/actions/app.actions';

const ModalDeleteUserFromTranscription = ({ handleOnClose }) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();
	return (
		<>
			<Modal.Body>
				<p dangerouslySetInnerHTML={ { __html:t('modals.permissionsDeleteUser.bodyHTML') } } />
			</Modal.Body>
			<Modal.Footer>
				<Button
					variant='secondary'
					onClick={ () => {
						dispatch(appActions.setModalConfirmInfo("USER_DELETE_REJECTED"));
						handleOnClose();
					} }
					className='modal__button--cancel'
				>
					{t('form.cancel')}
				</Button>
				<Button
					variant='primary'
					onClick={ () => {
						dispatch(appActions.setModalConfirmInfo("USER_DELETE_CONFIRMED"));
						handleOnClose();
					} }
					className='modal__button--submit'
				>
					{t('modals.permissionsDeleteUser.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalDeleteUserFromTranscription.propTypes = {
	handleOnClose: PropTypes.func,
};

export default ModalDeleteUserFromTranscription;
import React from "react";
import { useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import { viewerActions } from "@/store/actions/viewer.actions";

import Button from '@Elements/Button/Button';

const ModalAcceptAllVerses = ({ handleOnClose, transcriptionId, pageId, layerId }) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	const acceptAllVerses = () => {
		dispatch(viewerActions.setAllVersesOnPageAndLayerStatus(transcriptionId, pageId, layerId, 'verified'));
	};

	return (
		<>
			<Modal.Body>
				<p dangerouslySetInnerHTML={ { __html:t('modals.acceptAllVerses.body') } } />
			</Modal.Body>
			
			<Modal.Footer>
				<Button
					variant='secondary'
					onClick={() => {
						handleOnClose();
					}}
					className='modal__button--cancel'
				>
					{t('form.cancel')}
				</Button>
				<Button
					variant='primary'
					type='submit'
					onClick={() => {
						acceptAllVerses();
						handleOnClose();
					}}
					className='modal__button--submit'
				>
					{t('modals.acceptAllVerses.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalAcceptAllVerses.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
	pageId: PropTypes.number,
	layerId: PropTypes.number,
};

export default ModalAcceptAllVerses;
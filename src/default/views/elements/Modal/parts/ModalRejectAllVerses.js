import React from "react";
import { useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import { viewerActions } from "@/store/actions/viewer.actions";

import Button from '@Elements/Button/Button';

const ModalRejectAllVerses = ({ handleOnClose, transcriptionId, pageId, layerId }) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	const rejectAllVerses = () => {
		dispatch(viewerActions.setAllVersesOnPageAndLayerStatus(transcriptionId, pageId, layerId, 'incorrect'));
	};

	return (
		<>
			<Modal.Body>
				<p dangerouslySetInnerHTML={ { __html:t('modals.rejectAllVerses.body') } } />
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
						rejectAllVerses();
						handleOnClose();
					}}
					className='modal__button--submit'
				>
					{t('modals.rejectAllVerses.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalRejectAllVerses.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
	pageId: PropTypes.number,
	layerId: PropTypes.number,
};

export default ModalRejectAllVerses;
import React from "react";
import { useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import api from "@/services/api";

import { appActions } from "@/store/actions/app.actions";
import { viewerActions } from "@/store/actions/viewer.actions";

import Button from '@Elements/Button/Button';

const ModalIndexesDelete = ({handleOnClose, verseId, wordId, indexId}) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	const handleDeleteClick = () => {
		dispatch(appActions.setLoading(true));
		api.delete(`/indexes/${indexId}/` + (wordId ? `words/${wordId}` : `verses/${verseId}`))
			.then(() => {
				dispatch(viewerActions.updateRemovedIndex(verseId, wordId, indexId));
				dispatch(viewerActions.allowUndoDisallowRedo());
				dispatch(appActions.setLoading(false));

				handleOnClose();
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
			});
	};

	return (
		<>
			<Modal.Body>
				<p dangerouslySetInnerHTML={ { __html:t('modals.listOfIndex.bodyHTML') } } />
			</Modal.Body>
			<Modal.Footer>
				<Button
					variant='secondary'
					onClick={ () => { 
						handleOnClose();
					} }
					className='modal__button--cancel'
				>
					{t('form.cancel') }
				</Button>
				<Button
					variant='primary'
					onClick={ () => handleDeleteClick() }
					className='modal__button--submit'
				>
					{t('modals.listOfIndex.deleteButton')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalIndexesDelete.propTypes = {
	handleOnClose: PropTypes.func,
	verseId: PropTypes.number,
	wordId: PropTypes.object,
	indexId: PropTypes.number,
};

export default ModalIndexesDelete;
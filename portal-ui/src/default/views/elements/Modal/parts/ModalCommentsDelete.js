import React from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import api from "@/services/api";

import { useShowModal } from '@Elements/Modal/ModalHooks';

import { appActions } from "@/store/actions/app.actions";
import { viewerActions } from "@/store/actions/viewer.actions";

import Button from '@Elements/Button/Button';

const ModalCommentsDelete = ({handleOnClose, verseId, transcriptionId, pageId}) => {
	const { t } = useTranslation();

	const commentId = useSelector((state) => state.app.modalCommentDeleteCommentId);
	const verses = useSelector((state) => state.viewer.data.verses);
	const commentsCount = parseInt(verses.find(x => x.id === verseId).comments - 1);

	const dispatch = useDispatch();

	const modalCommentsVerse = useShowModal({
		title:t('modals.commentsVerse.titleVerse'),
		type: 'viewer modal-comments',
		name: 'commentsVerse',
		componentPath: './parts/ModalCommentsVerse',
		componentProps: { verseId: parseInt(verseId), transcriptionId: transcriptionId, pageId: pageId  },
		withClose: false,
	});

	const deleteComment = () => {
		dispatch(appActions.setLoading(true));

		api.delete(`/comments/${commentId}`)
			.then(() => {
				dispatch(appActions.setLoading(false));
				dispatch(appActions.setModalCommentsDeleteCommentId(null));
				dispatch(viewerActions.updateVerseCommentsCount(verseId, commentsCount));
				handleOnClose();
				if (commentsCount) {
					modalCommentsVerse();
				}
			})
			.catch((er) => {
				console.error(er);
				dispatch(appActions.setLoading(false));
				dispatch(appActions.setModalCommentsDeleteCommentId(null));
				handleOnClose();
				modalCommentsVerse();
			});
	};

	return (
		<>
			<Modal.Body>
				<p dangerouslySetInnerHTML={ { __html:t('modals.commentsDelete.bodyHTML') } } />
			</Modal.Body>
			<Modal.Footer>
				<Button
					variant='secondary'
					onClick={ () => { 
						handleOnClose();
						modalCommentsVerse();
					} }
					className='modal__button--cancel'
				>
					{t('form.cancel') }
				</Button>
				<Button
					variant='primary'
					onClick={ () => deleteComment() }
					className='modal__button--submit'
				>
					{t('modals.commentsDelete.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalCommentsDelete.propTypes = {
	handleOnClose: PropTypes.func,
	verseId: PropTypes.number,
	transcriptionId: PropTypes.number,
	pageId: PropTypes.number,
};

export default ModalCommentsDelete;
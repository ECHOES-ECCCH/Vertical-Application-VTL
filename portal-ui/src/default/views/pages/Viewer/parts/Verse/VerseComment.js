import React from "react";
import PropTypes from "prop-types";
import { useSelector } from 'react-redux';

import { useShowModal } from '@Elements/Modal/ModalHooks';

import Button from "@Elements/Button/Button";

const VerseComment = ({ verse, transcriptionId, pageId }) => {
	const dict = useSelector((state) => state.app.dict);

	const modalCommentsVerse = useShowModal({
		title: dict.modals.commentsVerse.titleVerse,
		type: 'viewer modal-comments',
		name: 'commentsVerse',
		componentPath: './parts/ModalCommentsVerse',
		componentProps: { verseId: parseInt(verse.id), verified: verse.status === 'verified', transcriptionId: transcriptionId, pageId: pageId  },
		withClose: true,
	});

	return (
		<div className='verse__comments'>
			{ verse.comments > 0 && 
			<Button 
				className='verse__comments--button'
				variant='verseinfo'
				onClick={ () => modalCommentsVerse() }
			>
				<i className='icon-comment' />
				<span className='verse__comments--count'>{verse.comments}</span>
			</Button>}
			
		</div> 
	);
};

VerseComment.propTypes = {
	verse: PropTypes.object,
	transcriptionId: PropTypes.number,
	pageId: PropTypes.number,
};

export default VerseComment;
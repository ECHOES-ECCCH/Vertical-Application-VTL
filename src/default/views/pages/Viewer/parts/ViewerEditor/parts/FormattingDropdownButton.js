import React, { memo } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useParams } from "react-router";
import { Dropdown } from 'react-bootstrap';
import PropTypes from "prop-types";

import { useShowModal } from '@Elements/Modal/ModalHooks';
import { viewerActions } from '@/store/actions/viewer.actions';

const FormattingDropdownButton = ({ icon, iconImg, disabled, func, label, width }) => {
	const dispatch = useDispatch();
	const verses = useSelector((state) => state.viewer.data.verses);
	const verseActive = useSelector((state) => state.viewer.editor.verseActive);
	const verseGroupActive = useSelector((state) => state.viewer.editor.verseGroupActive);
	const wordActive = useSelector(state => state.viewer.editor.wordActive);
	const wordGroupActive = useSelector(state => state.viewer.editor.wordGroupActive);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const { t } = useTranslation();

	const { transcriptionId, pageId } = useParams();

	const modalDeleteVerse = useShowModal({
		title:t('modals.deleteVerse.title'),
		type: 'viewer',
		name: 'deleteVerse',
		componentPath: './parts/ModalDeleteVerse',
		componentProps: { transcriptionId: parseInt(transcriptionId) },
	});

	const modalDeleteSelectedVerses = useShowModal({
		title:t('modals.deleteSelectedVerses.title'),
		type: 'viewer',
		name: 'deleteSelectedVerses',
		componentPath: './parts/ModalDeleteSelectedVerses',
		componentProps: { transcriptionId: parseInt(transcriptionId) },
	});

	const modalDeleteWord = useShowModal({
		title:t('modals.deleteWord.title'),
		type: 'viewer',
		name: 'deleteWord',
		componentPath: './parts/ModalDeleteWord',
		componentProps: { transcriptionId: parseInt(transcriptionId) },
	});

	const modalDeleteSelectedWords = useShowModal({
		title:t('modals.deleteSelectedWords.title'),
		type: 'viewer',
		name: 'deleteSelectedWords',
		componentPath: './parts/ModalDeleteSelectedWords',
		componentProps: { transcriptionId: parseInt(transcriptionId) },
	});

	const modalDeleteVerses = useShowModal({
		title:t('modals.deleteVerses.title'),
		type: 'viewer',
		name: 'deleteVerses',
		componentPath: './parts/ModalDeleteVerses',
		componentProps: { transcriptionId: parseInt(transcriptionId) },
	});

	return (
		<Dropdown.Item
			onClick={ () => {
				switch (func) {
					case 'verse-delete':
						if (verseActive) {
							modalDeleteVerse();
						} else if (verseGroupActive.firstVerseNo !== null) {
							modalDeleteSelectedVerses();
						}
						break;
					case 'word-delete':
						if (wordActive.wordId !== null) {
							modalDeleteWord();
						} else if (wordGroupActive.firstWordNo !== null) {
							modalDeleteSelectedWords();
						}
						break;
					case 'verses-delete':
						modalDeleteVerses();
						break;
					case 'word-add':
						dispatch(viewerActions.markWord(
							wordGroupActive.activeWordsIds.length === 0 ? [wordActive.wordId] : wordGroupActive.activeWordsIds, 
							'ADD',
							'',
							transcriptionId, 
							selectedLayer,
						));
						break;
					case 'word-add-above':
						dispatch(viewerActions.markWord(
							wordGroupActive.activeWordsIds.length === 0 ? [wordActive.wordId] : wordGroupActive.activeWordsIds, 
							'ADD',
							'above',
							transcriptionId, 
							selectedLayer,
						));
						break;
					case 'word-add-below':
						dispatch(viewerActions.markWord(
							wordGroupActive.activeWordsIds.length === 0 ? [wordActive.wordId] : wordGroupActive.activeWordsIds, 
							'ADD',
							'below',
							transcriptionId, 
							selectedLayer,
						));
						break;
					case 'word-add-margin':
						dispatch(viewerActions.markWord(
							wordGroupActive.activeWordsIds.length === 0 ? [wordActive.wordId] : wordGroupActive.activeWordsIds, 
							'ADD',
							'margin',
							transcriptionId, 
							selectedLayer,
						));
						break;
					default:
						if (verseActive) {
							let verseActiveNo = null;
							verses.forEach(verse => { if(verse.id == verseActive) { verseActiveNo = verse.verseNo; } });
							if (verseActiveNo) {
								dispatch(viewerActions.createNewBlock(transcriptionId, pageId, selectedLayer, func, verseActiveNo, verseActiveNo));
							}
						} else if (verseGroupActive.firstVerseNo !== null && verseGroupActive.lastVerseNo !== null) {
							dispatch(viewerActions.createNewBlock(transcriptionId, pageId, selectedLayer, func, verseGroupActive.firstVerseNo, verseGroupActive.lastVerseNo));
						}
				}
			} }
			disabled={ disabled }
		>
			{icon && 
			<>
				<span className='d-none'>{icon}</span>
				<span><i className={ `icon-${icon}` } /></span> {label}
			</>}
			{iconImg && 
			<>
				<span style={{ width: width }}><img src={iconImg} /></span> {label}
			</>}
		</Dropdown.Item>
	);
};

FormattingDropdownButton.propTypes = {
	label: PropTypes.string,
	iconImg: PropTypes.string,
	disabled: PropTypes.bool,
	func: PropTypes.string,
	icon: PropTypes.string,
	width: PropTypes.number,
};

export default memo(FormattingDropdownButton);
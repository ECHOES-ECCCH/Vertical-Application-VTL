import React, { useRef, useEffect, useCallback } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import { viewerActions } from "@/store/actions/viewer.actions";

import Button from '@Elements/Button/Button';

const ModalDeleteSelectedWords = ({handleOnClose, transcriptionId}) => {
	const { t } = useTranslation();

	const allVerses = useSelector(state => state.viewer.data.verses);
	const wordGroupActive = useSelector((state) => state.viewer.editor.wordGroupActive);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const dispatch = useDispatch();

	const getNextWordToSelect = useCallback(() => {
		let selectedVerse;
		for (let i = 0; i < allVerses.length; ++i) {
			if (allVerses[i].id == wordGroupActive.verseId) {
				selectedVerse = allVerses[i];
				break;
			}
		}

		if (selectedVerse === undefined || selectedVerse.words.length == wordGroupActive.activeWordsIds.length) {
			return { verseId: null, wordId: null };
		}

		let firstWordIndex;
		let lastWordIndex;
		for (let i = 0; i < selectedVerse.words.length; ++i) {
			if (selectedVerse.words[i].ordering == wordGroupActive.firstWordNo) {
				firstWordIndex = i;
			}
			if (selectedVerse.words[i].ordering == wordGroupActive.lastWordNo) {
				lastWordIndex = i;
			}
		}

		let firstWordInOrderIndex;
		let lastWordInOrderIndex;

		if (firstWordIndex < lastWordIndex) {
			firstWordInOrderIndex = firstWordIndex;
			lastWordInOrderIndex = lastWordIndex;
		} else {
			firstWordInOrderIndex = lastWordIndex;
			lastWordInOrderIndex = firstWordIndex;
		}

		let nextWordId;
		if (lastWordInOrderIndex == selectedVerse.words.length - 1 && firstWordInOrderIndex > 0) {
			nextWordId = selectedVerse.words[firstWordInOrderIndex - 1]?.id;
		} else {
			nextWordId = selectedVerse.words[lastWordInOrderIndex + 1]?.id;
		}

		if (nextWordId) {
			return { verseId: selectedVerse.id, wordId: nextWordId };
		}

		return { verseId: null, wordId: null };
	}, [wordGroupActive, allVerses]);

	const deleteWords = () => {
		dispatch(viewerActions.deleteWords(wordGroupActive.verseId, wordGroupActive.activeWordsIds, getNextWordToSelect(), selectedLayer));
	};

	const btnElement = useRef(null);

	useEffect(() => {
		if (btnElement.current) {
			btnElement.current.focus();
		}
	}, []);

	return (
		<>
			<Modal.Body>
				<p dangerouslySetInnerHTML={ { __html:t('modals.deleteSelectedWords.bodyHTML') } } />
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
					onClick={ () => deleteWords() }
					ref={ btnElement }
					className='modal__button--submit'
				>
					{t('modals.deleteSelectedWords.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalDeleteSelectedWords.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
};

export default ModalDeleteSelectedWords;
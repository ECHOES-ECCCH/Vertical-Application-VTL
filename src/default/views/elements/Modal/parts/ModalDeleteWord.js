import React, { useRef, useEffect, useCallback } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import { viewerActions } from "@/store/actions/viewer.actions";

import Button from '@Elements/Button/Button';

const ModalDeleteWord = ({handleOnClose, transcriptionId}) => {
	const { t } = useTranslation();

	const allVerses = useSelector(state => state.viewer.data.verses);
	const wordActive = useSelector(state => state.viewer.editor.wordActive);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const dispatch = useDispatch();

	const getNextWordToSelect = useCallback(() => {
		let selectedVerse;
		for (let i = 0; i < allVerses.length; ++i) {
			if (allVerses[i].id == wordActive.verseId) {
				selectedVerse = allVerses[i];
				break;
			}
		}

		if (selectedVerse === undefined || selectedVerse.words.length <= 1) {
			return { verseId: null, wordId: null };
		}

		let wordIndex;
		for (let i = 0; i < selectedVerse.words.length; ++i) {
			if (selectedVerse.words[i].id == wordActive.wordId) {
				wordIndex = i;
				break;
			}
		}

		let nextWordId;
		if (wordIndex == selectedVerse.words.length - 1) {
			nextWordId = selectedVerse.words[wordIndex - 1]?.id;
		} else {
			nextWordId = selectedVerse.words[wordIndex + 1]?.id;
		}

		if (nextWordId) {
			return { verseId: selectedVerse.id, wordId: nextWordId };
		}

		return { verseId: null, wordId: null };
	}, [wordActive, allVerses]);

	const deleteWord = () => {
		dispatch(viewerActions.deleteWord(wordActive.verseId, wordActive.wordId, getNextWordToSelect(), selectedLayer));
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
				<p dangerouslySetInnerHTML={ { __html:t('modals.deleteWord.bodyHTML') } } />
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
					onClick={ () => deleteWord() }
					ref={ btnElement }
					className='modal__button--submit'
				>
					{t('modals.deleteWord.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalDeleteWord.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
};

export default ModalDeleteWord;
import React from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import { viewerActions } from '@/store/actions/viewer.actions';
import useOCRModelSelection from "@/utils/useOCRModelSelection";

import Button from '@Elements/Button/Button';
import OCRModelSelector from "@Elements/OCR/OCRModelSelector";

const ModalAutoTranscribeVerse = ({ handleOnClose, transcriptionId }) => {
	const { t } = useTranslation();
	const dispatch = useDispatch();

	const verseActive = useSelector(state => state.viewer.editor.verseActive);
	const verseGroupActive = useSelector(state => state.viewer.editor.verseGroupActive);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);

	const selection = useOCRModelSelection(transcriptionId);

	const handleAutoTranscribeVerse = () => {
		let verses = [];
		if (verseActive) {
			verses = [verseActive];
		} else if (verseGroupActive.firstVerseNo !== null && verseGroupActive.lastVerseNo !== null) {
			verses = verseGroupActive.activeVersesIds;
		}
		const models = selection.selectedModels.join('+');
		dispatch(viewerActions.submitAutoTranscribeVerses(transcriptionId, selectedLayer, verses, models));
	};

	return (
		<>
			<Modal.Body>
				<p dangerouslySetInnerHTML={{ __html: t('modals.autoTranscribeVerse.bodyHTML') }} />
				<OCRModelSelector selection={selection} dropdownClassName='dropdown-new dropdown-viewer' />
			</Modal.Body>
			<Modal.Footer>
				<Button variant='secondary' onClick={handleOnClose} className='modal__button--cancel'>
					{t('form.cancel')}
				</Button>
				<Button
					variant='primary'
					onClick={handleAutoTranscribeVerse}
					className='modal__button--submit'
					disabled={!selection.selectedModels.length}
				>
					{t('modals.autoTranscribeVerse.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalAutoTranscribeVerse.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
};

export default ModalAutoTranscribeVerse;
import React, { useRef, useEffect, useCallback } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import { viewerActions } from "@/store/actions/viewer.actions";

import Button from '@Elements/Button/Button';

const ModalDeleteVerse = ({handleOnClose, transcriptionId}) => {
	const { t } = useTranslation();

	const allVerses = useSelector(state => state.viewer.data.verses);
	const verseActive = useSelector((state) => state.viewer.editor.verseActive);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const dispatch = useDispatch();

	const getNextVerseToSelect = useCallback(() => {
		let numberOfVersesInLayer = 0;
		let currentVerseNo;
		for (let i = 0; i < allVerses.length; ++i) {
			if (allVerses[i].pageLayer.layerId == selectedLayer) {
				numberOfVersesInLayer++;
				if(allVerses[i].id == verseActive) {
					currentVerseNo = allVerses[i].verseNo;
				}
			}
		}

		if (numberOfVersesInLayer == 1 || currentVerseNo === undefined) {
			return null;
		}

		let nextVerseNo;
		if (currentVerseNo == numberOfVersesInLayer) {
			nextVerseNo = currentVerseNo - 1;
		} else {
			nextVerseNo = currentVerseNo + 1;
		}

		let nextVerseId;
		for (let i = 0; i < allVerses.length; ++i) {
			if (allVerses[i].pageLayer.layerId == selectedLayer && allVerses[i].verseNo == nextVerseNo) {
				nextVerseId = allVerses[i].id;
				break;
			}
		}

		if (nextVerseId) {
			return nextVerseId;
		}

		return null;
	}, [verseActive, allVerses]);

	const deleteVerse = () => {
		dispatch(viewerActions.deleteVerse(verseActive, getNextVerseToSelect(), selectedLayer));
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
				<p dangerouslySetInnerHTML={ { __html:t('modals.deleteVerse.bodyHTML') } } />
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
					onClick={ () => deleteVerse() }
					ref={ btnElement }
					className='modal__button--submit'
				>
					{t('modals.deleteVerse.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalDeleteVerse.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
};

export default ModalDeleteVerse;
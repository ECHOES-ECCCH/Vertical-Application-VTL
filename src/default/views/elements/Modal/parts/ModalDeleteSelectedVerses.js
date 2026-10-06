import React, { useRef, useEffect, useCallback } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import { viewerActions } from "@/store/actions/viewer.actions";

import Button from '@Elements/Button/Button';

const ModalDeleteSelectedVerses = ({handleOnClose, transcriptionId}) => {
	const { t } = useTranslation();

	const allVerses = useSelector(state => state.viewer.data.verses);
	const verseGroupActive = useSelector((state) => state.viewer.editor.verseGroupActive);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const dispatch = useDispatch();

	const getNextVerseToSelect = useCallback(() => {
		let numberOfVersesInLayer = 0;
		for (let i = 0; i < allVerses.length; ++i) {
			if (allVerses[i].pageLayer.layerId == selectedLayer) {
				numberOfVersesInLayer++;
			}
		}

		if (numberOfVersesInLayer == verseGroupActive.activeVersesIds.length) {
			return null;
		}

		let firstVerseInOrderNo;
		let lastVerseInOrderNo;
		if (verseGroupActive.firstVerseNo < verseGroupActive.lastVerseNo) {
			firstVerseInOrderNo = verseGroupActive.firstVerseNo;
			lastVerseInOrderNo = verseGroupActive.lastVerseNo;
		} else {
			firstVerseInOrderNo = verseGroupActive.lastVerseNo;
			lastVerseInOrderNo = verseGroupActive.firstVerseNo;
		}

		let nextVerseNo;
		if (lastVerseInOrderNo == numberOfVersesInLayer && firstVerseInOrderNo > 1) {
			nextVerseNo = firstVerseInOrderNo - 1;
		} else {
			nextVerseNo = lastVerseInOrderNo + 1;
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
	}, [verseGroupActive, allVerses]);

	const deleteVerses = () => {
		dispatch(viewerActions.deleteVerses(verseGroupActive.activeVersesIds, getNextVerseToSelect(), selectedLayer));
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
				<p dangerouslySetInnerHTML={ { __html:t('modals.deleteSelectedVerses.bodyHTML') } } />
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
					onClick={ () => deleteVerses() }
					ref={ btnElement }
					className='modal__button--submit'
				>
					{t('modals.deleteSelectedVerses.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalDeleteSelectedVerses.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
};

export default ModalDeleteSelectedVerses;
import React, { memo, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useTranslation } from 'react-i18next';
import { useParams } from "react-router";
import PropTypes from "prop-types";

import { useShowModal } from "@Elements/Modal/ModalHooks";
import { viewerActions } from "@/store/actions/viewer.actions";

import ModalWrapperChildren from "@Elements/Modal/ModalWrapperChildren";
import ModalAddSubst from '@Elements/Modal/parts/ModalAddSubst';

import Button from "@Elements/Button/Button";

const FormattingButton = ({ icon, disabled, func, tooltip, visible = true, indexType }) => {
	const dispatch = useDispatch();
	const verses = useSelector(state => state.viewer.data.verses);
	const verseActive = useSelector(state => state.viewer.editor.verseActive);
	const wordActive = useSelector(state => state.viewer.editor.wordActive);
	const verseEdit = useSelector(state => state.viewer.editor.verseEdit);
	const verseGroupActive = useSelector((state) => state.viewer.editor.verseGroupActive);
	const wordGroupActive = useSelector(state => state.viewer.editor.wordGroupActive);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const { t } = useTranslation();
	const dictToUse = indexType === 'person' ?t('modals.setAsPerson',{ returnObjects: true }) : (indexType === 'place' ?t('modals.setAsPlace',{ returnObjects: true }) :t('modals.setAsTerm',{ returnObjects: true }));

	const { transcriptionId, pageId } = useParams();

	const [ isAddingSubst, setIsAddingSubst ] = useState(false);

	const modalAutoTranscribeVerse = useShowModal({
		title:t('modals.autoTranscribeVerse.title'),
		type: "viewer",
		name: "autoTranscribeVerse",
		componentPath: "./parts/ModalAutoTranscribeVerse",
		componentProps: { transcriptionId: parseInt(transcriptionId), pageId: parseInt(pageId) },
	});

	const modalCommentsVerse = useShowModal({
		title:t('modals.commentsVerse.titleAdd'),
		type: "viewer modal-comments",
		name: "commentsVerse",
		componentPath: "./parts/ModalCommentsVerse",
		componentProps: { verseId: parseInt(verseActive), addBox: true, transcriptionId: transcriptionId, pageId: pageId },
		withClose: true,
	});

	const modalSetVerseAsIndex = useShowModal({
		title: dictToUse.titleAdd,
		type: "viewer modal-index",
		name: "PersonVerse",
		componentPath: "./parts/ModalSetVerseAsIndex",
		componentProps: {
			transcriptionId: parseInt(transcriptionId),
			verseId: parseInt(wordActive?.verseId ? wordActive.verseId : verseActive),
			wordId: parseInt(wordActive?.wordId),
			addBox: true,
			indexType,
		},
	});

	let disabledStatus;
	if (disabled === undefined) {
		disabledStatus = !verseActive || verseEdit;
	} else {
		disabledStatus = disabled;
	}

	return (
		visible &&
		<div
			data-tooltip-id='custom-tooltip' data-tooltip-content={ tooltip }
			data-place='right'
		>
			<ModalWrapperChildren
				modalVisible={isAddingSubst}
				setModalVisible={setIsAddingSubst}
				title={t("modals.addSubst.title")}
				type='viewer'
			>
				<ModalAddSubst
					handleOnClose={() => setIsAddingSubst(false)}
					wordIds={wordGroupActive.activeWordsIds.length === 0 ? [wordActive.wordId] : wordGroupActive.activeWordsIds}
					transcriptionId={parseInt(transcriptionId)}
					layerId={+selectedLayer}
				/>
			</ModalWrapperChildren>
			<Button
				variant='viewer-button'
				className='viewer__button--menu'
				onClick={ () => {
					switch (func) {
						case "verse-comment":
							modalCommentsVerse();
							break;
						case "recognize-content":
							modalAutoTranscribeVerse();
							break;
						case 'verse-set-as-index':
							modalSetVerseAsIndex();
							break;
						case 'clear-verses': 
							if (verseActive) {
								dispatch(viewerActions.clearVerses(selectedLayer, [verseActive]));
							} else if(verseGroupActive.firstVerseNo !== null && verseGroupActive.lastVerseNo !== null) {
								dispatch(viewerActions.clearVerses(selectedLayer, verseGroupActive.activeVersesIds));
							}
							break;
						case 'move-up':
						case 'move-down':
							if (verseActive) {
								dispatch(viewerActions.submitVerseFormatting(verseActive, func, selectedLayer));
							} else if(verseGroupActive.firstVerseNo !== null && verseGroupActive.lastVerseNo !== null) {
								dispatch(viewerActions.moveVerses(verseGroupActive.activeVersesIds, func, selectedLayer));
							}
							break;
						case 'move-up-10':
							if (verseActive) {
								dispatch(viewerActions.submitVerseFormatting(verseActive, `move-up?times=10`, selectedLayer));
							} else if(verseGroupActive.firstVerseNo !== null && verseGroupActive.lastVerseNo !== null) {
								dispatch(viewerActions.moveVerses(verseGroupActive.activeVersesIds, `move-up?times=10`, selectedLayer));
							}
							break;
						case 'move-down-10':
							if (verseActive) {
								dispatch(viewerActions.submitVerseFormatting(verseActive, `move-down?times=10`, selectedLayer));
							} else if(verseGroupActive.firstVerseNo !== null && verseGroupActive.lastVerseNo !== null) {
								dispatch(viewerActions.moveVerses(verseGroupActive.activeVersesIds, `move-down?times=10`, selectedLayer));
							}
							break;
						case 'word-del':
							dispatch(viewerActions.markWord(
								wordGroupActive.activeWordsIds.length === 0 ? [wordActive.wordId] : wordGroupActive.activeWordsIds, 
								'DEL',
								null,
								transcriptionId, 
								selectedLayer,
							));
							break;
						case 'gap':
							dispatch(viewerActions.markWord(
								wordGroupActive.activeWordsIds.length === 0 ? [wordActive.wordId] : wordGroupActive.activeWordsIds, 
								'GAP',
								null,
								transcriptionId, 
								selectedLayer,
							));
							break;
						case 'word-subst':
							setIsAddingSubst(true);
							break;
						default:
							if (verseActive) {
								let verseActiveNo = null;
								verses.forEach(versei => { if(versei.id == verseActive) { verseActiveNo = versei.verseNo; } });
								if (verseActiveNo) {
									dispatch(viewerActions.createNewBlock(transcriptionId, pageId, selectedLayer, func, verseActiveNo, verseActiveNo));
								}
							} else if (verseGroupActive.firstVerseNo !== null && verseGroupActive.lastVerseNo !== null) {
								dispatch(viewerActions.createNewBlock(transcriptionId, pageId, selectedLayer, func, verseGroupActive.firstVerseNo, verseGroupActive.lastVerseNo));
							}
					}
				} }
				disabled={ disabledStatus }
				data-tooltip-id='custom-tooltip' data-tooltip-content={ tooltip }
			>
				<span className='d-none'>{icon}</span>
				<i className={ `icon-${icon}` } />
			</Button>
		</div>
	);
};

FormattingButton.propTypes = {
	tooltip: PropTypes.string,
	disabled: PropTypes.bool,
	func: PropTypes.string,
	icon: PropTypes.string,
	visible: PropTypes.bool,
	indexType: PropTypes.string,
};

export default memo(FormattingButton);

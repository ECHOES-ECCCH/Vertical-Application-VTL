import React, { useState, useEffect, useCallback } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { useParams } from "react-router";
import PropTypes from "prop-types";
import { Editor, EditorState, ContentState, SelectionState, RichUtils, Modifier, convertFromHTML } from "draft-js";
import { convertToHTML } from 'draft-convert';

import { viewerActions } from '@/store/actions/viewer.actions';

import Button from "@Elements/Button/Button";

import { useShowModal } from '@Elements/Modal/ModalHooks';

const VerseActive = ({ verse, toggleVerseEdit, annotation }) => {
	const { t } = useTranslation();

	const mobilePanels = useSelector((state) => state.app.viewer.mobilePanels);

	const dispatch = useDispatch();
	const { transcriptionId, pageId } = useParams();
	const modalPaste = useShowModal({
		title: t('modals.pastedTooLongText.title'),
		bodyHTML: t('modals.pastedTooLongText.body'),
		type: 'viewer',
		name: 'pastedTooLongText',
		withFooter: true,
		actionButton: t('modals.pastedTooLongText.button'),
	});

	const keyEscape = useCallback((event) => {
		if (event.keyCode === 27) {
			toggleVerseEdit();
		}
	}, []);

	const defaultValues = {
		verseContent: (verse.content ? verse.content : ""),
	};

	const { register, handleSubmit, setValue, getValues } = useForm({ defaultValues: defaultValues });

	const tempEditorState = convertFromHTML(verse.content === null ? "" : verse.content);
	const [editorState, setEditorState] = useState(
		EditorState.createWithContent(ContentState.createFromBlockArray(
			tempEditorState.contentBlocks,
			tempEditorState.entityMap,
		)),
	);

	const toggleStyle = useSelector((state) => state.viewer.editor.styleActive);
	const insertCharacter = useSelector((state) => state.viewer.editor.addChar);
	const layersData = useSelector((state) => state.viewer.transcriptionLayersData);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);

	useEffect(() => {
		setEditorState(moveSelectionToEnd(editorState));
		document.addEventListener("keydown", keyEscape);
		return () => {
			document.removeEventListener("keydown", keyEscape);
		};
	}, []);

	useEffect(() => {
		const contentDraftToHtml = convertToHTML({
			styleToHTML: (style) => {
				if (style === 'STRIKETHROUGH') {
					return <strike />;
				}
			},
		})(editorState.getCurrentContent());
		setValue('verseContent', contentDraftToHtml);
		dispatch(viewerActions.setStyleCurrent(editorState.getCurrentInlineStyle()));
	}, [editorState]);

	useEffect(() => {
		if (toggleStyle !== '') {
			setEditorState(RichUtils.toggleInlineStyle(editorState, toggleStyle));
			dispatch(viewerActions.setStyleActive(''));
		}
	}, [toggleStyle]);

	useEffect(() => {
		if (insertCharacter !== '') {
			setEditorState(EditorState.push(editorState, Modifier.replaceText(
				editorState.getCurrentContent(),
				editorState.getSelection(),
				insertCharacter,
			)));
			dispatch(viewerActions.setInsertCharacter(''));
		}
	}, [insertCharacter]);

	const moveSelectionToEnd = useCallback((_editorState) => {
		const content = _editorState.getCurrentContent();
		const blockMap = content.getBlockMap();

		const key = blockMap.last().getKey();
		const length = blockMap.last().getLength();

		const selection = new SelectionState({
			anchorKey: key,
			anchorOffset: length,
			focusKey: key,
			focusOffset: length,
		});
		return EditorState.forceSelection(_editorState, selection);
	}, []);

	const onPastedText = text => {
		if (text.length > 4000) {
			modalPaste();
		}
		let tx = text.replace(/(-\n | - \n)/g, '').replace(/\n/g, ' ').substring(0, 4000);
		setEditorState(EditorState.push(editorState, Modifier.replaceText(
			editorState.getCurrentContent(),
			editorState.getSelection(),
			tx,
		)));
		return 'handled';
	};
	const onEnterKey = () => {
		onFieldAccept(getValues().verseContent);
		return 'handled';
	};
	const onSubmit = data => {
		onFieldAccept(data.verseContent);
	};

	const onFieldAccept = text => {
		const noParagraphText = text.replace('<p>', '').replace('</p>', '');
		if (mobilePanels === 'student') {
			dispatch(viewerActions.submitVerseStudentContent(verse.id, noParagraphText));
		} else {
			dispatch(viewerActions.submitVerseContent(verse.id, noParagraphText, transcriptionId, pageId, selectedLayer));
		}
	};

	let layerNumber = layersData.idToNumber.get(verse.layerId ? `${verse.layerId}` : verse.pageLayer.layerId);

	return (
		<div className={`${annotation ? "verse__block-annotation verse__block-annotation--active" : "verse__block verse__block--active" }`}>
			<div className='verse__num'>
				{verse.verseNo}
				<sub>[{layerNumber}]</sub>
			</div>

			<form
				className='verse__editor'
				onSubmit={handleSubmit(onSubmit)}
			>
				<div className='verse__editor--container'>
					<input type='hidden' {...register('verseContent')} />
					<div
						id='edit'
						className='d-none'
					/>
					<Editor
						editorState={editorState}
						onChange={setEditorState}
						handlePastedText={onPastedText}
						handleReturn={onEnterKey}
						ariaDescribedBy='edit'
					/>
				</div>
				<div className='verse__editor--buttons'>
					<Button
						variant='third verse'
						onClick={() => toggleVerseEdit()}
					>
						<span className='d-none'>Close</span>
						<i className='icon-close' />
					</Button>
					<Button
						className={ annotation ? 'accept-change-annotation' : 'accept-change' }
						variant={ annotation ? 'primary verse verse-annotation' : 'primary verse' }
						type='submit'
					>
						<span className='d-none'>Accept</span>
						<i className='icon-accept' />
					</Button>
				</div>
			</form>
		</div>
	);
};

VerseActive.propTypes = {
	verse: PropTypes.object,
	toggleVerseEdit: PropTypes.func,
	annotation: PropTypes.bool,
};

export default VerseActive;
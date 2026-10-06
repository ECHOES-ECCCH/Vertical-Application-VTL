import React, { useCallback, useEffect, useState } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useForm } from 'react-hook-form';
import { useParams } from "react-router";
import PropTypes from "prop-types";
import { Editor, EditorState, ContentState, SelectionState, RichUtils, Modifier, convertFromHTML } from "draft-js";
import { convertToHTML } from 'draft-convert';

import { viewerActions } from '@/store/actions/viewer.actions';

import Button from "@Elements/Button/Button";

const WordActive = ({ verseId, word, toggleWordEdit }) => {
	const dispatch = useDispatch();
	const { transcriptionId } = useParams();

	const keyEscape = useCallback((event) => {
		if (event.keyCode === 27) {
			toggleWordEdit();
		}
	}, []);

	const defaultValues = {
		verseContent: (word.content ? word.content : ""),
	};

	const { register, handleSubmit, setValue, getValues } = useForm({ defaultValues: defaultValues });

	const tempEditorState = convertFromHTML(word.content === null ? "" : word.content);
	const [editorState, setEditorState] = useState(
		EditorState.createWithContent(ContentState.createFromBlockArray(
			tempEditorState.contentBlocks,
			tempEditorState.entityMap,
		)),
	);

	const toggleStyle = useSelector((state) => state.viewer.editor.styleActive);
	const insertCharacter = useSelector((state) => state.viewer.editor.addChar);
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
		dispatch(viewerActions.submitWordContent(transcriptionId, selectedLayer, verseId, word.id, noParagraphText));
		//toggleWordEdit();
	};

	return (
		<div className='verse-word__block verse-word__block--active'>
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
						onClick={() => toggleWordEdit()}
					>
						<span className='d-none'>Close</span>
						<i className='icon-close' />
					</Button>
					<Button
						className='accept-change'
						variant='primary verse'
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

WordActive.propTypes = {
	verseId: PropTypes.number,
	word: PropTypes.object,
	toggleWordEdit: PropTypes.func,
};

export default WordActive;
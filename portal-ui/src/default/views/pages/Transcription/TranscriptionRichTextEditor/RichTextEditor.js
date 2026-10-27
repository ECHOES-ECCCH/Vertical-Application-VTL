import React, {useState} from 'react';
import { useTranslation } from 'react-i18next';
import { EditorState, ContentState, convertFromHTML } from 'draft-js';
import { convertToHTML } from 'draft-convert';
import { Editor } from 'react-draft-wysiwyg';
import PropTypes from "prop-types";

import 'react-draft-wysiwyg/dist/react-draft-wysiwyg.css';

const RichTextEditor = ({elementValue, setElementValue, readOnly, disabled}) => {
	const [isToolbarHidden, setIsToolbarHidden] = useState(true);
	const { t } = useTranslation();

	const RICHTEXT_TOOLBAR_SETTINGS = {
		options: ['inline', 'list', 'remove'],
		inline: {
			// delete  italic
			options: ['bold', 'underline','monospace'],
			bold: { title:t('transcription.metadata.richText.inline.bold') },
			underline: { title:t('transcription.metadata.richText.inline.underline') },
			monospace: { title:t('transcription.metadata.richText.inline.monospace') },
		},
		list:{
			unordered: { title:t('transcription.metadata.richText.list.unordered') },
			ordered: { title:t('transcription.metadata.richText.list.ordered') },
			indent: { title:t('transcription.metadata.richText.list.indent') },
			outdent: { title:t('transcription.metadata.richText.list.outdent') },
		},
		remove: { title:t('transcription.metadata.richText.remove') },
	};

	const tempElementState = convertFromHTML(elementValue === null ? "" : elementValue);
	const convertedElementState = ContentState.createFromBlockArray(
		tempElementState.contentBlocks,
		tempElementState.entityMap,
	);

	const [elementState, setElementState] = useState(
		() => EditorState.createWithContent(convertedElementState),
	);
	const handleEditorChange = (state) => {
		setElementState(state);
		setElementValue(convertToHTML(state.getCurrentContent()));
	};
    
	return (
		<Editor
			editorClassName={`transcription-new__form--richText-editor ${readOnly ? 'transcription-new__form--richText-editor-readOnly' : isToolbarHidden && 'transcription-new__form--richText-editor-tb-hidden'} ${disabled && 'transcription-new__form--richText-editor-disabled'}` }
			toolbarClassName={`${readOnly || `transcription-new__form--richText-tb ${isToolbarHidden && 'transcription-new__form--richText-tb-hidden'}`}`}
			editorState={ elementState }
			onEditorStateChange={readOnly ? null : handleEditorChange}
			toolbar={ RICHTEXT_TOOLBAR_SETTINGS }
			readOnly={readOnly || disabled}
			toolbarHidden={readOnly}
			handlePastedText={(text) => false }
			onFocus={() => setIsToolbarHidden(false)}
			onBlur={() => setIsToolbarHidden(true)}
			stripPastedStyles
		/>

	);
};

RichTextEditor.propTypes = {
	elementValue: PropTypes.string, 
	setElementValue: PropTypes.func, 
	readOnly: PropTypes.bool, 
	disabled: PropTypes.bool,
};

export default RichTextEditor;
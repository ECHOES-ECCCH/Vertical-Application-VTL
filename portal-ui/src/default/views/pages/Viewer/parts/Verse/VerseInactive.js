/* eslint-disable no-console */
import React, { useState, useEffect, memo, useRef } from "react";
import { useParams } from "react-router";
import PropTypes from "prop-types";
import { useDebounce } from 'use-debounce';
import { useSelector } from 'react-redux';

import Button from "@Elements/Button/Button";

import IsVerseOnScreen from "@Pages/Viewer/parts/Verse/IsVerseOnScreen";

const VerseInactive = ({ showContent, verse, toggleVerseEdit, toggleVerseActive, annotation }) => {
	const { pageId } = useParams();
	const isMobile = useSelector((state) => state.viewer.isMobile);
	const viewMode = useSelector((state) => state.viewer.data.viewMode);
	const verseActive = useSelector((state) => state.viewer.editor.verseActive);
	const verseGroupActive = useSelector((state) => state.viewer.editor.verseGroupActive);
	const verseEdit = useSelector((state) => state.viewer.editor.verseEdit);
	const mobilePanels = useSelector((state) => state.app.viewer.mobilePanels);
	const searchResults = useSelector((state) => state.viewer.searchResults);
	const layersData = useSelector((state) => state.viewer.transcriptionLayersData);

	const verseRef = useRef();
	const isVisible = IsVerseOnScreen(verseRef);

	const readOnly = viewMode === 'read-only';

	const [ clickNum, setClickNum ] = useState(0);
	const [ debounceClick ] = useDebounce(clickNum, 200);
	const [ contentClass, setContentClass ] = useState('verse__content');

	const editCondition = !(
		readOnly ||
        isMobile ||
        verse.image ||

        (viewMode === 'student' && !(mobilePanels === 'student'))
	);

	useEffect(() => {
		if (verseActive === verse.id && !isVisible) {
			verseRef.current.scrollIntoView({
				behavior: "smooth",
				block: 'center',
			});
		}
	}, [ verse.id, verseActive ]);

	useEffect(() => {
		if (debounceClick >= 2 && editCondition) {
			toggleVerseEdit(verse.id);
		} else if (clickNum > 0) {
			toggleVerseActive(verse.id);
		}
		setClickNum(0);
	}, [ debounceClick ]);

	useEffect(() => {
		if(searchResults.pages.has(pageId)){
			if(searchResults.pages.get(pageId).verses.includes(verse.id + "")){
				setContentClass('verse__content verse__lightup');
			}else{
				setContentClass('verse__content');
			}
		}else{
			setContentClass('verse__content');
		}
	}, [searchResults]);

	let layerNumber = layersData.idToNumber.get(verse.layerId ? `${verse.layerId}` : verse.pageLayer.layerId);

	return (
		<div
			className={ `${annotation ? "verse__block-annotation verse__block-annotation--inactive" : "verse__block verse__block--inactive"}${verse.id === verseActive || verseGroupActive.activeVersesIds.includes(verse.id) ? ' active' : ''} ${verseEdit ? ' activeEdit' : ''}` }
			ref={ verseRef }
		>
			<Button
				onClick={ () => { document.activeElement?.blur(); setClickNum(clickNum + 1); } }
				variant='verse'
			>
				<div className='verse__num'>
					{verse.verseNo}
					<sub>[{layerNumber}]</sub>
				</div>
				<div
					className={ contentClass } 
					dangerouslySetInnerHTML={ { __html: showContent ? verse.content : "" } }
				/>

			</Button>
			{editCondition && verse.id === verseActive &&
			<Button
				onClick={() => {
					toggleVerseEdit(verse.id);
				}}
				variant='verse-edit'
			>
				<span className='d-none'>Edit</span>
				<i className='icon-edit' />
			</Button>}
		</div>
	);
};

VerseInactive.propTypes = {
	showContent: PropTypes.bool,
	verse: PropTypes.object,
	toggleVerseEdit: PropTypes.func,
	toggleVerseActive: PropTypes.func,
	annotation: PropTypes.bool,
};

export default memo(VerseInactive);
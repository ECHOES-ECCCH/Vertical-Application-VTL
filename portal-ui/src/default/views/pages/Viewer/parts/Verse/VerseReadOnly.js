import React, { useState, useEffect, memo, useRef } from "react";
import { useParams } from "react-router";
import PropTypes from "prop-types";
import { useSelector } from 'react-redux';

import Button from "@Elements/Button/Button";

import IsVerseOnScreen from "@Pages/Viewer/parts/Verse/IsVerseOnScreen";

const VerseReadOnly = ({ verse, annotation }) => {
	const { pageId } = useParams();
	const verseActive = useSelector((state) => state.viewer.editor.verseActive);
	const verseEdit = useSelector((state) => state.viewer.editor.verseEdit);
	const searchResults = useSelector((state) => state.viewer.searchResults);
	const layersData = useSelector((state) => state.viewer.transcriptionLayersData);

	const verseRef = useRef();
	const isVisible = IsVerseOnScreen(verseRef);

	const [ contentClass, setContentClass ] = useState('verse__content');

	useEffect(() => {
		if (verseActive === verse.id && !isVisible) {
			verseRef.current.scrollIntoView({
				behavior: "smooth",
			});
		}
	}, [ verse.id, verseActive ]);

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
			className={ `${annotation ? "verse__block-annotation verse__block-annotation--readonly" : "verse__block verse__block--readonly"} ${verse.id === verseActive ? ' active' : ''} ${verseEdit ? ' activeEdit' : ''}` }
			ref={ verseRef }
		>
			<Button
				variant='verse'
			>
				<div className='verse__num'>
					{verse.verseNo}
					<sub>[{layerNumber}]</sub>
				</div>
				<div
					className={ contentClass } 
					dangerouslySetInnerHTML={ { __html: verse.content } }
				/>

			</Button>
		</div>
	);
};

VerseReadOnly.propTypes = {
	verse: PropTypes.object,
	annotation: PropTypes.bool,
};

export default memo(VerseReadOnly);
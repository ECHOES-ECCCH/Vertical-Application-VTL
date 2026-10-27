import React, { useCallback, useEffect, useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useParams, useHistory } from 'react-router';
import PropTypes from "prop-types";

import { viewerActions } from "@/store/actions/viewer.actions";

import VerseInactive from "@Pages/Viewer/parts/Verse/VerseInactive";
import VerseActive from "@Pages/Viewer/parts/Verse/VerseActive";
import VerseReadOnly from "@Pages/Viewer/parts/Verse/VerseReadOnly";

import Music from "@Pages/Viewer/parts/Verse/Music";
import MusicReadOnly from "@Pages/Viewer/parts/Verse/MusicReadOnly";

import VerseComment from '@Pages/Viewer/parts/Verse/VerseComment';
import VerseExpand from '@Pages/Viewer/parts/Verse/VerseExpand';
import VerseStatus from "@Pages/Viewer/parts/Verse/VerseStatus";
import VerseIndex from "@Pages/Viewer/parts/Verse/VerseIndex";

import VerseWords from '@Pages/Viewer/parts/Verse/VerseWords';

import { CheckViewModePermission } from "@/permissions/ViewModes";

const Verse = ({ verse, hierarchyDepth }) => {
	const history = useHistory();
	const dispatch = useDispatch();

	const { transcriptionId, pageId } = useParams();

	const isMobile = useSelector((state) => state.viewer.isMobile);
	const verseEdit = useSelector(state => state.viewer.editor.verseEdit);
	const viewMode = useSelector(state => state.viewer.data.viewMode);
	const mobilePanels = useSelector(state => state.app.viewer.mobilePanels);
	const layersData = useSelector((state) => state.viewer.transcriptionLayersData);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const wordActive = useSelector(state => state.viewer.editor.wordActive);
	const wordGroupActive = useSelector(state => state.viewer.editor.wordGroupActive);
	const wordEdit = useSelector(state => state.viewer.editor.wordEdit);
	const shiftHeld = useSelector(state => state.viewer.editor.shiftPressed);
	const showTextStructure = useSelector(state => (state.viewer.showTextStructure && !state.viewer.annotationLayer));

	const [hasActiveWord, setHasActiveWord] = useState(false);

	useEffect(() => {
		if(verse.words){
			setHasActiveWord(!!verse.words.find((word) => { return word.id == wordActive.wordId || word.id == wordEdit.wordId || word.id == wordGroupActive.activeWordsIds[0]; }));
		}
	}, [verse.words, wordActive, wordGroupActive]);

	const toggleWordActive = useCallback(
		value => {
			dispatch(viewerActions.setWordActive({verseId: verse.id, wordId: value}, false));
		},
		[dispatch],
	);

	const toggleFirstWordInVerseActive = useCallback(
		() => {
			let wordId = verse.words[0].id;
			if (wordId) {
				dispatch(viewerActions.setWordActive({verseId: verse.id, wordId: wordId}, false));
			}
		},
		[dispatch, verse.words],
	);

	const toggleVerseActive = useCallback(
		value => {
			dispatch(viewerActions.setVerseActive(value ? value : null, shiftHeld));
		},
		[dispatch, shiftHeld],
	);

	const toggleVerseEdit = useCallback(
		value => {
			dispatch(viewerActions.setVerseActive(value ? value : null));
			dispatch(viewerActions.setVerseEdit(value ? value : null));
		},
		[dispatch],
	);

	const [isOnAnnotationLayer, setIsOnAnnotationLayer] = useState(false);

	useEffect(() => {
		const layerNum = layersData.idToNumber.get(verse.layerId ? verse.layerId : verse.pageLayer.layerId) - 1;

		const result = layersData.layers[layerNum]?.layer.type === 'IIIF_ANNOTATION';
	
		setIsOnAnnotationLayer(result);
	}, [layersData, verse.layerId, verse.pageLayer?.layerId]);

	const isOnSelectedLayer = verse.layerId ? (selectedLayer == verse.layerId) : (selectedLayer === verse.pageLayer.layerId);

	return (
		<>
			<li
				style={{paddingLeft: (hierarchyDepth * 20 + "px")}}
				className={`viewer__verses--item verse ${
					viewMode === "student" ? `verse__student-status--${verse.learningStatus}` : ``
				}`}
			>
				{verse.type === "MUSIC_NOTATION" ? (
					isOnSelectedLayer ? (
						mobilePanels !== 'student' ? (
							<Music verse={verse} toggleVerseActive={toggleVerseActive} />
						) : (
							<MusicReadOnly verse={verse} />
						)
					) : (
						<MusicReadOnly verse={verse} />
					)
				) : (
					isOnSelectedLayer ? (
						verseEdit === verse.id ? (
							<VerseActive verse={verse} toggleVerseEdit={toggleVerseEdit} toggleVerseActive={toggleVerseActive} annotation={isOnAnnotationLayer} />
						) : (
							<VerseInactive 
								showContent={!hasActiveWord} 
								verse={verse} 
								toggleVerseEdit={toggleVerseEdit} 
								toggleVerseActive={toggleVerseActive} 
								annotation={isOnAnnotationLayer} 
							/>
						)
					) : (
						<VerseReadOnly verse={verse} annotation={isOnAnnotationLayer} />
					)
				)}

				{!isMobile && isOnSelectedLayer && !showTextStructure && verse.indexes?.length > 0 && (
					<VerseIndex verseId={verse.id} indexes={verse.indexes} />
				)}

				{!isMobile && isOnSelectedLayer && CheckViewModePermission("canSeeComments", viewMode) && mobilePanels !== "student" && (
					<VerseComment verse={verse} transcriptionId={transcriptionId} pageId={pageId} />
				)}

				{!isMobile && isOnSelectedLayer && verse.words && verse.words.length !== 0 && (
					<VerseExpand hasActiveWord={hasActiveWord} activateFirstWord={ toggleFirstWordInVerseActive } setActiveWord={ (id) => { toggleWordActive({id}); }} />
				)}

				{!isMobile && CheckViewModePermission("canSeeVersesStatus", viewMode) && mobilePanels !== "student" && (
					<VerseStatus verse={verse} />
				)}
			</li>
			{ (isOnSelectedLayer && (hasActiveWord)) && <VerseWords verseId={verse.id} words={verse.words} hierarchyDepth={hierarchyDepth} /> }
		</>
	);
};

Verse.propTypes = {
	hierarchyDepth: PropTypes.number,
	verse: PropTypes.object,
};

export default Verse;

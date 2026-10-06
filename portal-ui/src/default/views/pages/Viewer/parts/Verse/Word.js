import React, { useCallback } from "react";
import PropTypes from "prop-types";

import { useSelector, useDispatch } from "react-redux";

import { viewerActions } from "@/store/actions/viewer.actions";

import VerseIndex from "@Pages/Viewer/parts/Verse/VerseIndex";
import WordActive from '@Pages/Viewer/parts/Verse/WordActive';
import WordInactive from '@Pages/Viewer/parts/Verse/WordInactive';

import WordMark from '@Pages/Viewer/parts/Verse/WordMark';

const Word = ({verseId, word, hierarchyDepth}) => {
	const dispatch = useDispatch();
	const wordEdit = useSelector(state => state.viewer.editor.wordEdit);
	const isMobile = useSelector((state) => state.viewer.isMobile);
	const shiftHeld = useSelector(state => state.viewer.editor.shiftPressed);
	const showTextStructure = useSelector(state => state.viewer.showTextStructure);

	const toggleWordActive = useCallback(
		value => {
			dispatch(viewerActions.setWordActive(value ? value : {verseId: null, wordId: null}, shiftHeld));
		},
		[dispatch, shiftHeld],
	);

	const toggleWordEdit = useCallback(
		value => {
			dispatch(viewerActions.setWordActive(value ? value : {verseId: null, wordId: null}));
			dispatch(viewerActions.setWordEdit(value ? value : {verseId: null, wordId: null}));
		},
		[dispatch],
	);

	return (
		<li className='verse-word'>
			<div className='verse-word__offset' style={{ paddingLeft: hierarchyDepth ? (hierarchyDepth * 20 + 40) + "px" : "40px" }}>
				{wordEdit.wordId === word.id ? (
					<WordActive verseId={verseId} word={word} toggleWordEdit={toggleWordEdit} />
				) : (
					<WordInactive verseId={verseId} word={word} toggleWordEdit={toggleWordEdit} toggleWordActive={toggleWordActive} />
				)}
			</div>
			{!isMobile && word.teiSubst && (
				<WordMark wordId={word.id} type='subst' additionalInfo={word.teiSubst}/>
			)}
			{!isMobile && word.teiAdd !== null && (
				<WordMark wordId={word.id} type='add' additionalInfo={word.teiAdd}/>
			)}
			{!isMobile && word.teiDel && (
				<WordMark wordId={word.id} type='del'/>
			)}
			{!isMobile && word.teiGap && (
				<WordMark wordId={word.id} type='gap'/>
			)}
			{!isMobile && !showTextStructure && word.indexes?.length > 0 && (
				<VerseIndex verseId={verseId} wordId={word.id} indexes={word.indexes} />
			)}
		</li>
	);
};

Word.propTypes = {
	verseId: PropTypes.number,
	word: PropTypes.object,
	hierarchyDepth: PropTypes.number,
};

export default Word;
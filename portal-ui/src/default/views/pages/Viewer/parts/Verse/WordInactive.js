/* eslint-disable no-console */
import React, { useState, useEffect, useRef } from "react";
import PropTypes from "prop-types";
import { useDebounce } from 'use-debounce';
import { useSelector } from 'react-redux';

import Button from "@Elements/Button/Button";

import IsVerseOnScreen from "@Pages/Viewer/parts/Verse/IsVerseOnScreen";

const WordInactive = ({ verseId, word, toggleWordEdit, toggleWordActive }) => {
	const isMobile = useSelector((state) => state.viewer.isMobile);
	const viewMode = useSelector((state) => state.viewer.data.viewMode);
	const wordActive = useSelector((state) => state.viewer.editor.wordActive);
	const wordGroupActive = useSelector((state) => state.viewer.editor.wordGroupActive);
	const wordEdit = useSelector((state) => state.viewer.editor.wordEdit);
	const mobilePanels = useSelector((state) => state.app.viewer.mobilePanels);

	const verseRef = useRef();
	const isVisible = IsVerseOnScreen(verseRef);

	const readOnly = viewMode === 'read-only';

	const [ clickNum, setClickNum ] = useState(0);
	const [ debounceClick ] = useDebounce(clickNum, 200);

	const editCondition = !(
		readOnly ||
        isMobile ||

        (viewMode === 'student' && !(mobilePanels === 'student'))
	);

	useEffect(() => {
		if (wordActive.wordId === word.id && !isVisible) {
			verseRef.current.scrollIntoView({
				behavior: "smooth",
				block: 'center',
			});
		}
	}, [ word.id, wordActive ]);

	useEffect(() => {
		if (debounceClick >= 2 && editCondition) {
			toggleWordEdit({ verseId: verseId, wordId: word.id });
		} else if (clickNum > 0) {
			toggleWordActive({ verseId: verseId, wordId: word.id });
		}
		setClickNum(0);
	}, [ debounceClick ]);

	return (
		<div
			className={ `verse-word__block verse-word__block--inactive${word.id === wordActive.wordId || wordGroupActive.activeWordsIds.includes(word.id)  ? ' active' : ''} ${wordEdit.wordId ? ' activeEdit' : ''}` }
			ref={ verseRef }
		>
			<Button
				onClick={ () => setClickNum(clickNum + 1) }
				variant='verse'
			>
				<div
					className='verse-word__content'
					dangerouslySetInnerHTML={ { __html: word.content } }
				/>

			</Button>
			{editCondition && word.id === wordActive.wordId &&
			<Button
				onClick={() => {
					toggleWordEdit({ verseId: verseId, wordId: word.id });
				}}
				variant='verse-edit'
			>
				<span className='d-none'>Edit</span>
				<i className='icon-edit' />
			</Button>}
		</div>
	);
};

WordInactive.propTypes = {
	verseId: PropTypes.number,
	word: PropTypes.object,
	toggleWordEdit: PropTypes.func,
	toggleWordActive: PropTypes.func,
};

export default WordInactive;
import React from "react";
import PropTypes from "prop-types";

import Word from '@Pages/Viewer/parts/Verse/Word';

const VerseWords = ({verseId, words, hierarchyDepth}) => {
	return (
		<ul className='viewer__verses--list'>
			{words.map((word) => {
				return <Word verseId={verseId} hierarchyDepth={hierarchyDepth} key={word.id} word={word}/>;
			})}
		</ul>
	);
};

VerseWords.propTypes = {
	verseId: PropTypes.number,
	words: PropTypes.array,
	hierarchyDepth: PropTypes.number,
};

export default VerseWords;
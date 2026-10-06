import React from "react";
import PropTypes from "prop-types";

import styles from "@Pages/Transcription/TranscriptionIndexes/TranscriptionIndexes.module.sass";

function TranscriptionIndexesTag({tagText, tag, onClick, chosenTag, count}) {
	return (
		<>
			{tag === chosenTag ?
				<div className={`${styles["tag-type"]} ${styles["tag-type-active"]}`} onClick={onClick}> {tagText}  <span className={`${styles["tag-type--count"]}`}>({count})</span></div>
				: 
				<div className={styles["tag-type"]} onClick={onClick}> {tagText} <span className={`${styles["tag-type--count"]}`}>({count})</span></div>}
		</>
	);
}

TranscriptionIndexesTag.propTypes = {
	tagText: PropTypes.string, 
	tag: PropTypes.string, 
	onClick: PropTypes.func, 
	chosenTag: PropTypes.string, 
	count: PropTypes.number,
};

export default TranscriptionIndexesTag;

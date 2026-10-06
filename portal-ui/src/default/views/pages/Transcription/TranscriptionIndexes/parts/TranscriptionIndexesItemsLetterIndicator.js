import React from 'react';
import PropTypes from "prop-types";

import styles from "@Pages/Transcription/TranscriptionIndexes/TranscriptionIndexes.module.sass";

function TranscriptionIndexesItemsLetterIndicator({letter}) {
	const letterToShow = letter.toUpperCase();
	return (
		<div className={styles["indexes__table--content-indicator"]}>
			<div className={styles["indexes__table--content-indicator-letter"]}>
				{letterToShow}
			</div>
			<div className={styles["indexes__table--content-indicator-line"]} />
		</div>
	);
}
TranscriptionIndexesItemsLetterIndicator.propTypes = {
	letter: PropTypes.string,
};

export default TranscriptionIndexesItemsLetterIndicator;

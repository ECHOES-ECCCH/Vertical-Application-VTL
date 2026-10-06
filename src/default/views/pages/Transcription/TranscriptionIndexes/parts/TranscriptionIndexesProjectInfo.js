import React from 'react';
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";

import styles from "@Pages/Transcription/TranscriptionIndexes/TranscriptionIndexes.module.sass";

function TranscriptionIndexesProjectInfo({ title, projectOwner }) {
	const { t } = useTranslation();

	return (
		<section className={styles["indexes__info"]}>
			<div className={styles["indexes__info--container"]}>
				<span className={styles["indexes__info--label"]}>{t('transcription.indexes.projectName')}</span>
				<span className={styles["indexes__info--title"]}>{ title }</span>
			</div>
			<div className={styles["indexes__info--container"]}>
				<span className={styles["indexes__info--label"]}>{t('transcription.indexes.author')}</span>
				<span className={styles["indexes__info--author"]}>{ projectOwner }</span>
			</div>
			<br />
			<div className={styles["indexes__info--container"]}>
				<span className={styles["indexes__info--label"]}>{t('transcription.indexes.pageInfo')}</span>
			</div>
		</section>
	);
}

TranscriptionIndexesProjectInfo.propTypes = {
	title: PropTypes.string.isRequired,
	projectOwner: PropTypes.string.isRequired,
};

export default TranscriptionIndexesProjectInfo;

import React from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { reverse } from 'named-urls';
import PropTypes from "prop-types";

import url from "@/router/urls";

import styles from "@Pages/Transcription/parts/TranscriptionParts.module.sass";

function TranscriptionBackLink({ transcriptionId }) {
	const { t } = useTranslation();

	const linkToTranscriptionDetails = reverse(url.transcription.details, { transcriptionId: parseInt(transcriptionId) });

	return (
		<Link to={linkToTranscriptionDetails } className={styles["backlink"]}> {t('transcription.indexes.goToInfoButtonText')}  </Link>
	);
}

TranscriptionBackLink.propTypes = {
	transcriptionId: PropTypes.string.isRequired,
};

export default TranscriptionBackLink;

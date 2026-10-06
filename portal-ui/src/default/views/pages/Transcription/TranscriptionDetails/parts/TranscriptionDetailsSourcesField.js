import React from 'react';
import { useTranslation } from 'react-i18next';
import PropTypes from 'prop-types';
import parse from "html-react-parser";

import styles from "@Pages/Transcription/TranscriptionDetails/TranscriptionDetails.module.sass";

const importFormats = ['mets','iiif','tei'];

const TranscriptionDetailsSourcesField = ({metadata, viewMode}) => {
	const { t } = useTranslation();

	const documentSrc = metadata.transcriptionInfo.documentSrc;
	const documentOrigin = metadata.transcriptionInfo.imgSrc;
	let creationMode = '';
	if (importFormats.includes(metadata.creationMode)){
		creationMode = metadata.creationMode.toString().toUpperCase();
	}

	return (
		<section>
			{ ( metadata.transcriptionInfo.releaseDate ) &&
			<div className={ styles["transcription__meta--details-wrapper"] }>
				<div className={ styles["transcription__meta--details-wrapper--title"] }>
					{t('transcription.metadata.fieldTitle.releaseDate') }:
				</div>
				<div>
					<a href={`/transcriptions?order=last-modification-date&q=${metadata.transcriptionInfo.releaseDate}&page=1&type=ALL`}>{metadata.transcriptionInfo.releaseDate}</a>
				</div>
			</div>}
			{ metadata.transcriptionInfo.documentTypes &&
			<div className={ styles["transcription__meta--details-wrapper"] }>
				<div className={ styles["transcription__meta--details-wrapper--title"] }>
					{t('transcription.metadata.fieldTitle.sourceFileTypes') }:
				</div>
				<div>
					<a href={`/transcriptions?order=last-modification-date&q=${metadata.transcriptionInfo.documentTypes}&page=1&type=ALL`}>{metadata.transcriptionInfo.documentTypes}</a>
				</div>
			</div>}
			<div className={ styles["transcription__meta--details-wrapper"] }>
				<div className={ styles["transcription__meta--details-wrapper--title"] }>
					<span>{t('transcription.metadata.fieldTitle.sourceFileInfo') }: </span>
					{ documentSrc && <strong>{t('transcription.metadata.fieldTitle.sourceFileTypeWebsite')}:</strong> }
					{ !documentSrc && <strong>{t('transcription.metadata.fieldTitle.sourceFileTypeInternal')}</strong> }
				</div>
				{ documentSrc && 
				<div>
					<a href={"//"+documentSrc}>{documentSrc}</a>
				</div> }
			</div>
			{ metadata.transcriptionInfo.imgSrc &&
			<div className={ styles["transcription__meta--details-wrapper"] }>
				<div className={ styles["transcription__meta--details-wrapper--title"] }>
					{t('transcription.metadata.fieldTitle.sourceFileOriginInfo') }:
				</div>
				<div className={ styles["transcription__meta--details-wrapper--rich"] }>
					{parse(documentOrigin)}
				</div>
			</div>}
			{ creationMode &&
			<div className={ styles["transcription__meta--details-wrapper"] }>
				<div className={ styles["transcription__meta--details-wrapper--title"] }>
					{t('transcription.metadata.fieldTitle.projectImported') }:
				</div>
				<div>
					{creationMode}
				</div>
			</div>}
		</section>
	);
};

TranscriptionDetailsSourcesField.propTypes = {
	metadata: PropTypes.object,
	viewMode: PropTypes.string,
};

export default TranscriptionDetailsSourcesField;
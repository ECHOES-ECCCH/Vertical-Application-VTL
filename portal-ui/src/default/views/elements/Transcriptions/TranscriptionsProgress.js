import React from 'react';
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";

import ProgressBar from '@Elements/ProgressBar/ProgressBar';
import styles from '@Elements/Transcriptions/TranscriptionsItem.module.sass';

const TranscriptionsProgress = ({percentOfVerified = 0, percentOfTranscribed = 0, matchesPerTranscription = [], isSearchView, isUserTranscriptions, useMetadata}) => {
	const { t } = useTranslation();

	return (
		<div className={ styles["transcr-item__description--progress"] }>
			{ isSearchView && !isUserTranscriptions && useMetadata != "METADATA" ? 
				matchesPerTranscription.length !== 0 ?
					<div className={ styles["transcr-item__description--matches"]}>{ `${t('transcriptions.item.foundMatches')} `} 
						<span className={ styles["transcr-item__description--matches-bold"]}>{matchesPerTranscription.reduce((a,b)=>(parseInt(a)+parseInt(b)), 0)}</span> 
						{` ${t('transcriptions.item.on')} `} 
						{/* <span className={ styles["transcr-item__description--matches-bold"]}>{matchesPerTranscription.length}</span> */}
						{matchesPerTranscription.length > 1 ? 
							<><span className={ styles["transcr-item__description--matches-bold"]}> {matchesPerTranscription.length} </span> {t('transcriptions.item.pages')} </>
							:t('transcriptions.item.page')} 
						
					</div> : null
				: 
				""}
			{(percentOfVerified || percentOfVerified > 0) ?
				(
					<div className={ styles["transcr-item__description--progress-wrapper"] }>
						<div className={ styles["transcr-item__description--progress-text"] }>
							{t('transcriptions.description.verificationAdvance')}
						</div>
						<div className={ styles["transcr-item__description--progress-bar"] }>
							<ProgressBar progress={ percentOfVerified } />
						</div>
						<div className={ styles["transcr-item__description--progress-text"] }>
							{percentOfVerified}%
						</div>
					</div>
				) : null}
			{(percentOfTranscribed || percentOfTranscribed === 0) ? (
				<div className={ styles["transcr-item__description--progress-wrapper"] }>
					<div className={ `${styles["transcr-item__description--progress-text"]} ${styles["transcr-item__description--progress-text-space"]}` }>
						{t('transcriptions.description.transcriptionAdvance')}
					</div>
					<div className={ styles["transcr-item__description--progress-bar"] }>
						<ProgressBar progress={ percentOfTranscribed } />
					</div>
					<div className={ styles["transcr-item__description--progress-text"] }>
						{percentOfTranscribed}%
					</div>
				</div>
			) : null}

		</div>
	);
};

TranscriptionsProgress.propTypes = {
	percentOfVerified: PropTypes.number,
	percentOfTranscribed: PropTypes.number,
	matchesPerTranscription: PropTypes.number,
	isSearchView: PropTypes.bool,
	isUserTranscriptions: PropTypes.bool,
	useMetadata: PropTypes.string,
};

export default TranscriptionsProgress;
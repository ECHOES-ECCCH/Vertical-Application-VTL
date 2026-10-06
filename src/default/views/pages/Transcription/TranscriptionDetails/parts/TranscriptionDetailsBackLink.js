import React from 'react';
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Link } from "react-router-dom";

import url from "@/router/urls";

import styles from "@Pages/Transcription/TranscriptionDetails/TranscriptionDetails.module.sass";

const TranscriptionDetailsBackLink = () => {
	const { t } = useTranslation();

	const isLoggedIn = useSelector(state => state.auth.credentials.isLoggedIn);

	return (
		<div>
			{(isLoggedIn) &&
				<div>
					<div className={ styles["transcription__backLink"] }>
						{t('transcription.details.returnToMyTranscripts1')}
						<Link to={ url.transcriptionsUser }>
							{t('transcription.details.returnToMyTranscripts2')}
						</Link>
					</div>
					
				</div>}
		</div>
	);
};

export default TranscriptionDetailsBackLink;

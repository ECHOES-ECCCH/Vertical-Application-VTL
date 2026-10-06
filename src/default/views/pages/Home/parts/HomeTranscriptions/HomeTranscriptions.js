import React from 'react';
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";

import TranscriptionItem from '@Elements/Transcriptions/TranscriptionsItem';

import styles from "@Pages/Home/parts/HomeTranscriptions/HomeTranscriptions.module.sass";

const HomeTranscriptions = ({transcriptions, homeComponent}) => {
	const { t } = useTranslation();

	return (
		<section className={ styles["home-transcriptions"] }>
			<div className={ styles["home-transcriptions__header"] }>
				<h2 className={ styles["home-transcriptions__header--title"] }>
					{t('main.'+homeComponent+'.title')}
				</h2>

			</div>
			<div className={ styles["home-transcriptions__transcriptions"] }>
				{homeComponent === 'lastTranscribed' ?
					(transcriptions.length > 0 ?
						transcriptions.map((item, index) => (
							<TranscriptionItem
								transcription={ item }
								key={ item.analyticInfo.title + index.toString() }
							/>
						))
						:
						<h2 className={ styles["home-transcriptions__header--title"] }>
							{t('main.'+homeComponent+'.noTranscriptions')}
						</h2>
					) : <TranscriptionItem transcription={ transcriptions } />}
			</div>
		</section>
	);
};

HomeTranscriptions.propTypes = {
	transcriptions: PropTypes.oneOfType([
		PropTypes.object,
		PropTypes.arrayOf(
			PropTypes.shape({
				map: PropTypes.object,
				length: PropTypes.number,
			}).isRequired,
		),
	]),
	homeComponent: PropTypes.string,
};

export default HomeTranscriptions;

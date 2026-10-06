import React, { memo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import PropTypes from 'prop-types';
import { Container } from "react-bootstrap";

import Moment from 'react-moment';

import { reverse } from 'named-urls';
import { Link } from "react-router-dom";

import url from "@/router/urls";
import TranscriptionsItemPage from '@Elements/Transcriptions/TranscriptionsItemPage';
import TranscriptionsProgress from '@Elements/Transcriptions/TranscriptionsProgress';

import LinkButton from "@Elements/Button/LinkButton";

import styles from '@Elements/Transcriptions/TranscriptionsItem.module.sass';

const TranscriptionsItem = ({ transcription, matchesPerTranscription, isSearchView, isUserTranscriptions, useMetadata, pagesToUse, searchString }) => {
	const { creator, pages, percentOfVerified, viewMode, creationMode, analyticInfo, transcriptionInfo } = transcription;
	const { t } = useTranslation();

	const linkToTranscriptionDetails = reverse(url.transcription.details, { transcriptionId: parseInt(transcription.id) });
	const layers = transcription.layers.sort( (a,b) => Math.sign(parseInt(a.id)-parseInt(b.id)) );
	const layersNum = layers.length || 0;
	const [ showAll, setShowAll ] = useState(false);

	return (

		<div className={ styles["transcr-item"] }>
			<div className={ styles["transcr-item__description--left-wrapper"] }>
				<Container>
					<Link
						to={ linkToTranscriptionDetails }
						className={styles["transcr-item__description--title"] }
					>
						{analyticInfo?.title}
					</Link>
					{ ( transcription.datePublished) &&
						<div>
							{t('transcriptions.description.sourcePublishDate')}
							<Moment
								format='L'
							>
								{transcription.datePublished}
							</Moment>
						</div>}
				</Container>
			</div>

			<div className={ styles["transcr-item__description"] }>
				<Container>
					{transcriptionInfo &&
					<div className={ styles["transcr-item__description--metadata"]}>
						<div className={ styles["transcr-item__description--metadata-left"]}>
							{creator?.email && 
							<div>
								{t('transcriptions.description.projectAuthor') } {creator.email}
							</div>}
							{transcriptionInfo.documentTypes && 
							<>
								<span className={ styles["transcr-item__description--separator"] } />
								<div>
									{`${t('transcriptions.description.type')}${ transcriptionInfo.documentTypes }`}
								</div>
							</>}
							{transcriptionInfo.releaseDate &&
							<>
								<span className={ styles["transcr-item__description--separator"] } />
								<div>
									{t('transcriptions.description.releaseDate')}{transcriptionInfo.releaseDate}
								</div>
							</>}
						</div>
						{pages &&
						<div className={ styles["transcr-item__description--metadata-right"]}>
							{t('transcriptions.description.files')}{pages.hits}
						</div>}
					</div>}
				</Container>
			</div>

			<Container>
				<div className={ styles["transcr-item__description--addInfo"] }>
					<div>{t('transcription.details.transcriptionLayersCount')}{layersNum}</div>

					{isSearchView ?
						<TranscriptionsProgress
							isSearchView={ isSearchView }
							transcriptionPages={ pagesToUse ? pagesToUse.hitCount : 0 }
							matchesPerTranscription={ matchesPerTranscription }
							percentOfVerified={ transcription.percentOfVerified }
							percentOfTranscribed={ transcription.percentOfTranscribed }
							isUserTranscriptions={ isUserTranscriptions }
							useMetadata={ useMetadata }
						/> :
						<TranscriptionsProgress
							isSearchView={ isSearchView }
							matchesPerTranscription={matchesPerTranscription}
							percentOfVerified={ transcription.percentOfVerified }
							percentOfTranscribed={ transcription.percentOfTranscribed }
							isUserTranscriptions={ isUserTranscriptions }
							useMetadata={ useMetadata }
						/>}
				</div>

				<div className={ styles["transcr-item__pages"] }>
					{(searchString && searchString !== null) ?
						(pagesToUse &&
							(pagesToUse.length > 0) ?
							pagesToUse.slice(0, showAll ? pagesToUse.length : 6).map((page, index) =>
								(
									<TranscriptionsItemPage
										key={ page.page.id }
										page={ page.page }
										transcriptionId={ parseInt(transcription.id) }
										percentOfVerified={ percentOfVerified }
										viewMode={ viewMode }
										creationMode={ creationMode }
										matchesPerTranscription={page}
										layers={layers}
									/>
								)):
							(pages &&
								(pages.list.length > 0) && pages.list.map((page) =>
								(
									<TranscriptionsItemPage
										key={ page.id }
										page={ page }
										transcriptionId={ parseInt(transcription.id) }
										percentOfVerified={ percentOfVerified }
										viewMode={ viewMode }
										creationMode={ creationMode }
										layers={layers}
									/>
								),
							))
						):
						(pages &&
							(pages.list.length > 0) && pages.list.map((page) =>
							(
								<TranscriptionsItemPage
									key={ page.id }
									page={ page }
									transcriptionId={ parseInt(transcription.id) }
									percentOfVerified={ percentOfVerified }
									viewMode={ viewMode }
									creationMode={ creationMode }
									layers={layers}
								/>
							),
						))}
				</div>
				<div className={ styles["transcr-item__footer"] }>
					{(searchString && searchString !== null && pagesToUse && pagesToUse.length > 6) &&
						<div className={ styles["transcr-item__footer__first"] }/> }
					<LinkButton
						variant='primary'
						className={ styles["transcr-item__button"] }
						to={linkToTranscriptionDetails}
					>
						{t('transcriptions.description.showMore')}
					</LinkButton>
					{(searchString && searchString !== null && pagesToUse && pagesToUse.length > 6) && 
						<div className={styles["transcr-item__footer__show-all"]}>
							<span onClick={()=>{ setShowAll(!showAll); }}>
								{ !showAll ? t('transcriptions.item.expandMatches') : t('transcriptions.item.foldMatches') }
								{ showAll ? <i className='icon-arrow-up-full' /> : <i className='icon-arrow-down-full' /> }
							</span>
						</div>}
				</div>
			</Container>
		</div>
	);
};

TranscriptionsItem.propTypes = {
	transcription: PropTypes.object,
	matchesPerTranscription: PropTypes.number,
	isSearchView: PropTypes.bool,
	isUserTranscriptions: PropTypes.bool,
	useMetadata: PropTypes.string,
	pagesToUse: PropTypes.array,
	searchString: PropTypes.string,
};

export default memo(TranscriptionsItem);
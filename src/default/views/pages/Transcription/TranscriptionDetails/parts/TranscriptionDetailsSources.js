import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";

import Button from '@Elements/Button/Button';

import TranscriptionDetailsSourcesField from "@Pages/Transcription/TranscriptionDetails/parts/TranscriptionDetailsSourcesField";
import styles from "@Pages/Transcription/TranscriptionDetails/TranscriptionDetails.module.sass";

const TranscriptionDetailsSources = ({ metadata, viewMode }) => {

	const { t } = useTranslation();

	const currentLang = useSelector((state) => state.app.currentLang);

	const [ collapsed, setCollapsed ] = useState(true);
	const [ TranscriptionData, setTranscriptionDetails ] = useState({});
	const [ analyticLanguage, setAnalyticLanguage ] = useState();
	const [ monographicLanguage, setMonographicLanguage ] = useState();
	const [ Type, setType ] = useState();
	const [ Chapter, setChapter ] = useState();
	const [ Volume, setVolume ] = useState();
	const [ PageRange, setPageRange ] = useState();
	const [ Publisher, setPublisher ] = useState();
	const [ PublicationPlace, setPublicationPlace ] = useState();
	const [ PublicationDate, setPublicationDate ] = useState();
	const [ ISBN, setISBN ] = useState();
	const [ EISBN, setEISBN ] = useState();
	const [ EISSN, setEISSN ] = useState();
	const [ ISSN, setISSN ] = useState();
	const [ Pages, setPages ] = useState();
	const [ IssueNo, setIssueNo ] = useState();
	const [ Summary, setSummary ] = useState();
	const [ Keywords, setKeywords ] = useState([]);
	const [ links, setLinks ] = useState([]);
	const [ MonographicActors, setMonographicActors ] = useState([]);
	const [ AnalyticActors, setAnalyticActors ] = useState([]);
	const [ externalUrl, setExternalUrl ] = useState('');
	const [ externalId, setExternalId ] = useState('');
	const [ monographicTitle, setMonographicTitle ] = useState('');
	const [ analyticTitle, setAnalyticTitle ] = useState('');
	const [ transcriptionState, setTranscriptionState ] = useState('');

	const toggle = () => {
		setCollapsed(!collapsed);
	};

	const roleFilter = (array, role) => {
		return array.filter(arr => arr.role.code === role);
	};
	const findCodes = (array) => {
		return array.map(a => a.role.code);
	};

	useEffect(() => {
		if (metadata) {
			setTranscriptionDetails(metadata);
			if (metadata.analyticInfo.language) {
				setAnalyticLanguage(metadata.analyticInfo.language[currentLang]);
			}
			if (metadata.monographicInfo.language) {
				setMonographicLanguage(metadata.monographicInfo.language[currentLang]);
			}
			setAnalyticTitle(metadata.analyticInfo.title);
			setAnalyticActors(metadata.analyticInfo.actors);
			if (metadata.continuation) {

				setTranscriptionState(metadata.continuation.state);
			}
			if (metadata.monographicInfo.biblType) {
				setType(metadata.monographicInfo.biblType[currentLang]);
			}
			setMonographicTitle(metadata.monographicInfo.title);
			setChapter(metadata.monographicInfo.chapterNo);
			setVolume(metadata.monographicInfo.volumeNo);
			setPageRange(metadata.monographicInfo.pageRange);
			setPublisher(metadata.monographicInfo.publisher);
			setPublicationPlace(metadata.monographicInfo.publicationPlace);
			setPublicationDate(metadata.monographicInfo.publicationDate);
			setMonographicActors(metadata.monographicInfo.actors);
			setISBN(metadata.monographicInfo.isbn);
			setEISBN(metadata.monographicInfo.eisbn);
			setEISSN(metadata.monographicInfo.eissn);
			setISSN(metadata.monographicInfo.issn);
			setPages(metadata.monographicInfo.pages);
			setIssueNo(metadata.monographicInfo.issueNo);

			setSummary(metadata.profileDesc.summary);
			setKeywords(metadata.profileDesc.keywords);
			setLinks(metadata.profileDesc.links);
			setExternalId(metadata.externalId);
			setExternalUrl(metadata.externalUrl);
			setMonographicTitle(metadata.monographicInfo.title);
			setAnalyticTitle(metadata.analyticInfo.title);
		}
	}, [ metadata, currentLang ]);

	const AnalyticActorsCodes = new Set(findCodes(AnalyticActors));
	const MonographicActorsCodes = new Set(findCodes(MonographicActors));

	return (
		<section className={ styles["transcription__section--border"] }>
			<div className={ styles["transcription__main"] }>
				<div className={ styles["transcription__main--button-wrapper"] }>
					<h3>{t('transcription.metadata.fieldTitle.sourcesInfo')}</h3>
					<Button className={styles["transcription__main--button-wrapper--button-collapse"]}
						onClick={ toggle }
						variant='link'
					>
						{collapsed ?t('transcription.details.expand') :t('transcription.details.collapse')}
						{collapsed ? <i className='icon-arrow-down-full' /> : <i className='icon-arrow-up-full' />}
					</Button>
				</div>
				<div className={collapsed ? styles["transcription__main--collapsed"] : ""}>
					{metadata && 
						<TranscriptionDetailsSourcesField
							metadata={ metadata }
							viewMode={ viewMode }
						/>}
				</div>
			</div>
		</section>

	);
};

TranscriptionDetailsSources.propTypes = {
	metadata: PropTypes.object,
	viewMode: PropTypes.string,
};

export default TranscriptionDetailsSources;

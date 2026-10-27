import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";
import { Link } from 'react-router-dom';

import url from "@/router/urls";
import Button from '@Elements/Button/Button';

import styles from "@Pages/Transcription/TranscriptionDetails/TranscriptionDetails.module.sass";

const TranscriptionDetailsMetadata = ({ metadata, viewMode }) => {

	const { t } = useTranslation();

	const currentLang = useSelector((state) => state.app.currentLang);

	const [ collapsed, setCollapsed ] = useState(true);
	const [ TranscriptionData, setTranscriptionDetails ] = useState({});
	const [ analyticLanguage, setAnalyticLanguage ] = useState();
	const [ monographicLanguage, setMonographicLanguage ] = useState();
	const [ Type, setType ] = useState();
	const [ signature, setSignature ] = useState();
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

			if (metadata.monographicInfo.biblType) {
				setType(metadata.monographicInfo.biblType[currentLang]);
			}
			setSignature(metadata.monographicInfo.signature);
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
		<section className={ styles["transcription__section"] }>
			<div className={ styles["transcription__main"] }>
				<div className={ styles["transcription__main--button-wrapper"] }>
					<h3/>
					<Button
						onClick={ toggle }
						variant='link'
					>
						{collapsed ?t('transcription.details.expand') :t('transcription.details.collapse')}
						{collapsed ? <i className='icon-arrow-down-full' /> : <i className='icon-arrow-up-full' />}
					</Button>
				</div>
				{(analyticTitle || AnalyticActors.length > 0 || analyticLanguage) &&
					<div className={
						collapsed ? styles["transcription__main--collapsed"] : styles["transcription__main--first"]
					}
					>
						<h2 className={ styles["transcription__description--title"] }>{t('transcription.details.analyticLevel')}</h2>
						<p className={ styles["transcription__description--text"] }>{t('transcription.details.analyticLevelDescription')}</p>
					</div>}
				<div className={ collapsed ? styles["transcription__meta--collapsed"] : styles["transcription__meta"] }>
					{(collapsed || (!collapsed && analyticTitle)) &&
						<>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.title')}</h3>
							<h3 className={ styles["transcription__meta--title"] }>{(viewMode === 'read-only') && <i className='icon-lock-close2' />}{collapsed ? TranscriptionData.titleLabel : analyticTitle}</h3>
						</>}
				</div>
				{collapsed && TranscriptionData.authorsLabel &&
					<div className={ styles["transcription__meta"] }>
						<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.author')}</h3>
						<h3 className={ styles["transcription__meta--value"] }>{TranscriptionData.authorsLabel}</h3>
					</div>}

				<div className={
					collapsed ? styles["transcription__main--collapsed"] : ""
				}
				>
					{AnalyticActorsCodes &&
						<>
							{AnalyticActorsCodes.map(element => {
								let newActors = roleFilter(AnalyticActors, element);
								return (
									<div
										className={ styles["transcription__meta"] }
										key={ element }
									>
										<h3 className={ styles["transcription__meta--name"] }>{newActors[0].role[currentLang]}: </h3>
										<h3 className={ styles["transcription__meta--value"] }>{newActors.map((item, id) => {
											return (
												(id !== newActors.length - 1) ? (item.name + "; ") : (item.name)
											);
										})}
										</h3>
									</div>
								);
							})}
						</>}
					{analyticLanguage &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.language')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{analyticLanguage}</h3>
						</div>}
					{(
						monographicTitle 
						|| monographicLanguage 
						|| Type 
						|| signature 
						|| MonographicActorsCodes.size > 0 
						|| Chapter 
						|| Volume 
						|| PageRange 
						|| PublicationDate 
						|| PublicationPlace 
						|| Publisher 
						|| ISBN 
						|| Pages 
						|| EISSN 
						|| ISSN 
						|| EISBN 
						|| IssueNo
					) &&
						<>
							<h2 className={ styles["transcription__description--title"] }>{t('transcription.details.monographicLevel')}</h2>
							<p className={ styles["transcription__description--text"] }>{t('transcription.details.monographicLevelDescription')}</p>
						</>}
					{monographicTitle &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.title')}</h3>
							<h3 className={ styles["transcription__meta--title"] }>{monographicTitle}</h3>
						</div>}
					{Type &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.type')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{Type}</h3>
						</div>}
					{signature &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.form.signature')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{signature}</h3>
						</div>}
					{MonographicActorsCodes &&
						<>
							{MonographicActorsCodes.map(element => {
								let newActors = roleFilter(MonographicActors, element);
								return (
									<div
										className={ styles["transcription__meta"] }
										key={ element }
									>
										<h3 className={ styles["transcription__meta--name"] }>{newActors[0].role[currentLang]}: </h3>
										<h3 className={ styles["transcription__meta--value"] }>{newActors.map((item, id) => {
											return (
												(id !== newActors.length - 1) ? (item.name + "; ") : (item.name)
											);
										})}
										</h3>
									</div>
								);
							})}
						</>}
					{monographicLanguage &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.language')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{monographicLanguage}</h3>
						</div>}
					{Chapter &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.numberOfChapter')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{Chapter}</h3>
						</div>}
					{Volume &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.volumeNumber')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{Volume}</h3>
						</div>}
					{IssueNo &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.numberOfNotebook')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{IssueNo}</h3>
						</div>}
					{PageRange &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.pageRange')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{PageRange}</h3>
						</div>}
					{Pages &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.pages')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{Pages}</h3>
						</div>}
					{Publisher &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.publisher')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{Publisher}</h3>
						</div>}
					{PublicationPlace &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.placeOfPublication')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{PublicationPlace}</h3>
						</div>}
					{PublicationDate &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.dateOfPublication')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{PublicationDate}</h3>
						</div>}
					{ISBN &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.isbn')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{ISBN}</h3>
						</div>}
					{EISBN &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.EISBN')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{EISBN}</h3>
						</div>}
					{ISSN &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.ISSN')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{ISSN}</h3>
						</div>}
					{EISSN &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.EISSN')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{EISSN}</h3>
						</div>}
					{(Keywords.length > 0 || Summary || links.length > 0) &&
						<>
							<h2 className={ styles["transcription__description--title"] }>{t('transcription.details.textLevel')}</h2>
							<p className={ styles["transcription__description--text"] }>{t('transcription.details.textLevelDescription')}</p>
						</>}
					{Keywords.length > 0 &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.keywords')}</h3>
							<div className={ styles["transcription__meta--value"] }>
								{Keywords.map((item, id) => {
									return (
										<span key={ item }>
											<Link
												className={ styles["transcription__meta--links"] }
												to={ `${url.transcriptions}?order=last-modification-date&q=${item}&page=1` }
											>
												{item}
											</Link>
											{(id !== links.length - 1) && (<>; </>)}
										</span>

									);
								})}
							</div>
						</div>}
					{Summary &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.summary')}</h3>
							<h3 className={ styles["transcription__meta--value"] }>{Summary}</h3>
						</div>}
					{links.length > 0 &&
						<div className={ styles["transcription__meta"] }>
							<h3 className={ styles["transcription__meta--name"] }>{t('transcription.details.links')}</h3>
							<h3 className={ styles["transcription__meta--links-container"] }>

								{links.map((item, id) => (
									<span key={ item }>
										<a
											className={ styles["transcription__meta--links"] }
											href={ item }
											target='_blank'
											rel='noopener noreferrer'
										>
											{item}
											<i className='icon-external' />
										</a>
										{(id === links.length - 1) || (<>; </>)}
									</span>
								))}

							</h3>
						</div>}
					{(externalUrl || externalId) &&
						<>
							<h2 className={ styles["transcription__description--title"] }>{t('transcription.details.identifierLevel')}</h2>
							{externalUrl &&
								<div className={ styles["transcription__meta"] }>
									<h3 className={ styles["transcription__meta--name"] }>
										{t('transcription.details.importAdress')}
										<span
											className='transcription__tooltip'
											data-tooltip-id='custom-tooltip' data-tooltip-content={ t('transcription.details.importAdressDesc') }
											data-place='right'
										>
											<i className='icon-question'  />
										</span>
									</h3>
									<h3 className={ styles["transcription__meta--value"] }>
										<a
											className={ styles["transcription__meta--links"] }
											href={ externalUrl }
											target='_blank'
											rel='noopener noreferrer'
										>
											{externalUrl}
										</a>
									</h3>
								</div>}
							{externalId &&
								<div className={ styles["transcription__meta"] }>
									<h3 className={ styles["transcription__meta--name"] }>
										{t('transcription.details.externalId')}
										<span
											className='transcription__tooltip'
											data-tooltip-id='custom-tooltip' data-tooltip-content={ t('transcription.details.externalIdDesc') }
											data-place='right'
										>
											<i className='icon-question'  />
										</span>
									</h3>
									<h3 className={ styles["transcription__meta--value"] }>{externalId}</h3>
								</div>}
						</>}
				</div>
			</div>
		</section>

	);
};

TranscriptionDetailsMetadata.propTypes = {
	metadata: PropTypes.object,
	viewMode: PropTypes.string,
};

export default TranscriptionDetailsMetadata;

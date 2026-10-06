import React, { useEffect, useState } from 'react';
import { useSelector } from "react-redux";
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";
import { reverse } from 'named-urls';
import { Dropdown, Container } from 'react-bootstrap';
import { useHistory } from 'react-router-dom';

import url from "@/router/urls";

import Alert from "@Elements/Alert/Alert";
import Button from '@Elements/Button/Button';
import LinkButton from "@Elements/Button/LinkButton";
import Pagination from '@Components/Pagination/Pagination';
import TranscriptionsItemPage from '@Elements/Transcriptions/TranscriptionsItemPage';
import { CheckSecurityGroupPermission } from "@/permissions/SecurityGroups";
import { CheckViewModePermission } from "@/permissions/ViewModes";

import { useShowModal } from '@Elements/Modal/ModalHooks';

import styles from "@Pages/Transcription/TranscriptionDetails/TranscriptionDetails.module.sass";

import ProgressBar from '@Elements/ProgressBar/ProgressBar';

const TranscriptionDetailsPages = ({ 
	pages, 
	percentOfTranscribed,
	lastModificationDate, 
	onGoToPage, 
	viewMode, 
	creationMode, 
	transcriptionId, 
	transcriptionProcessingStatusDisabledButtons,
	reloadPages,
	quota,
	continuation,
	usersCount,
	layers,
}) => {
	const { t } = useTranslation();

	const securityGroup = useSelector((state) => state.auth.credentials.securityGroup);

	const [ currentPageNum, setCurrentPageNum ] = useState(0);
	const [ pagesNum, setPagesNum ] = useState(1);
	const [ overallTransciptionsNum, setOverallTransciptionsNum ] = useState(0);
	const [ pagesList, setPagesList ] = useState([]);
	
	const history = useHistory();
	const hasContinuation = continuation && continuation.lastPageId;
	const isEmptyTranscription = pages.hits === 0;

	const modalAutoTranscribePages = useShowModal({
		title:t('modals.autoTranscribePages.title'),
		name: 'autoTranscribePages',
		componentPath: './parts/ModalAutoTranscribePages',
		componentProps: { transcriptionId: parseInt(transcriptionId), layers: layers  },
	});

	const modalTrainOCR = useShowModal({
		title:t('modals.trainOCR.title'),
		name: 'trainOCR',
		componentPath: './parts/ModalTrainOCR',
		componentProps: { transcriptionId: parseInt(transcriptionId), layers: layers, pageCount: pages.hits  },
	});

	useEffect(() => {
		setCurrentPageNum(pages.page);
		setPagesNum(pages.pages);
		setOverallTransciptionsNum(pages.hits);
		setPagesList(pages.list);
	}, [ pages, pages.list ]);

	const onContinueTranscription = () => {
		if (continuation.lastPageId){
			const linkToTranscriptionView = reverse(url.transcription.viewer, { transcriptionId: parseInt(transcriptionId), pageId: parseInt(continuation.lastPageId) });
			history.push(linkToTranscriptionView);
		}
		
	};

	const modalDownloadResult = useShowModal({
		title:t('modals.downloadResults.title'),
		name: 'downloadResultsPage',
		type: 'download',
		componentPath: './parts/ModalDownloadResults',
		componentProps: { transcriptionId: parseInt(transcriptionId), layers: layers },
		withClose: true,
	});

	const modalImportTextToLayer = useShowModal({
		title:t('modals.importTextToLayer.title'),
		name: 'modalImportTextToLayer',
		componentPath: './parts/ModalImportTextToLayer',
		componentProps: { transcriptionId: parseInt(transcriptionId), layers: layers },
		withClose: true,
	});

	const modalAddPages = useShowModal({
		title:t('modals.addPages.title'),
		name: 'modalAddPages',
		componentPath: './parts/ModalAddPages',
		componentProps: { transcriptionId: parseInt(transcriptionId), reloadPages: reloadPages, quota: quota },
		withClose: false,
	});

	const linkToTranscriptionPermissions = reverse(url.transcription.permissions, { transcriptionId: transcriptionId });
	const linkToTranscriptionLayers = reverse(url.transcription.layers, { transcriptionId: transcriptionId});

	return (
		<div className={ styles["pages-list__wrapper"] }>
			<Container>
				<section className={ styles["pages-list"] }>
					<Alert className={ styles["transcription__alert--margin-bottom"] } />
					<div className={ styles["pages-list__head"] }>
						<div className={ styles["pages-list__head-column"] }>
							<span className={ styles["pages-list__header--thick"] }>
								<span className={ styles["pages-list__header--thick--item"] }>
									<span className={ styles["pages-list__header--thick--item--key"] }>{t('transcriptions.description.lastEdited')}</span>
									<span className={ styles["pages-list__header--thick--item--value"] }>{lastModificationDate}</span>
								</span>
								<span className={ styles["pages-list__header--thick--item"] }>
									<span className={ styles["pages-list__header--thick--item--key"] }>{t('transcriptions.description.transcriptionAdvance')}</span>
									<span className={ styles["pages-list__header--thick--item--value"] }>
										<span className={ styles["pages-list__header--thick--item--progress-bar"] }><ProgressBar progress={ percentOfTranscribed } /></span>
										{percentOfTranscribed}%
									</span>
								</span>
							</span>
						</div>

					</div>
					<div className={ styles["pages-list__head"] }>
						<div className={ styles["transcription__desktopNav"] }>
							<div
								className='btn-group'
								role='group'
							>
								{ CheckViewModePermission("canSeeContinueTranscriptionScreenButton", viewMode) && 
								<Button
									variant='primary'
									onClick={ () => onContinueTranscription() }
									disabled={ ((!hasContinuation) || transcriptionProcessingStatusDisabledButtons) }
								>
									{t('transcription.details.continue') }
								</Button>}
								<LinkButton
									variant='primary'
									href={ linkToTranscriptionLayers }
									disabled={ false }
								>
									{t('transcription.details.addTextLayer') }
								</LinkButton>
								<LinkButton
									variant='primary'
									href={ reverse(url.transcription.indexes, { transcriptionId: parseInt(transcriptionId) }) }
									disabled={ isEmptyTranscription }
								>
									{t('transcription.details.indexes')}
								</LinkButton>
								{ CheckViewModePermission("canSeeModerationScreenButton", viewMode) && 
								<LinkButton
									variant='primary'
									href={ reverse(url.transcription.verification, { transcriptionId: parseInt(transcriptionId) }) }
									disabled={ !(CheckViewModePermission("canOpenModerationScreen", viewMode) && overallTransciptionsNum > 0) }
								>
									{t('transcription.details.moderation')}
								</LinkButton> }
								{ CheckViewModePermission("canSeeContributorsScreenButton", viewMode) && 
								<LinkButton
									variant='primary'
									href={ linkToTranscriptionPermissions }
									disabled={ !CheckViewModePermission("canOpenContributorsScreen", viewMode) }
								>
									{t('transcription.details.contributors')} ({usersCount})
								</LinkButton> }
							</div>
						</div>
						<div className={ styles["transcription__mobileNav"] }>
							<Dropdown className={ styles["transcription__mobileNav--dropdown"] }>
								<Dropdown.Toggle
									variant='dropdown-secondary'
									id='details'
								>
									<i className='icon-hamburger' />
									{t('transcription.details.workWithProject')}
									<i className='icon-arrow-down-full' />
								</Dropdown.Toggle>
								<Dropdown.Menu className={ styles["transcription__mobileNav--dropdown-menu"] }>
									{ CheckViewModePermission("canSeeContinueTranscriptionScreenButton", viewMode) && 
									<Dropdown.Item 
										onClick={ () => onContinueTranscription() } 
										disabled={ ((!hasContinuation) || transcriptionProcessingStatusDisabledButtons) }
									>
										{t('transcription.details.continue')}
									</Dropdown.Item> }
									<Dropdown.Item 
										href={ linkToTranscriptionLayers } 
										disabled={ false }
									>
										{t('transcription.details.addTextLayer')}
									</Dropdown.Item>
									<Dropdown.Item href={ reverse(url.transcription.indexes, { transcriptionId: parseInt(transcriptionId) }) }
										disabled={ isEmptyTranscription }
									>
										{t('transcription.details.indexes')}
									</Dropdown.Item>
									{ CheckViewModePermission("canSeeModerationScreenButton", viewMode) && 
									<Dropdown.Item 
										href={ reverse(url.transcription.verification, { transcriptionId: parseInt(transcriptionId) }) }
										disabled={ !(CheckViewModePermission("canOpenModerationScreen", viewMode) && overallTransciptionsNum > 0) }
									>
										{t('transcription.details.moderation')}
									</Dropdown.Item> }
									{ CheckViewModePermission("canSeeContributorsScreenButton", viewMode) && 
									<Dropdown.Item 
										href={ linkToTranscriptionPermissions }
										disabled={ false }
									>
										{t('transcription.details.contributors')} ({usersCount})
									</Dropdown.Item>}
								</Dropdown.Menu>
							</Dropdown>
						</div>
				
					</div>
					<div className={ styles["pages-list__head"] }>
						<div className={ styles["transcription__desktopNav"] }>
							<div
								className='btn-group'
								role='group'
							>
								{ CheckViewModePermission("canSeeAddNewPagesButton", viewMode) && 
								<Button
									variant='secondary'
									onClick={ (e) => { 
										e.preventDefault();
										e.target.blur();
										modalAddPages();
									} }
									disabled={ !CheckViewModePermission("canAddNewPages", viewMode) || transcriptionProcessingStatusDisabledButtons  || creationMode !== 'web-form'}
								>
									{t('transcription.details.addNewPages')}
								</Button>}
								{ CheckViewModePermission("canSeeAutoTranscribePagesButton", viewMode) && 
								<Button
									variant='secondary'
									onClick={ () => modalAutoTranscribePages() }
									disabled={ !(CheckViewModePermission("canAutoTranscribePages", viewMode) && overallTransciptionsNum > 0) || transcriptionProcessingStatusDisabledButtons }
								>
									{t('transcription.details.automaticallyTranscribe')}
								</Button>}
								{ CheckSecurityGroupPermission("canTrainOCR", securityGroup) && 
								<Button
									variant='secondary'
									disabled={ !(percentOfTranscribed > 0) && !(pages.count > 0) }
									onClick={ () => modalTrainOCR() }
								>
									{t('transcription.details.trainOCR')}
								</Button>}
								<Button
									variant='secondary'
									disabled={ !(percentOfTranscribed > 0) && !(pages.count > 0) }
									onClick={ (e) => {
										e.target.blur();
										modalDownloadResult();
									} }
								>{t('transcription.details.downloadResults')}
								</Button>
								{CheckViewModePermission("canSeeImportTextButton", viewMode) && 
								<Button
									variant='secondary'
									onClick={ (e) => { 
										e.target.blur();
										modalImportTextToLayer();
									} }
									disabled={ !(CheckViewModePermission("canImportText", viewMode) && overallTransciptionsNum > 0) || transcriptionProcessingStatusDisabledButtons }
								>
									{t('transcription.details.loadText')}
								</Button>}
							</div>
						</div>
					</div>
					<div className={ styles["transcription__mobileNav"] }>
						<Dropdown>
							<Dropdown.Toggle
								variant='dropdown-secondary'
								id='details'
							>
								<i className='icon-hamburger' /> {t('transcription.details.moreOptions')} <i className='icon-arrow-down-full' />
							</Dropdown.Toggle>
							<Dropdown.Menu className={ styles["transcription__mobileNav--dropdown-menu"] }>
								{(CheckViewModePermission("canSeeAddNewPagesButton", viewMode)) && 
								<Dropdown.Item 
									onClick={ (e) => { 
										e.preventDefault();
										e.target.blur();
										modalAddPages();
									} }
									disabled={ !CheckViewModePermission("canAddNewPages", viewMode) || transcriptionProcessingStatusDisabledButtons || creationMode !== 'web-form' }
								>
									{t('transcription.details.addNewPages')}
								</Dropdown.Item>}
								{CheckViewModePermission("canSeeAutoTranscribePagesButton", viewMode) && 
								<Dropdown.Item 
									onClick={ () => modalAutoTranscribePages() }
									disabled={ !(CheckViewModePermission("canAutoTranscribePages", viewMode) && overallTransciptionsNum > 0) || transcriptionProcessingStatusDisabledButtons }
								>
									{t('transcription.details.automaticallyTranscribe')}
								</Dropdown.Item>}
								<Dropdown.Item 
									onClick={ () => modalDownloadResult() }
									disabled={ !(percentOfTranscribed > 0) && !(pages.count > 0) }
								>
									{t('transcription.details.downloadResults')}
								</Dropdown.Item>
								{CheckViewModePermission("canSeeImportTextButton", viewMode) && 
								<Dropdown.Item 
									onClick={ (e) => { 
										e.target.blur();
										modalImportTextToLayer();
									} }
									disabled={ !(CheckViewModePermission("canImportText", viewMode) && overallTransciptionsNum > 0) || transcriptionProcessingStatusDisabledButtons }
								>
									{t('transcription.details.loadText')}
								</Dropdown.Item>}									
							</Dropdown.Menu>
						</Dropdown>
					</div>
					<h1 className={ styles["pages-list__header"] }>
						{t('transcription.details.transcriptionFiles')} ({overallTransciptionsNum})
					</h1>

					{(pagesList) && (pagesList.length > 0) && (
						<Pagination
							currentPage={ currentPageNum }
							pagesNum={ pagesNum }
							onGoToPage={ onGoToPage }
						>
							<div className={ `${styles["pages-list__grid"]}` }>
								{
									pagesList.map((page) => (
										<TranscriptionsItemPage
											key={ page.id }
											page={ page }
											viewMode={ viewMode }
											creationMode={ creationMode }
											isInTranscriptionDetails
											transcriptionId={ transcriptionId }
											currentPage={ currentPageNum }
											transcriptionProcessingStatusDisabledButtons={ transcriptionProcessingStatusDisabledButtons }
											layers={layers}
										/>
									))
								}
							</div>
						</Pagination>
					)}

					{(!pagesList || pagesList.length == 0) && (
						!transcriptionProcessingStatusDisabledButtons ? (
							<div className={ styles["pages-list__no-page"] }>
								{t('transcription.details.noPagesInfo1')} 
								<a href='#' onClick={ (e) => { 
									e.preventDefault();
									modalAddPages();
								} }
								>{t('transcription.details.noPagesInfo2')}
								</a>.
							</div>
						) : (
							<div className={ styles["pages-list__no-page"] }>
								{ t('transcription.details.noPagesInfoLoading') }
							</div>
						)
					)}
				</section>
			</Container>
		</div>
	);
};

TranscriptionDetailsPages.propTypes = {
	pages: PropTypes.object,
	percentOfTranscribed: PropTypes.number,
	lastModificationDate: PropTypes.string,
	onGoToPage: PropTypes.func,
	viewMode: PropTypes.string,
	creationMode: PropTypes.string,
	transcriptionId: PropTypes.number,
	transcriptionProcessingStatusDisabledButtons: PropTypes.bool,
	reloadPages: PropTypes.func,
	quota: PropTypes.object,
	continuation: PropTypes.object,
	usersCount: PropTypes.number,
	layers: PropTypes.array,
};

export default TranscriptionDetailsPages;
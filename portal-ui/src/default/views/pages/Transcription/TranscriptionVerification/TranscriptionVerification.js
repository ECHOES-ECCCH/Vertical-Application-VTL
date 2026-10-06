import React, { useState, useEffect } from "react";
import { useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useLocation, useHistory, useParams } from 'react-router';
import { Container } from 'react-bootstrap';
import { reverse } from 'named-urls';

import url from "@/router/urls";
import api from "@/services/api";
import { appActions } from '@/store/actions/app.actions';
import { CheckViewModePermission } from "@/permissions/ViewModes";

import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import Button from "@Elements/Button/Button";
import Error403 from "@Pages/Error/Error403";
import Pagination from '@Components/Pagination/Pagination';

import SearchTranscriptions from "@Elements/Search/SearchTranscriptions";
import SearchTypeEnum from "@/utils/SearchTypeEnum";
import TranscriptionBackLink from "@Pages/Transcription/parts/TranscriptionBackLink";

import styles from "@Pages/Transcription/TranscriptionVerification/TranscriptionVerification.module.sass";
import iconEdit from '@Assets/icons/svg/Icon_Edit.svg';

const TranscriptionVerification = () => {
	const { t } = useTranslation();

	const history = useHistory();
	const location = useLocation();

	const dispatch = useDispatch();
	const { transcriptionId } = useParams();
	const [ title, setTitle ] = useState('');
	const [ viewMode, setViewMode ] = useState('read-only');
	const [ verificationData, setVerificationData ]  = useState(null);
	const [ searchString, setSearchString ] = useState("");
	const [ useMetadata, setUseMetadata ] = useState(SearchTypeEnum.ALL.toString());
	const [ summarizeData, setSummarizeData] = useState(null);
	const [ currentPageNum, setCurrentPageNum ] = useState(1);
	const [ pagesNum, setPagesNum ] = useState(1);
	const [ currentLayer, setCurrentLayer ] = useState(0);
	const [ sortedColumn, setSortedColumn] = useState(0);
	const [ perPageCount, setPerPage] = useState(50);
	const [ sortBy, setSortBy] = useState('page-no');
	const [ sortDirection, setSortDirection] = useState('asc');
	
	useEffect(() => {
		getTranscriptionData();
	}, []);

	const getTranscriptionData = () => {
		dispatch(appActions.setLoading(true));

		api.get(`/transcriptions/${transcriptionId}`)
			.then(({data}) => {				
				setTitle(data.analyticInfo.title);
				setSummarizeData(data);

				setViewMode(data.viewMode);
				dispatch(appActions.setLoading(false));				
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
			});
	};

	useEffect(() => {
		if (currentPageNum === 0) return;
		dispatch(appActions.setLoading(true));

		api.get(`/transcriptions/${transcriptionId}/statistics`, { params: { page: currentPageNum, perPage: perPageCount, sortBy: sortBy, sortDirection: sortDirection } })
			.then(({data}) => {
				setVerificationData(data);
				setCurrentPageNum(data.paginatedPagesStatistics[currentLayer].page);
				setPagesNum(data.paginatedPagesStatistics[currentLayer].pages);
				if (currentPageNum > data.paginatedPagesStatistics[currentLayer].pages) {
					goPage(data.paginatedPagesStatistics[currentLayer].pages);
				}				
				dispatch(appActions.setLoading(false));				
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
			});
	}, [ currentPageNum, sortBy, sortDirection]);

	const goPage = (pageNum) => {
		const linkToTranscriptionVerification = reverse(url.transcription.verification, { transcriptionId: parseInt(transcriptionId) });
		history.push(linkToTranscriptionVerification + '?page=' + pageNum);
	};

	useEffect(() => {
		const pageNum = new URLSearchParams(location.search).get("page");
		if (!pageNum || pageNum < 1) {
			const linkToTranscriptionVerification = reverse(url.transcription.verification, { transcriptionId: parseInt(transcriptionId) });
			history.replace(linkToTranscriptionVerification + '?page=1');
		} else {
			setCurrentPageNum(parseInt(pageNum));
		}
	}, [ location ]);
    
	const breadcrumbsList = [
		{ id: 1, label: "home", link: url.home },
		{ id: 2, label:t('breadcrumbs.transcriptions'), link: url.transcriptions },
		{ id: 3, label: title, link: reverse(url.transcription.details, { transcriptionId: transcriptionId }) },
		{ id: 4, label:t('transcription.verification.transcriptionVerification') },
	];

	const onGoToPage = (pageNum, perPageCount) => {
		goPage(pageNum);
	};
	const sortColumn = (i) => {
		if(Math.abs(sortedColumn) == Math.abs(i))
		{
			if(sortedColumn < 0)
				i = 0;
			else
				i = -sortedColumn;
		}
		
		const compare = (a, b) => {
			let res = 0, vala = 0, valb = 0;
			switch(Math.abs(i))
			{
				case 0:
					vala = parseInt(a.pageNo);
					valb = parseInt(b.pageNo);
					break;
				case 2:
					vala = parseInt(a.versesInFile - a.versesTranscribed - a.versesMarkedAsVerified - a.versesMarkedAsIncorrect);
					valb = parseInt(b.versesInFile - b.versesTranscribed - b.versesMarkedAsVerified - b.versesMarkedAsIncorrect);
					break;	
				case 3:
					vala = parseInt(a.versesTranscribed);
					valb = parseInt(b.versesTranscribed);
					break;	
				case 4:
					vala = parseInt(a.versesMarkedAsVerified);
					valb = parseInt(b.versesMarkedAsVerified);
					break;					
				case 5:
					vala = parseInt(a.versesMarkedAsIncorrect);
					valb = parseInt(b.versesMarkedAsIncorrect);
					break;					
				default:
					vala = parseInt(a.pageNo);
					valb = parseInt(b.pageNo);
			}			
			return (i <= 0 ? 1 : -1) * (vala - valb);
		};

		setSortedColumn(i);
		if(pagesNum < 2)
		{
			let newData = Object.assign({}, verificationData);
			newData.paginatedPagesStatistics[currentLayer].pageStatisticsList.sort(compare);
			setVerificationData(newData);
		}
		else {
			if(i == 0)
			{
				setSortBy('page-no');
			}
			if(Math.abs(i) == 2)
			{
				setSortBy('in-transcription');
			}
			if(Math.abs(i) == 3)
			{
				setSortBy('to-accept');
			}
			if(Math.abs(i) == 4)
			{
				setSortBy('accepted');
			}
			if(Math.abs(i) == 5)
			{
				setSortBy('rejected');
			}
			setSortDirection(i <= 0 ? 'asc' : 'desc');
		}
	};
	
	const getColumnIcon = (i) => {
		if(i > 1 && i < 6)
		{
			let icon = sortedColumn == i ? 'icon-sort-up' : (sortedColumn == -i ? 'icon-sort-down' : 'icon-sort');
			return (
				<i
					className={icon}
					onClick={ () => sortColumn(i) }
				/>
			);
		}
		return null;
	};

	return (CheckViewModePermission("canOpenModerationScreen", viewMode)) ? (
		<main>
			<SearchTranscriptions
				setSearchString={ setSearchString }
				searchString={ searchString }
				setUseMetadata={ setUseMetadata }
			/>
			<Container className={ styles["verification--container"] }>
				<Breadcrumbs breadcrumbsList={ breadcrumbsList } />
				<TranscriptionBackLink transcriptionId={transcriptionId} />

				<section className={ styles["verification"] }>
					<div className={ styles["verification__title--container"] }>
						<h1 className={ styles["verification__title"] }>
							{t('transcription.verification.transcriptionVerification')}
						</h1>
					</div>
					<div className={styles["verification__status"]}>
						<div>
							{t('transcriptions.description.name')}<b>{summarizeData.titleLabel}</b>
						</div>
						<div>
							{t('transcriptions.description.projectAuthor')} <b>{summarizeData.creator.email}</b>
						</div>
					</div>
					<div className={styles["verification__status"]}>
						{t('transcriptions.description.verificationDesc')}
					</div>

					<div className={ styles["verification__advancement"] }>
						<span className={ styles["verification__advancement--label-main"] }>
							{t('transcription.verification.advancement.main')}
						</span>
						<div>
							{ summarizeData && verificationData && verificationData.layersStatistics.map((layer, index) => (
								<div
									key={index}
									className={ styles["verification__advancement--item"] }
								>
									<div className={ styles["verification__advancement--layer-name"] }>{summarizeData?.layers[index].name} </div>
									<div />
									<div>{t('transcription.verification.advancement.toVerify')}</div>
									<div className={ styles["verification__advancement--value"] }>{layer.allVerses - layer.versesTranscribed - layer.versesMarkedAsVerified - layer.versesMarkedAsIncorrect}/{layer.allVerses}</div>
									<div>{t('transcription.verification.advancement.verified')}</div>
									<div className={ styles["verification__advancement--value"] }>{layer.versesTranscribed}/{layer.allVerses}</div>
									<div>{t('transcription.verification.advancement.correct')}</div>
									<div className={ styles["verification__advancement--value"] }>{layer.versesMarkedAsVerified}/{layer.allVerses}</div>
									<div>{t('transcription.verification.advancement.incorrect')}</div>
									<div className={ styles["verification__advancement--value"] }>{layer.versesMarkedAsIncorrect}/{layer.allVerses}</div> 
								</div>
							))}
						</div>
					
					</div>
					<div className='clearfix' />
					<Pagination
						currentPage={ currentPageNum }
						pagesNum={ pagesNum }
						onGoToPage={ onGoToPage }
					>
						<div>
							<div
								id='verificatrionTabs'
								className={styles["tabs"]}
							>
								{ summarizeData && verificationData && verificationData.paginatedPagesStatistics.map((layer, index) => (
									<a
										className={`${styles["tabs__item"]} ${index == currentLayer ? styles["tabs__item--active"] : ""}`}
										key={index}
										href='#verificatrionTabs'
										onClick={(e) => { e.preventDefault(); return setCurrentLayer(index); } }
									>{t('transcriptions.layers.layer')} {index+1}
									</a>
								))}
							</div>
					
							<div className={ styles["verification__table"] }>
								<div className={ styles["verification__table--caption"] }>
									{t('transcriptions.layers.layerName')} : {summarizeData?.layers[currentLayer].name}
								</div>

								<div className={`${styles["verification__table--header"] } ${styles["verification__table--row"]}`}>
									{t('transcriptions.varification.columns', { returnObjects: true }).map((c, index) => (
										<div key={index}>{c} {getColumnIcon(index)}</div>
									))}
								</div>
								{ verificationData && verificationData.paginatedPagesStatistics[currentLayer].pageStatisticsList.map((page) => (
									<div key={page.pageNo} className={`${styles["verification__table--row"]} ${styles["verification__table--item"]}`}>
										<div>
											<span className={styles["verification__table--col-desc"]}>{t('transcriptions.varification.columns', { returnObjects: true })[0]}</span>
											{page.pageNo}
										</div>
										<div>
											<span className={styles["verification__table--col-desc"]}>{t('transcriptions.varification.columns', { returnObjects: true })[1]}</span>
											{page.versesInFile}
										</div>
										<div>
											<span className={styles["verification__table--col-desc"]}>{t('transcriptions.varification.columns', { returnObjects: true })[2]}</span>
											{page.versesInFile - page.versesTranscribed - page.versesMarkedAsVerified - page.versesMarkedAsIncorrect}
											{page.versesInFile != 0 ? " (" + Math.floor((page.versesInFile - page.versesTranscribed - page.versesMarkedAsVerified - page.versesMarkedAsIncorrect) * 100 / page.versesInFile) + "%)" : "" }
										</div>
										<div>
											<span className={styles["verification__table--col-desc"]}>{t('transcriptions.varification.columns', { returnObjects: true })[3]}</span>
											{page.versesTranscribed}
											{page.versesInFile != 0 ? " (" + Math.floor(page.versesTranscribed*100/page.versesInFile) + "%)" : "" }
										</div>
										<div>
											<span className={styles["verification__table--col-desc"]}>{t('transcriptions.varification.columns', { returnObjects: true })[4]}</span>
											{page.versesMarkedAsVerified}
											{page.versesInFile != 0 ? " (" + Math.floor(page.versesMarkedAsVerified*100/page.versesInFile) + "%)" : "" }
										</div>
										<div>
											<span className={styles["verification__table--col-desc"]}>{t('transcriptions.varification.columns', { returnObjects: true })[5]}</span>
											{page.versesMarkedAsIncorrect}
											{page.versesInFile != 0 ? " (" + Math.floor(page.versesMarkedAsIncorrect*100/page.versesInFile) + "%)" : "" }
										</div>
										<div>
											<span className={styles["verification__table--col-desc"]}>{t('transcriptions.varification.columns', { returnObjects: true })[6]}</span>
											{page.lastModificationDate} {page.lastModifier}
										</div>
										<div className={styles["verification__table--item__buttons"]} >
											<Button
												variant='secondary'
												className={styles["verification__table--item__button"]}
												onClick={() => history.push(`/transcription/${transcriptionId}/page/${page.pageId}`)}
											>
												<img
													src={iconEdit}
													alt={t('transcriptions.varification.goToVerification')}
												/>

												<span>{t('transcriptions.varification.goToVerification')}</span>
											</Button>
										</div>
									</div>
								))}
							</div>
						</div>
					</Pagination>
				</section>
			</Container>

		</main>
	) : (
		<Error403 />
	);
};

export default TranscriptionVerification;

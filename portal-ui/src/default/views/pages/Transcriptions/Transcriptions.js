import React, { useState, useEffect, useCallback } from 'react';
import { useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useLocation, useHistory } from 'react-router';
import { Container } from 'react-bootstrap';

import api from "@/services/api";
import url from "@/router/urls";

import { appActions } from "@/store/actions/app.actions";
import Button from "@Elements/Button/Button";

import Pagination from '@Components/Pagination/Pagination';
import TranscriptionItem from '@Elements/Transcriptions/TranscriptionsItem';
import TranscriptionsSortSelect from '@Elements/Transcriptions/TranscriptionsSortSelect';
import TranscriptionsMyScopeSelect from '@Elements/Transcriptions/TranscriptionsMyScopeSelect';
import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import SearchTranscriptions from "@Elements/Search/SearchTranscriptions";

import styles from "@Pages/Transcriptions/Transcriptions.module.sass";
import SearchTypeEnum from "@/utils/SearchTypeEnum";
import NotFound from '@Assets/icons/svg/NotFound.svg';

const Transcriptions = () => {
	const { t } = useTranslation();

	const location = useLocation();
	const history = useHistory();
	const dispatch = useDispatch();

	const listUserTranscriptions = url.transcriptionsUser === location.pathname;
	const [initialQueryLoaded, setInitialQueryLoaded] = useState(false);
	const [currentPageNum, setCurrentPageNum] = useState(null);
	const [perPage, setPerPage] = useState(0);
	const [pagesNum, setPagesNum] = useState(null);
	const [overallTransciptionsNum, setOverallTransciptionsNum] = useState(0);
	const [transcriptions, setTranscriptions] = useState({});
	const [matchesPerTranscription, setMatchesPerTranscription] = useState([]);
	const [metadataHits, setMetadataHits] = useState([]);
	const [pages, setPages] = useState([]);

	const [searchString, setSearchString] = useState("");
	const [scope, setScope] = useState(null);
	const [visibility, setVisibility] = useState(null);
	const [order, setOrder] = useState(null);
	const [noUserTranscriptionsMsg, setNoUserTranscriptionsMsg] = useState(null);

	const [useMetadata, setUseMetadata] = useState(SearchTypeEnum.ALL.toString());
	
	const loadData = useCallback((myParams) => {
		const apiAddress = (listUserTranscriptions) ? '/my-transcriptions' : (myParams.q.length > 0 ? '/transcriptions/search' : '/transcriptions');
		dispatch(appActions.setLoading(true));
		api.get(apiAddress, { params: myParams })
			.then(({ data }) => {
				setNoUserTranscriptionsMsg(data.pages > 0 || !listUserTranscriptions ? null
					: myParams.scope === 'creator' ? t('transcriptions.search.userHasNoTranscriptionsCreator')
						: myParams.scope === 'member' ? t('transcriptions.search.userHasNoTranscriptionsMember')
							: t('transcriptions.search.userHasNoTranscriptions'));
				if (data.pages === 0) {
					dispatch(appActions.setLoading(false));
					setPagesNum(0);
					setOverallTransciptionsNum(0);
					return;
				}
				if (myParams.page > parseInt(data.pages)) {
					setCurrentPageNum(parseInt(data.pages));
				} else {
					dispatch(appActions.setLoading(false));
					setOverallTransciptionsNum(data.hits);
					setPagesNum(data.pages);
					if (listUserTranscriptions) {
						setTranscriptions(data.transcriptions);
					} else {
						myParams.q.length > 0 && setMetadataHits(data.transcriptions.map(item => item.metadataHits));
						myParams.q.length > 0 && setMatchesPerTranscription(data.transcriptions.map(element => element.pagesWithHitCounts.map(item => item.hitCount)));
						setTranscriptions(myParams.q.length > 0 ? data.transcriptions.map(element => element.transcription) : data.transcriptions);

						myParams.q.length > 0 && setPages(data.transcriptions.map(element => element.pagesWithHitCounts));
					}
				}
			});
	}, [history]);

	useEffect(() => {
		const usp = new URLSearchParams(location.search);
		setCurrentPageNum(parseInt(usp.get("page")) || 1);
		setPerPage(parseInt(usp.get("perpage")) || parseInt(localStorage.getItem("perPage")) || 90);
		setUseMetadata(usp.get("type") || SearchTypeEnum.ALL.toString());
		setScope(usp.get("scope"));
		setVisibility(usp.get("visibility"));
		const q = usp.get("q") || '';
		setSearchString(q);
		
		let urlOrder = usp.get("order") || 'last-modification-date';
		if (urlOrder === 'hit-count' && q === '') {
			urlOrder = 'last-modification-date';
		}
		setOrder(urlOrder);
		setInitialQueryLoaded(true);
	}, []);

	useEffect(() => {
		const usp = new URLSearchParams(location.search);
		usp.set('page', currentPageNum);
		usp.set('perpage', perPage);
		usp.set('type', useMetadata);
		usp.set('order', order);
		scope && usp.set('scope', scope);
		visibility && usp.set('visibility', visibility);
		searchString && usp.set('q', searchString);
		history.push(`?${usp.toString()}${location.hash}`);
	}, [ searchString, order, scope, visibility, currentPageNum, perPage, useMetadata, location.search, location.hash]);

	useEffect(() => {
		if (initialQueryLoaded) {
			const myParams = {
				page: currentPageNum,
				perpage: perPage,
				q: searchString,
				order: order,
				scope: scope,
				visibility: visibility,
			};
			if (!listUserTranscriptions) {
				myParams.type = useMetadata;
			}
			loadData(myParams);
		}
	}, [loadData, listUserTranscriptions,
		searchString, order, scope, visibility, currentPageNum, perPage, initialQueryLoaded]);

	const onGoToPage = (pageNum, perPageCount) => {
		if (perPageCount) {
			setPerPage(perPageCount);
			localStorage.setItem("perPage", perPageCount);
		}
		setCurrentPageNum(pageNum);
	};

	const breadcrumbsList = () => {
		if (listUserTranscriptions) {
			return (
				[
					{ id: 1, label: "home", link: url.home },
					{ id: 2, label: t('breadcrumbs.account'), link: url.auth.account },
					{ id: 3, label: t('breadcrumbs.transcriptionsUser') },
				]
			);
		} else if (searchString) {
			return (
				[
					{ id: 1, label: "home", link: url.home },
					{ id: 2, label: t('breadcrumbs.searchResults') },
				]
			);
		} else {
			return (
				[
					{ id: 1, label: "home", link: url.home },
					{ id: 2, label: t('breadcrumbs.transcriptions') },
				]
			);
		}
	};

	return (
		<main className={styles["transcriptions"]}>
			<SearchTranscriptions
				setSearchString={setSearchString}
				searchString={searchString}
				setUseMetadata={setUseMetadata}
			/>
			<Container>
				<Breadcrumbs breadcrumbsList={breadcrumbsList()} />
			</Container>
			<Container>
				<section
					className={(!listUserTranscriptions) ? styles["transcriptions__header"] : styles["transcriptions__header--columns"]}
				>
					<h1 className={styles["transcriptions__header--title"]}>
						{(!searchString) ? (
							<>
								{listUserTranscriptions ? t('transcriptions.header.titleMy') : t('transcriptions.header.title')}
								<span className={styles["transcriptions__header--transcr-number"]}> ({overallTransciptionsNum})</span>
							</>
						) :
							<>
								{t('transcriptions.header.searchResultsText')} {searchString} ({overallTransciptionsNum})
							</>}
					</h1>

					{(listUserTranscriptions) &&
						<Button
							variant='primary'
							className={styles["transcr-item__button"]}
							href={url.transcription.new}
						>
							{t('transcriptions.header.createNew')}
						</Button>}
				</section>
			</Container>
			<section className={(pagesNum > 1) ? styles["transcriptions__pagination-wrapper"] : styles["transcriptions__no-pagination-wrapper"]}>
				{pagesNum === 0 ? (
					noUserTranscriptionsMsg ? (
						<div className='container'>
							<div className='pagination-top'>
								<TranscriptionsMyScopeSelect
									setScope={setScope}
									scope={scope}
									setVisibility={setVisibility}
									visibility={visibility}
								/>
							</div>
							<div className={styles["transcriptions__error"]}>
								{noUserTranscriptionsMsg}
							</div>
						</div>
					) : (
						<div className={styles["transcriptions__error"] + " constainer"}>
							<div>
								<img
									src={NotFound}
									alt={t('transcriptions.search.noTranscriptionsFound')}
								/>
							</div>
							<div>
								{t('transcriptions.search.noTranscriptionsFound')}<br />
								<small>{t('transcriptions.search.description')}</small>
							</div>
						</div>
					)

				) : (
					<div className={styles.container}>
						<Pagination
							className='container'
							leftTopChildren={
								<div className='pagination-top'>
									{(listUserTranscriptions) &&
										<TranscriptionsMyScopeSelect
											setScope={setScope}
											scope={scope}
											setVisibility={setVisibility}
											visibility={visibility}
										/>}

									<TranscriptionsSortSelect
										setOrder={setOrder}
										order={order}
										isUserTranscriptions={listUserTranscriptions}
										isTextSearch={searchString !== ''}
									/>
								</div>
							}
							currentPage={currentPageNum}
							pagesNum={pagesNum}
							onGoToPage={onGoToPage}
							perPage={perPage}
							title={t('transcriptions.paging.title')}
							showPerPage
						>
							<div className={styles["transcriptions__transcr-container"]}>
								{

									(transcriptions.length > 0) &&
									transcriptions.map((item, index) => {
										return (
											<TranscriptionItem
												isSearchView
												matchesPerTranscription={matchesPerTranscription[index]}
												transcription={item}
												metadataHits={metadataHits[index]}
												key={item.analyticInfo.title + index.toString()}
												isUserTranscriptions={listUserTranscriptions}
												useMetadata={useMetadata}
												pagesToUse={searchString !== "" ? pages[index] : []}
												searchString={searchString}
											/>
										);
									})
								}
							</div>
						</Pagination>
					</div>
				)}
			</section>
		</main>
	);
};
export default Transcriptions;

import React, { useEffect, useState, useCallback } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useParams, useLocation, useHistory } from 'react-router';
import { Container } from "react-bootstrap";
import { reverse } from 'named-urls';

import { appActions } from "@/store/actions/app.actions";

import api from "@/services/api";
import url from "@/router/urls";

import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import Pagination from '@Components/Pagination/Pagination';
import { useShowModal } from '@Elements/Modal/ModalHooks';

import AdminTranscriptionItem from '@Pages/Admin/UserTranscriptions/parts/AdminTranscriptionItem';
import SearchTranscriptions from "@Elements/Search/SearchTranscriptions";
import SearchTypeEnum from "@/utils/SearchTypeEnum";

import styles from "@Pages/Admin/UserTranscriptions/UserTranscriptions.module.sass";

const UserTranscriptions = () => {
	const { t } = useTranslation();

	const dispatch = useDispatch();
	const location = useLocation();
	const history = useHistory();
	const { id: userId } = useParams();

	const [searchTranscriptionsString, setSearchTranscriptionsString] = useState("");

	const [ apiData, setApiData ] = useState(null);
	const [ currentPageNum, setCurrentPageNum ] = useState(null);
	const [ searchString, setSearchString ] = useState(null);
	const [ currentSearchedString, setCurrentSearchedString ] = useState("");
	const [ initialLoaded, setInitialLoaded ] = useState(false);
	const [ sortDirection, setSortDirection ] = useState('ASC');

	const [useMetadata, setUseMetadata] = useState(SearchTypeEnum.ALL.toString());

	const modalConfirmInfo = useSelector((state) => state.app.modalConfirmInfo);

	const loadData = useCallback((endpoint, params) => {
		dispatch(appActions.setLoading(true));
		api.get(endpoint, { params })
			.then(({ data }) => {
				if ((parseInt(params.page) > parseInt(data.pages)) && (data.pages !== 0)) {
					history.replace(`?${params.q ? `&q=${params.q}&` : ''}page=${data.pages}`);
				}
				else {
					dispatch(appActions.setLoading(false));
					setInitialLoaded(true);
					setApiData(data);
				}
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
			});
	}, [ dispatch, history ]);

	useEffect(() => {
		const usp = new URLSearchParams(location.search);
		let pageNum = parseInt(usp.get("page"));
		const query = usp.get("q");
		setCurrentSearchedString(query ? encodeURIComponent(query) : "");
		if (!pageNum || pageNum < 1) {
			if (query && query.length > 1) {
				history.replace(`?q=${query}&page=1`);
			} else {
				history.replace(`?page=1`);
			}
		} else {
			setSearchString(query);
			setCurrentPageNum(pageNum);
			loadData('/user-transcriptions/list', {
				ownerId: userId,
				page: pageNum,
				q: ((query && query.length > 1) ? query : null),
				perpage: 8,
				order: "title",
				"order-dir": sortDirection,
			});
		}
	}, [ location, history, loadData, userId, sortDirection ]);

	useEffect(() => {
		if (initialLoaded) {
			history.push(`?${searchString ? `&q=${searchString}&` : ''}page=${currentPageNum}`);
		}
	}, [ currentPageNum, searchString, history ]);

	const openTranscription = (transcriptionId) => {
		history.push(reverse(url.transcription.details, { transcriptionId: transcriptionId }));
	};

	const [ transcriptionToDeleteId, setTranscriptionToDeleteId ] = useState(null);
	const modalDeleteTranscription = useCallback(useShowModal({
		title:t('modals.deleteTranscription.title'),
		name: 'deleteGroup',
		componentPath: './parts/ModalDeleteTranscriptionCallback',
	}), [ useShowModal ]);

	useEffect(() => {
		switch (modalConfirmInfo) {
			case "TRANSCRIPTION_DELETE_CONFIRMED":
				api.delete(`/transcriptions/${transcriptionToDeleteId}`)
					.then(() => {
						loadData('/user-transcriptions/list', {
							ownerId: userId,
							page: currentPageNum,
							q: ((searchString && searchString.length > 1) ? searchString : null),
							perpage: 8,
							order: "title",
							"order-dir": sortDirection,
						});
					});
				break;
			default:
				break;
		}
		dispatch(appActions.setModalConfirmInfo(null));
		setTranscriptionToDeleteId(null);
	}, [ modalConfirmInfo, currentPageNum, dispatch, loadData, searchString, userId ]);

	const getColumnIcon = () => {
		let icon = sortDirection == 'ASC' ? 'icon-sort-up' : (sortDirection == 'DESC' ? 'icon-sort-down' : 'icon-sort');
		return (
			<i
				className={icon}
				onClick={ () => { if(sortDirection == 'ASC'){ setSortDirection('DESC'); } else { setSortDirection('ASC'); } } }
			/>
		);
	};

	const deleteTranscription = (transcriptionId) => {
		setTranscriptionToDeleteId(transcriptionId);
	};

	useEffect(() => {
		if (transcriptionToDeleteId) {
			modalDeleteTranscription();
		}
	}, [ transcriptionToDeleteId, modalDeleteTranscription ]);

	const breadcrumbsList = [
		{ id: 1, label: "home", link: url.home },
		{ id: 2, label:t('breadcrumbs.account'), link: url.auth.account },
		{ id: 3, label:t('breadcrumbs.adminConsole'), link: url.auth.admin.users },
		{ id: 4, label:t('breadcrumbs.adminUserTranscriptions') },
	];

	return (
		<main>
			<SearchTranscriptions
				setSearchString={setSearchTranscriptionsString}
				searchString={searchTranscriptionsString}
				setUseMetadata={setUseMetadata}
			/>
			<Container>
				<Breadcrumbs breadcrumbsList={ breadcrumbsList } />
				<div className='block__header'>
					<h1 className='text--title'>
						{apiData &&
							<div>
								{t('admin.userTranscriptions.listDescription')}
								<strong>
									{(apiData && apiData.ownerEmail) ? apiData.ownerEmail : null}
								</strong>
								<span> ({apiData.transcriptions.length})</span>
							</div>}
					</h1>
				</div>
				<div className={ styles["transcriptions__header"] }>
					<p>{t('admin.userTranscriptions.description')}</p>
				</div>
				<div className={styles["transcriptions--search-input"]}>
					<i className='icon-search' />
					<input
						type='text'
						placeholder={t('admin.userTranscriptions.searchForProject')}
						value={currentSearchedString}
						onChange={e => {
							setCurrentSearchedString(e.target.value);
						}}
						onKeyDown={e => {
							if (e.key === 'Enter') {
								setSearchString(currentSearchedString);
							}
						}}
					/>
				</div>

				<section className={ (apiData && apiData.pages > 1) ? styles["transcription-list__pagination-wrapper"] : styles["transcription-list__no-pagination-wrapper"] }>
					{(apiData) && (
						<Pagination
							currentPage={ apiData.page }
							pagesNum={ apiData.pages }
							onGoToPage={ setCurrentPageNum }
						>
							<div className={ styles["transcription-list__wrapper"] }>
								<div className={styles["transcription-list__item-header"]}>
									<div className={styles["transcription-list__item--order-header"]}>
										{t('admin.userTranscriptions.no')}
									</div>
									<div className={styles["transcription-list__item--project-header"]}>
										{t('admin.userTranscriptions.projectName')}
										{getColumnIcon()}
									</div>
									<div className={styles["transcription-list__item--authors-header"]}>
										{t('admin.userTranscriptions.members')}
									</div>
									<div className={styles["transcription-list__item--buttons"]} />
								</div>
								{apiData.transcriptions &&
									apiData.transcriptions.map((item, index) => (
										<AdminTranscriptionItem
											key={ item.analyticInfo.title + item.id }
											owner={ apiData.ownerEmail }
											id={ item.id }
											orderNum={ index + ((apiData.page - 1) * apiData.perpage) }
											title={ item.analyticInfo.title }
											authors={ item.users }
											openTranscription={ openTranscription }
											deleteTranscription={ deleteTranscription }
										/>
									))}
							</div>
						</Pagination>
					)}
				</section>
			</Container>
		</main>
	);
};

export default UserTranscriptions;
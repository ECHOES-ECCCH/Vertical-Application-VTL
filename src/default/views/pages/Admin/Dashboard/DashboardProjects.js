import React, { useEffect, useState, useCallback } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { useLocation, useHistory } from "react-router";
import { Container } from "react-bootstrap";

import { appActions } from "@/store/actions/app.actions";
import { CheckSecurityGroupPermission } from "@/permissions/SecurityGroups";

import api from "@/services/api";
import url from "@/router/urls";

import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import Pagination from "@Components/Pagination/Pagination";

import ProjectItem from "@Pages/Admin/Dashboard/parts/ProjectItem";
import SearchTranscriptions from "@Elements/Search/SearchTranscriptions";
import SearchTypeEnum from "@/utils/SearchTypeEnum";

import styles from "@Pages/Admin/Dashboard/Dashboard.module.sass";

const Dashboard = () => {
	const { t } = useTranslation();

	const dispatch = useDispatch();
	const location = useLocation();
	const history = useHistory();

	const userType = useSelector((state) => state.auth.credentials.securityGroup);

	const [searchTranscriptionsString, setSearchTranscriptionsString] = useState("");

	const [apiData, setApiData] = useState({});
	const [currentPageNum, setCurrentPageNum] = useState(null);
	const [searchString, setSearchString] = useState(null);
	const [currentSearchedString, setCurrentSearchedString] = useState("");
	const [initialLoaded, setInitialLoaded] = useState(false);
	const [sortBy, setSortBy] = useState('title');
	const [sortDirection, setSortDirection] = useState('ASC');

	const [useMetadata, setUseMetadata] = useState(SearchTypeEnum.ALL.toString());

	const loadData = useCallback(
		(endpoint, params) => {
			dispatch(appActions.setLoading(true));
			api.get(endpoint, { params })
				.then(({ data }) => {
					if (parseInt(params.page) > parseInt(data.pages) && data.pages !== 0) {
						history.replace(`?${params.q ? `&q=${params.q}&` : ""}page=${data.pages}`);
					} else {
						dispatch(appActions.setLoading(false));
						setInitialLoaded(true);
						setApiData(data);
					}
				})
				.catch(() => {
					dispatch(appActions.setLoading(false));
				});
		},
		[dispatch, history],
	);

	const onQuotaChange = (id, quota) => {
		api.put(`/transcriptions/${id}/quota?quota=${quota}`);
	};

	const getColumnIcon = (column) => {
		let icon = column == sortBy ? (sortDirection == 'ASC' ? 'icon-sort-up' : (sortDirection == 'DESC' ? 'icon-sort-down' : 'icon-sort')) : 'icon-sort';
		return (
			<i
				className={icon}
				onClick={ () => { 
					setSortBy(column);
					if(sortDirection == 'ASC'){ 
						setSortDirection('DESC'); 
					} else { 
						setSortDirection('ASC'); 
					} 
				} }
			/>
		);
	};

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
			setSearchString(query ? encodeURIComponent(query) : "");
			setCurrentPageNum(pageNum);
			loadData("/transcriptions/list", {
				page: pageNum,
				q: query && query.length > 1 ? query : null,
				perpage: 50,
				order: sortBy,
				"order-dir": sortDirection,
			});
		}
	}, [location, history, sortBy, sortDirection, loadData]);

	useEffect(() => {
		if (initialLoaded) {
			history.push(`?${searchString ? `&q=${searchString}&` : ""}page=${currentPageNum}`);
		}
	}, [currentPageNum, searchString, initialLoaded]);

	const breadcrumbsList = [
		{ id: 1, label:t('breadcrumbs.home'), link: url.home },
		{ id: 2, label:t('breadcrumbs.account'), link: url.auth.account },
		{ id: 3, label:t('breadcrumbs.adminConsole') },
	];

	return (
		<main>
			<SearchTranscriptions
				setSearchString={setSearchTranscriptionsString}
				searchString={searchTranscriptionsString}
				setUseMetadata={setUseMetadata}
			/>
			<Container>
				<Breadcrumbs breadcrumbsList={breadcrumbsList} />

				<div className='block__header'>
					<h1 className='text--title'>{t('admin.dashboard.title')}</h1>
				</div>
				<div className={styles["userlist__block--top"]}>
					<p>{t('admin.dashboard.descriptionProject')}</p>
				</div>

				<section
					className={
						apiData.pages > 1
							? styles["userlist__pagination-wrapper"]
							: styles["userlist__no-pagination-wrapper"]
					}
				>
					<>
						<div className={styles["dashboard--search-input"]}>
							<i className='icon-search' />
							<input
								type='text'
								placeholder={t('admin.dashboard.searchPlaceholder')}
								value={decodeURIComponent(currentSearchedString)}
								onChange={e => {
									setCurrentSearchedString(encodeURIComponent(e.target.value));
								}}
								onKeyDown={e => {
									if (e.key === 'Enter') {
										setSearchString(currentSearchedString);
									}
								}}
							/>
						</div>
						<Pagination
							currentPage={apiData.page}
							pagesNum={apiData.pages}
							onGoToPage={setCurrentPageNum}
							leftTopChildren={
								<>
									<a className={`${styles["tabs__item"]} ${styles["tabs__item--active"]}`} href='#'>
										{t('admin.dashboard.projects')}
									</a>
									<a className={styles["tabs__item"]} href={url.auth.admin.users}>
										{t('admin.dashboard.users')}
									</a>
								</>
							}
						>
							<div className={styles["userlist__wrapper"]}>
								<div className={styles["userlist__item-header"] + " " + styles["userlist__project-item"]}>
									<div className={styles["userlist__item--order-header"]}>{t('admin.dashboard.no')}</div>
									<div className={styles["userlist__item--project-header"]}>
										{t('admin.dashboard.project')}
										{getColumnIcon("title")}
									</div>
									<div className={styles["userlist__item--email-header"]}>
										{t('admin.dashboard.superadministrator')}
										{getColumnIcon("email")}
									</div>
									<div className={styles["userlist__item--used-header"]}>{t('admin.dashboard.usedHeader')}</div>
									<div className={styles["userlist__item--limit"]}>
										{t('admin.dashboard.projectLimits')}
									</div>
								</div>
								{apiData.transcriptions &&
									apiData.transcriptions.map((p, index) => (
										<ProjectItem
											key={p.id}
											id={p.id}
											readOnly={!CheckSecurityGroupPermission("canChangeProjectLimit", userType)}
											orderNum={index + (apiData.page - 1) * apiData.perpage}
											email={p.creator.email}
											title={p.analyticInfo.title}
											quota={p.quota}
											usedQuota={p.userSpaceQuota}
											onQuotaChange={onQuotaChange}
										/>
									))}
							</div>
						</Pagination>
					</>
				</section>
			</Container>
		</main>
	);
};

export default Dashboard;

import React, { useState, useEffect } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { useParams } from "react-router";
import { reverse } from "named-urls";
import { useHistory } from "react-router-dom";
import { Container } from "react-bootstrap";

import api from "@/services/api";
import url from "@/router/urls";

import { appActions } from "@/store/actions/app.actions";

import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";

import TranscriptionPermissionsUsers from "@Pages/Transcription/TranscriptionPermissions/parts/TranscriptionPermissionsUsers";
import TranscriptionPermissionsProjectInfo from "@Pages/Transcription/TranscriptionPermissions/parts/TranscriptionPermissionsProjectInfo";
import TranscriptionPermissionsGroups from "@Pages/Transcription/TranscriptionPermissions/parts/TranscriptionPermissionsGroups";
import TranscriptionBackLink from "@Pages/Transcription/parts/TranscriptionBackLink";

import SearchTranscriptions from "@Elements/Search/SearchTranscriptions";
import SearchTypeEnum from "@/utils/SearchTypeEnum";

import styles from "@Pages/Transcription/TranscriptionPermissions/TranscriptionPermissions.module.sass";

const TranscriptionPermissions = () => {
	const dispatch = useDispatch();
	const { t } = useTranslation();

	const isLoggedIn = useSelector(state => state.auth.credentials.isLoggedIn);
	const isLoggingEnd = useSelector(state => state.auth.credentials.isLoggingEnd);
	const { transcriptionId } = useParams();
	const history = useHistory();

	const [viewMode, setViewMode] = useState(null);
	const [title, setTitle] = useState("");
	const [creator, setCreator] = useState("");
	const [allowedGroups, setAllowedGroups] = useState();
	const [allowedUsers, setAllowedUsers] = useState();
	const [dataLoaded, setDataLoaded] = useState(false);
	const [continuation, setContinuation] = useState(null);
	const [publicAvailable, setPublicAvailable] = useState(true);
	const [summarizeData, setSummarizeData] = useState([]);
	const [ searchString, setSearchString ] = useState("");
	const [ useMetadata, setUseMetadata ] = useState(SearchTypeEnum.ALL.toString());

	const linkToTranscriptionDetails = reverse(url.transcription.details, { transcriptionId: transcriptionId });

	useEffect(() => {
		if (isLoggedIn && isLoggingEnd) {
			reloadData();
		} else if (!isLoggedIn && isLoggingEnd) {
			history.push(linkToTranscriptionDetails);
		}
	}, [isLoggedIn, isLoggingEnd]);

	const reloadData = () => {
		dispatch(appActions.setLoading(true));
		getTranscriptionPermissionsData();
		getTranscriptionSummarizeData();
	};

	const getTranscriptionPermissionsData = () => {
		api.get(`/transcriptions/${transcriptionId}/permissions`)
			.then(({ data }) => {
				setViewMode(data.viewMode);
				setTitle(data.transcription.analyticInfo.title);
				setCreator(data.transcription.creator.email);
				setAllowedGroups(data.allowedGroups);

				const sortedUsersByCreator = data.allowedUsers.filter(
					user => user.user.email === data.transcription.creator.email,
				);
				sortedUsersByCreator.push(
					...data.allowedUsers.filter(user => user.user.email !== data.transcription.creator.email),
				);
				setAllowedUsers(sortedUsersByCreator);
				setContinuation(data.transcriptionContinuation);
				setPublicAvailable(data.publiclyAvailable);
				setDataLoaded(true);
			})
			.catch(() => {
				history.push(linkToTranscriptionDetails);
			});
	};

	const getTranscriptionSummarizeData = () => {
		api.get(`/transcriptions/${transcriptionId}/summarize`).then(({ data }) => {
			setSummarizeData(data);
			dispatch(appActions.setLoading(false));
		});
	};

	const breadcrumbsList = [
		{ id: 1, label: "home", link: url.home },
		{ id: 2, label:t('breadcrumbs.transcriptions'), link: url.transcriptions },
		{ id: 3, label: title, link: linkToTranscriptionDetails },
		{ id: 4, label:t('breadcrumbs.contributors') },
	];

	return (
		dataLoaded && (
			<main>
				<SearchTranscriptions
					setSearchString={ setSearchString }
					searchString={ searchString }
					setUseMetadata={ setUseMetadata }
				/>
				<Container>
					<Breadcrumbs breadcrumbsList={breadcrumbsList} />
					<TranscriptionBackLink transcriptionId={transcriptionId} />

					<section className={styles["permissions"]}>
						<div>
							<h1 className={styles["permissions__title"]}>{t('transcription.permissions.title')}</h1>
						</div>

						<div className={`${styles["permissions__main"]} permissions`}>
							<TranscriptionPermissionsProjectInfo
								title={title}
								creator={creator}
							/>
							<TranscriptionPermissionsUsers
								users={allowedUsers}
								creator={creator}
								getTranscriptionPermissionsData={reloadData}
								summarizeData={summarizeData}
								viewMode={viewMode}
							/>
							<TranscriptionPermissionsGroups
								groups={ allowedGroups }
								getTranscriptionPermissionsData={ getTranscriptionPermissionsData }
								viewMode={viewMode}
							/>
						</div>
					</section>
				</Container>
			</main>
		)
	);
};

export default TranscriptionPermissions;

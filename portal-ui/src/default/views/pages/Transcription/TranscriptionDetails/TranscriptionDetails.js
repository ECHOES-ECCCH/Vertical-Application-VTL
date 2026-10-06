import React, { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useLocation, useHistory, useParams } from 'react-router';
import { reverse } from 'named-urls';

import api from "@/services/api";
import url from "@/router/urls";
import { appActions } from "@/store/actions/app.actions";
import { alertActions } from "@/store/actions/alert.actions";

import { Container } from 'react-bootstrap';

import Error403 from "@Pages/Error/Error403";
import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import TranscriptionDetailsPages from "@Pages/Transcription/TranscriptionDetails/parts/TranscriptionDetailsPages";
import TranscriptionDetailsMetadata from "@Pages/Transcription/TranscriptionDetails/parts/TranscriptionDetailsMetadata";
import TranscriptionDetailsNav from "@Pages/Transcription/TranscriptionDetails/parts/TranscriptionDetailsNav";
import getTranscriptionProcessingStatus from '@/utils/transcriptionProcessingStatus';

const TranscriptionDetails = () => {
	const dispatch = useDispatch();
	const { t } = useTranslation();

	const isLoggedIn = useSelector(state => state.auth.credentials.isLoggedIn);

	const { transcriptionId } = useParams();

	const location = useLocation();
	const history = useHistory();

	const [ metadata, setMetadata ] = useState(null);
	const [ currentPageNum, setCurrentPageNum ] = useState(0);
	const [ pages, setPages ] = useState({});
	const [ layers, setLayers ] = useState([]);
	const [ viewMode, setViewMode ] = useState(null);
	const [ creationMode, setCreationMode ] = useState(null);
	const [ hidden, setHidden ] = useState(false);

	const [ transcriptionInfo, setTranscriptionInfo ] = useState(null);
	const [ percentOfTranscribed, setPercentOfTranscribed ] = useState(null);
	const [ percentOfVerified, setPercentOfVerified ] = useState(null);
	const [ lastModificationDate, setLastModificationDate ] = useState("");
	const [ creationDate, setCreationDate ] = useState("");
	const [ receiverId, setReceiverId ] = useState(null);
	const [ creatorEmail, setCreatorEmail ] = useState("");

	const [ title, setTitle ] = useState('');
	const [ noAccess, setNoAccess ] = useState(false);
	const [ continuation, setContinuation ] = useState(null);

	const [ transcriptionProcessingStatus, setTranscriptionProcessingStatus ] = useState(null);
	const [ transcriptionProcessingStatusPrevious, setTranscriptionProcessingStatusPrevious ] = useState('');
	const [ transcriptionProcessingStatusRequest, setTranscriptionProcessingStatusRequest ] = useState(false);
	const [ transcriptionProcessingStatusDisabledButtons, setTranscriptionProcessingStatusDisabledButtons ] = useState(true);

	const [ quota, setQuota ] = useState({});
	const [ usersCount, setUsersCount] = useState(0);

	const goPage = (pageNum) => {
		const linkToTranscriptionDetails = reverse(url.transcription.details, { transcriptionId: parseInt(transcriptionId) });
		history.push(linkToTranscriptionDetails + '?page=' + pageNum);
	};

	const acknowledgeStatus = () => {
		api.put(`/transcriptions/${transcriptionId}/processing-status-acknowledge`);
	};

	useEffect(() => {
		const pageNum = new URLSearchParams(location.search).get("page");
		if (!pageNum || pageNum < 1) {
			const linkToTranscriptionDetails = reverse(url.transcription.details, { transcriptionId: parseInt(transcriptionId) });
			history.replace(linkToTranscriptionDetails + '?page=1');
		} else {
			setCurrentPageNum(parseInt(pageNum));
		}
	}, [ location ]);

	const reloadPages = () => {
		if (currentPageNum === 0) return;
		dispatch(appActions.setLoading(true));
		api.get(`/transcriptions/${transcriptionId}`, { params: { page: currentPageNum } })
			.then((response) => {
				dispatch(appActions.setLoading(false));
				if (currentPageNum > response.data.pages.pages) {
					goPage(response.data.pages.pages);
				}
				setTranscriptionInfo(response.data.transcriptionInfo);
				setTitle(response.data.analyticInfo.title);
				setLayers(response.data.layers);
				setPages(response.data.pages);
				setViewMode(response.data.viewMode.toLowerCase());
				setCreationMode(response.data.creationMode.toLowerCase());
				setMetadata(response.data);
				setPercentOfTranscribed(response.data.percentOfTranscribed);
				setPercentOfVerified(response.data.percentOfVerified);
				setLastModificationDate(formatDate(response.data.lastModificationDate));
				setCreationDate(formatDate(response.data.dateCreated));
				setContinuation(response.data.continuation);
				setReceiverId(response.data.creator.id);
				setCreatorEmail(response.data.creator.email);
				setHidden(response.data.hidden);
				if(response.data.pages.list.length > 0){
					dispatch(appActions.setFirstPageId(response.data.pages.list[0].id));
				}
				setQuota({
					user: {
						quota: response.data.creator.quota,
						usedSpaceQuota: response.data.creator.usedSpaceQuota,
					},
					project: {
						quota: response.data.quota,
						usedSpaceQuota: response.data.usedSpaceQuota,
					},
				});
				setUsersCount(response.data.userCount);
			}).catch(() => {
				setNoAccess(true);
			});
	};

	useEffect(() => {
		reloadPages();
	},  [ currentPageNum, dispatch, isLoggedIn ]);

	useEffect(() => {
		setNoAccess(hidden && !isLoggedIn);
	}, [ isLoggedIn ]);

	useEffect(() => {
		getTranscriptionProcessingStatus(transcriptionId, setTranscriptionProcessingStatus, setTranscriptionProcessingStatusRequest);
	}, []);

	useEffect(() => {	
		
		let mounted = true;

		if (transcriptionProcessingStatusRequest) {
			if (transcriptionProcessingStatus) {
				setTranscriptionProcessingStatusDisabledButtons(transcriptionProcessingStatus.status === 'PROCESSING');
				const alert = {};
				if (transcriptionProcessingStatus.status === 'PROCESSING') {
					alert.text = 'transcription.alert.processing' + transcriptionProcessingStatus.details;
					alert.progress = transcriptionProcessingStatus.progress;
					alert.type = 'info';
					setTimeout(() => {
						if (mounted) {
							getTranscriptionProcessingStatus(transcriptionId,
								setTranscriptionProcessingStatus, setTranscriptionProcessingStatusRequest);
						}
					}, 5 * 1000);
				} else if (transcriptionProcessingStatus.status === 'READY') {
					alert.text = 'transcription.alert.processingOK';
					alert.type = 'success';
					alert.icon = 'icon-accept';
					alert.close = true;
					acknowledgeStatus();
				} else if (transcriptionProcessingStatus.status === 'FAILED') {
					alert.text = 'transcription.alert.processingFailed';
					// TODO use status.details for more specific message?
					alert.type = 'danger';
					alert.icon = 'icon-circle-warning-empty';
					alert.close = true;
					alert.onClose = acknowledgeStatus;
				}
				dispatch(alertActions.setAlert(alert));
			} else {
				setTranscriptionProcessingStatusDisabledButtons(false);
			}

		}

		return () => {
			mounted = false;
		};

	}, [ transcriptionProcessingStatusRequest ]);

	useEffect(() => {	
		if (transcriptionProcessingStatusPrevious?.status === 'PROCESSING' && transcriptionProcessingStatus?.status !== 'PROCESSING') {
			dispatch(appActions.setLoading(true));
			api.get(`/transcriptions/${transcriptionId}`, { params: { page: currentPageNum } })
				.then((response) => {
					dispatch(appActions.setLoading(false));
					setLayers(response.data.layers);
					setPages(response.data.pages);
					setPercentOfTranscribed(response.data.percentOfTranscribed);
					setPercentOfVerified(response.data.percentOfVerified);
				}).catch(() => {
					setNoAccess(true);
				});
		}

		setTranscriptionProcessingStatusPrevious(transcriptionProcessingStatus);
		
	}, [ transcriptionProcessingStatus ]);

	const formatDate = (date) => date.split(" ")[0].split("-").reverse().join('.');

	const breadcrumbsList = [
		{ id: 1, label: "home", link: url.home },
		{ id: 2, label:t('breadcrumbs.transcriptions'), link: url.transcriptions },
		{ id: 3, label: title },
	];

	return (!noAccess ? (
		<main>
		
			<Container>
				<Breadcrumbs breadcrumbsList={ breadcrumbsList } />
			
				<TranscriptionDetailsNav
					viewMode={ viewMode }
					creationMode={ creationMode }
					creationDate={ creationDate }
					transcriptionId={ parseInt(transcriptionId) }
					continuation={ continuation }
					receiverId={ parseInt(receiverId) }
					creatorEmail={ creatorEmail }
					title={ title }
					hidden={ hidden }
				/>
				<TranscriptionDetailsMetadata
					metadata={ metadata }
					viewMode={ viewMode }
					hidden={ hidden }
				/>
			</Container>
			<TranscriptionDetailsPages
				percentOfTranscribed={ percentOfTranscribed }
				percentOfVerified={ percentOfVerified }
				lastModificationDate={ lastModificationDate }
				pages={ pages }
				onGoToPage={ goPage }
				viewMode={ viewMode }
				creationMode={ creationMode }
				transcriptionId={ parseInt(transcriptionId) }
				transcriptionProcessingStatusDisabledButtons={ transcriptionProcessingStatusDisabledButtons }
				reloadPages={ reloadPages }
				quota={quota}
				continuation={ continuation }
				usersCount={ usersCount }
				layers={layers}
				transcriptionInfo={transcriptionInfo}
			/>

		</main>
	) : (
		<Error403 />
	));
};

export default TranscriptionDetails;

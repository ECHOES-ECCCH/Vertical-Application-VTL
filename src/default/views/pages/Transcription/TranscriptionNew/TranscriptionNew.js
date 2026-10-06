import React, { useEffect, useState } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Container } from "react-bootstrap";
import { Link } from "react-router-dom";
import { useParams, useHistory } from 'react-router';
import { reverse } from 'named-urls';

import { alertActions } from "@/store/actions/alert.actions";

import api from "@/services/api";
import url from "@/router/urls";
import { appActions } from '@/store/actions/app.actions';

import TranscriptionNewForm from '@Pages/Transcription/TranscriptionNew/parts/TranscriptionNewForm';

import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import Alert from "@Elements/Alert/Alert";
import Error403 from "@Pages/Error/Error403";
import { CheckSecurityGroupPermission } from "@/permissions/SecurityGroups";

const initialFormData = {
	resourceUrl: null,
	fileType: null,
	externalUrl: null,
	externalId: null,
	analyticInfo: {
		title: "",
		actors: [],
		language: null,
	},
	monographicInfo: {
		title: "",
		biblType: null,
		signature: null,
		actors: [],
		language: null,
		chapterNo: null,
		volumeNo: null,
		issueNo: null,
		pageRange: null,
		pages: null,
		publisher: null,
		publicationPlace: null,
		publicationDate: null,
		isbn: null,
		eisbn: null,
		issn: null,
		eissn: null,
	},
	profileDesc: {
		links: [ "" ],
		keywords: [],
		summary: null,
	},
	transcriptionInfo: {
		releaseDate: null,
		description: null,
		documentTypes: null,
		documentSrc: null,
		imgSrc: null,
		collectionName: null,
		contactName: null,
		contactSurname: null,
		contactEmail: null,
		guidelines: null,
	},
};

/** 
 * Page for creating, importing, publishing or editing a transcription
 * 
 * @component
 */
const TranscriptionNew = () => {
	const { t } = useTranslation();

	const dispatch = useDispatch();
	const history = useHistory();

	const currentLang = useSelector(state => state.app.currentLang);
	const isLoggedIn = useSelector(state => state.auth.credentials.isLoggedIn);
	const securityGroup = useSelector((state) => state.auth.credentials.securityGroup);
	const importedTranscription = useSelector(state => state.app.importedTranscription);

	const [ roleOptions, setRoleOptions ] = useState();
	const [ languageOptions, setLanguageOptions ] = useState();
	const [ biblTypes, setBiblTypes ] = useState();

	const transcriptionId = parseInt(useParams().transcriptionId);

	const [ defaultValues, setDefaultValues ] = useState(null);
	const [ temporaryData, setTemporaryData ] = useState(null);
	const [ editedTranscriptionLink, setEditedTranscriptionLink ] = useState(null);
	const [ isImported, setIsImported ] = useState(false);
	const [ dataProcessed, setDataProcessed ] = useState(false);

	let beingPublished = false;
	let currentUrlParams = new URLSearchParams(window.location.search);
	const publish = currentUrlParams.get("publish");
	if (publish && publish === 'true') {
		beingPublished = true;
	}

	useEffect(() => {
		if (isLoggedIn) {
			dispatch(appActions.setLoading(true));
			if (transcriptionId) {
				api.get(`/transcriptions/${transcriptionId}`)
					.then((response) => {
						setTemporaryData(response.data);
					});
			} else {
				if (importedTranscription) {

					setTemporaryData(JSON.parse(JSON.stringify(importedTranscription)));
					setIsImported(true);
					dispatch(appActions.setImportedTranscription(null));
				} else {
					setTemporaryData(JSON.parse(JSON.stringify(initialFormData)));
				}
			}
		}
	}, [ isLoggedIn, dispatch, transcriptionId ]);

	useEffect(() => {
		if(transcriptionId){
			setEditedTranscriptionLink(reverse(url.transcription.details, { transcriptionId: transcriptionId }));
		}
	}, [transcriptionId]);

	useEffect(() => {
		if (temporaryData) {
			getAvailableRoles();
			getAvailableLanguages();
			getBiblTypes();
		}
	}, [ temporaryData, currentLang ]);

	const getAvailableRoles = () => {
		api.get("/actor-roles")
			.then(res => {
				setRoleOptions(res.data.map((roleItem) => (
					{
						label: roleItem[currentLang],
						value: roleItem.code,
					}
				)));
			});
	};
	const getAvailableLanguages = () => {
		api.get("/languages")
			.then(res => {
				setLanguageOptions([ ...res.data, {
					code: "none",
					pl:t('transcription.form.none'),
					en:t('transcription.form.none'),
				} ].map((roleItem) => (
					{
						label: roleItem[currentLang],
						value: roleItem.code,
					}
				)));
			});
	};
	const getBiblTypes = () => {
		api.get("/bibl-types")
			.then(res => {
				setBiblTypes([ ...res.data, {
					code: "none",
					pl:t('transcription.form.none'),
					en:t('transcription.form.none'),
				} ].map(type => (
					{
						label: type[currentLang],
						value: type.code,
					}
				)));
			});

	};

	useEffect(() => {
		if (roleOptions && languageOptions && biblTypes && !dataProcessed) {
			setDataProcessed(true);
			processFormData(temporaryData);
		}
	}, [ roleOptions, languageOptions, biblTypes ]);

	const fetchActors = (actorsNode) => {
		if (!actorsNode || actorsNode.length < 1) {
			return [ { name: "", role: roleOptions[0].code } ];
		} else {
			return actorsNode.map((item) => ({ ...item, role: item.role.code }));
		}
	};

	const fetchLanguage = (languageNode) => {
		return (languageNode) ? languageNode.code : 'none';
	};

	const fetchBiblType = (biblNode) => {
		return (biblNode) ? biblNode.code : 'none';
	};

	const processImportResponse = (res) => {
		dispatch(appActions.setLoading(false));
		history.replace(reverse(url.transcription.details, { transcriptionId: parseInt(res.headers.location.split('/')[3]) }));

		window.scrollTo(0, 0);
		
		dispatch(
			alertActions.setAlert({
				type: "success",
				icon: "icon-accept",
				text: ( isImported ? "transcription.new.importSucced" : "transcription.new.alertSuccess" ),
				close: true,
				target: 'titleBox',
			}),
		);
	};

	const processResponse = (response) => {
		let transcriptionIdToUse = transcriptionId;

		if(isNaN(transcriptionIdToUse)){
			transcriptionIdToUse = response.data.id;
		}

		api.put(`/transcriptions/${transcriptionIdToUse}/hidden`, {"hidden": beingPublished ? false : true})
			.then(() => {
				history.replace(reverse(url.transcription.details, { transcriptionId: transcriptionId ? transcriptionId : response.data.id }));
				dispatch(appActions.setLoading(false));

				window.scrollTo(0, 0);

				dispatch(
					alertActions.setAlert({
						type: "success",
						icon: "icon-accept",
						text: (transcriptionId ? (beingPublished ? "transcription.new.alertPublishSuccess" : "transcription.new.alertEdit") : "transcription.new.alertSuccess"),
						close: true,
						target: 'titleBox',
					}),
				);
			});
	};
	const processError = (error) => {
		dispatch(appActions.setLoading(false));

		window.scrollTo(0, 0);

		dispatch(
			alertActions.setAlert({
				type: "danger",
				icon: "icon-circle-warning-empty",
				text: "transcription.new.alertError",
				close: true,
			}),
		);
		
		if (error.data && error.data.code) {
			switch (error.data.code) {
				case 'quota.error.user.exceeded': {
					dispatch(
						alertActions.setAlert({
							type: "danger",
							icon: "icon-circle-warning-empty",
							text: "form.messages.quotaExceeded",
							close: true,
						}),
					);
					break;
				}
				case 'quota.error.translation.exceeded': {
					dispatch(
						alertActions.setAlert({
							type: "danger",
							icon: "icon-circle-warning-empty",
							text: "form.messages.quotaTranscriptionExceeded",
							close: true,
						}),
					);
					break;
				}

				default:
					return false;
			}
		}

		if(error.data.resourceUrl){
			switch (error.data.resourceUrl) {
				case 'imports.text.format.link-less': {
					dispatch(
						alertActions.setAlert({
							type: "danger",
							icon: "icon-circle-warning-empty",
							text: "form.messages.linkLess",
							close: true,
						}),
					);
					break;
				}
			}
		}
	};

	const processFormData = (data) => {
		data.analyticInfo.actors = fetchActors(data.analyticInfo.actors);
		data.analyticInfo.language = fetchLanguage(data.analyticInfo.language);
		data.monographicInfo.actors = fetchActors(data.monographicInfo.actors);
		data.monographicInfo.language = fetchLanguage(data.monographicInfo.language);
		data.monographicInfo.biblType = fetchBiblType(data.monographicInfo.biblType);

		if (data.profileDesc) {
			data.profileDesc.keywords = (data.profileDesc.keywords && data.profileDesc.keywords.length > 0) ? (data.profileDesc.keywords.join("; ")) : ("");

			if (!data.profileDesc.links || data.profileDesc.links.length < 1) {
				data.profileDesc.links = [ { name: "" } ];
			} else {
				data.profileDesc.links = data.profileDesc.links.map(item => {
					return { name: item };
				});
			}
		}
		dispatch(appActions.setLoading(false));
		setDefaultValues(data);
	};

	const onExportData = (data) => {
		if (transcriptionId) {
			dispatch(appActions.setLoading(true));
			api.put(`/transcriptions/${transcriptionId}/metadata`, {
				'analyticInfo': {
					...data.analyticInfo,
					'title': data.analyticInfo.title,
				},
				'monographicInfo': {
					...data.monographicInfo,
				},
				'profileDesc': {
					...data.profileDesc,
					keywords: data.profileDesc.keywords,
				},
				'transcriptionInfo': {
					...data.transcriptionInfo,
				},
			})
				.then(response => processResponse(response))
				.catch(error => processError(error));
		} else {
			if (isImported && temporaryData) {
				dispatch(appActions.setLoading(true));
				api.post('/imports', {
					...temporaryData, ...data,
				})
					.then(response => processImportResponse(response))
					.catch(error => processError(error));
			} else {
				dispatch(appActions.setLoading(true));
				api.post("/transcriptions", {
					...data,
					'profileDesc': {
						...data.profileDesc,
						keywords: data.profileDesc.keywords,
					},
					'transcriptionInfo': {
						...data.transcriptionInfo,
					},
				})
					.then(response => processResponse(response))
					.catch(error => processError(error));
			}
		}

	};

	const breadcrumbsList = transcriptionId ? [
		{ label: "home", id: 1 },
		{ id: 2, label: t('breadcrumbs.transcriptionsUser'), link: url.transcriptionsUser },
		{ id: 3, label: temporaryData?.analyticInfo.title, link: editedTranscriptionLink },
		{ label: beingPublished ? t('form.publishTranscription') :t('transcription.edit.title'), id: 4 },
	] : [
		{ label: "home", id: 1 },
		{ label: t('transcription.new.title'), id: 2 },
	];

	return CheckSecurityGroupPermission("canCreateTranscription", securityGroup) ? (
		<main className='transcription-new'>
			<Container>
				<Breadcrumbs breadcrumbsList={ breadcrumbsList } />
				<div>
					<Alert />
					<div className='transcription-new__block'>

						<div className='transcription-new__title--container'>
							<div>
								<h2 style={{ margin: "20px 0px", fontWeight: "bold" }}>
									{transcriptionId
										? (beingPublished 
											?t('form.publishTranscription')
											:t('transcription.edit.title')
										)
										: (isImported
											?t('transcription.new.importedTitle')
											:t('transcription.new.title')
										)}
								</h2>
								{beingPublished &&
								<small>{t('transcription.details.publicInfo')}</small>}
							</div>
						</div>					
						{(!isImported && !transcriptionId) && (
							<span style={{ "marginBottom": "30px" }}>
								{t('transcription.new.beforeImport')}
								<Link
									to={ url.transcription.import }
									className='transcription-new__import'
								>
									{t('transcription.new.import')}
								</Link>
							</span>
						)}
						{defaultValues &&
							<TranscriptionNewForm
								transcriptionId={ transcriptionId }
								defaultValues={ defaultValues }
								isDisabled={ false }
								roleOptions={ roleOptions }
								biblTypes={ biblTypes }
								languageOptions={ languageOptions }
								onExportData={ onExportData }
								isImported={ isImported }
								forcePublish={ beingPublished }
							/>}
					</div>
				</div>
			</Container>
		</main>
	) : <Error403 />;
};

export default TranscriptionNew;

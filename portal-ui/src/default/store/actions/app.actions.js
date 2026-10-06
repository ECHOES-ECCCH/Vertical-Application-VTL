import api from "@/services/api";

const saveCookie = appData => ({
	type: "SAVE_COOKIE",
	appData,
});

const setLang = lang => ({
	type: "SET_CURRENT_LANGUAGE",
	lang,
});

const setMourning = isMourning => ({
	type: "SET_MOURNING",
	isMourning,
});

const setCookiePermission = cookiePermission => ({
	type: "SET_COOKIE_PERMISSION",
	cookiePermission,
});

const setLoading = isLoading => ({
	type: "SET_LOADING",
	isLoading,
});

const setError = isError => ({
	type: "SET_ERROR",
	isError,
});

const setNotFound = isNotFound => ({
	type: "SET_NOTFOUND",
	isNotFound,
});

const setSwapPanels = value => ({
	type: "SET_SWAP_PANELS",
	value,
});

const setTogglePanelsOSD = value => ({
	type: "SET_TOGGLE_PANELS_OSD",
	value,
});

const setTogglePanelsEditor = value => ({
	type: "SET_TOGGLE_PANELS_EDITOR",
	value,
});

const setMobilePanels = value => ({
	type: "SET_MOBILE_PANELS",
	value,
});

const getOCRProfiles = () => {
	return dispatch => {
		api.get("/ocr/languages").then(response => {
			dispatch(receiveOCRProfiles(response.data));
		});
	};
};

const receiveOCRProfiles = data => ({
	type: "RECEIVE_OCR_PROFILES",
	data,
});

const setImportedTranscription = value => ({
	type: "SET_IMPORTED_TRANSCRIPTION",
	value,
});

const setModalConfirmInfo = value => ({
	type: "SET_MODAL_CONFIRM_INFO",
	value,
});

const setModalDeleteTranscriptionPage = value => ({
	type: "SET_MODAL_DELETE_TRANSCRIPTION_PAGE",
	value,
});

const setModalDownloadTranscriptionPage = value => ({
	type: "SET_MODAL_DOWNLOAD_TRANSCRIPTION_PAGE",
	value,
});

const setModalLoadTextTranscriptionPage = value => ({
	type: "SET_MODAL_LOADTEXT_TRANSCRIPTION_PAGE",
	value,
});

const setModalCommentsDeleteCommentId = value => ({
	type: "SET_MODAL_COMMENTS_DELETE_COMMENTID",
	value,
});

const receiveQuestionnaireLink = value => ({
	type: "RECEIVE_QUESTIONNAIRE_LINK",
	value,
});

const getQuestionnaireLink = () => {
	return dispatch => {
		api.get("/info/survey-url").then(response => {
			dispatch(receiveQuestionnaireLink(response.data));
		});
	};
};

const setImportsDomains = value => ({
	type: "SET_IMPORTS_DOMAINS",
	value,
});

const setSelectedIndexId = value => ({
	type: "SET_SELECTED_INDEX_ID",
	value,
});

const setFirstPageId = value => ({
	type: "SET_FIRST_PAGE_ID",
	value,
});

const setAppConfig = value => (
	{
		type: "SET_APP_CONFIG",
		value,
	});

export const appActions = {
	saveCookie,
	setLang,
	setMourning,
	setLoading,
	setError,
	setNotFound,
	setCookiePermission,
	setSwapPanels,
	setTogglePanelsOSD,
	setTogglePanelsEditor,
	setMobilePanels,
	getOCRProfiles,
	setModalConfirmInfo,
	setModalDeleteTranscriptionPage,
	setModalDownloadTranscriptionPage,
	setModalLoadTextTranscriptionPage,
	setModalCommentsDeleteCommentId,
	setImportedTranscription,
	getQuestionnaireLink,
	setImportsDomains,
	setSelectedIndexId,
	setFirstPageId,
	setAppConfig,
};

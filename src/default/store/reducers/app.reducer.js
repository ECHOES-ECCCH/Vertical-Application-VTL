import appInitialState from '@/store/initialState/app.initialState';
import { dct } from '@/store/initialState/langs.initialState';

const initState = { ...appInitialState, dict: dct['pl'] };

export function app(state = initState, action) {
	switch (action.type) {
		case 'SAVE_COOKIE':
			return {
				...state,
				currentLang: action.payload.currentLang ? action.payload.currentLang : state.currentLang,
				cookiePermission: action.payload.cookiePermission ? action.payload.cookiePermission : state.cookiePermission,
				viewer: action.payload.viewer ? action.payload.viewer : state.viewer,
				dict: action.payload.currentLang ? dct[action.payload.currentLang] : dct[state.currentLang],
			};
		case 'SET_CURRENT_LANGUAGE':
			return {
				...state,
				langs: state.langs.map(lang => { return { ...lang, active: lang.id === action.lang.id }; }),
				currentLang: action.lang.id,
				dict: dct[action.lang.id],
			};
		case 'SET_APP_ERROR':
			return {
				...state,
				isAppError: true,
			};
		case 'RESET_APP_ERROR':
			return {
				...state,
				isAppError: false,
			};
		case 'SET_MOURNING':
			return {
				...state,
				isMourning: action.isMourning,
			};
		case 'SET_LOADING':
			return {
				...state,
				isLoading: action.isLoading,
			};
		case 'SET_ERROR':
			return {
				...state,
				isError: action.isError,
			};
		case 'SET_NOTFOUND':
			return {
				...state,
				isNotFound: action.isNotFound,
			};
		case 'SET_COOKIE_PERMISSION':
			return {
				...state,
				cookiePermission: true,
			};

		case 'SET_SWAP_PANELS':
			return {
				...state,
				viewer: {
					...state.viewer,
					swapPanels: action.value,
				},
			};

		case 'SET_TOGGLE_PANELS_OSD':
			return {
				...state,
				viewer: {
					...state.viewer,
					togglePanels: {
						...state.viewer.togglePanels,
						osd: action.value,
					},
				},
			};

		case 'SET_TOGGLE_PANELS_EDITOR':
			return {
				...state,
				viewer: {
					...state.viewer,
					togglePanels: {
						...state.viewer.togglePanels,
						editor: action.value,
					},
				},
			};

		case 'SET_MOBILE_PANELS':
			return {
				...state,
				viewer: {
					...state.viewer,
					mobilePanels: action.value,
				},
			};

		case 'SET_IMPORTED_TRANSCRIPTION':
			return {
				...state,
				importedTranscription: action.value,
			};
		case 'SET_MODAL_CONFIRM_INFO':
			return {
				...state,
				modalConfirmInfo: action.value,
			};
		case 'SET_MODAL_DELETE_TRANSCRIPTION_PAGE':
			return {
				...state,
				modalDeleteTranscriptionPage: action.value,
			};
		case 'SET_MODAL_DOWNLOAD_TRANSCRIPTION_PAGE':
			return {
				...state,
				modalDownloadTranscriptionPage: action.value,
			};
		case 'SET_MODAL_LOADTEXT_TRANSCRIPTION_PAGE':
			return {
				...state,
				modalLoadTextTranscriptionPage: action.value,
			};				
		case 'SET_MODAL_COMMENTS_DELETE_COMMENTID':
			return {
				...state,
				modalCommentDeleteCommentId: action.value,
			};

		case 'RECEIVE_QUESTIONNAIRE_LINK':
			return {
				...state,
				questionnaireLink: action.value,
			};

		case 'RECEIVE_OCR_PROFILES':
			return {
				...state,
				OCRProfiles: action.data,
			};
		case 'SET_IMPORTS_DOMAINS':
			return {
				...state,
				importsDomains: action.value,
			};
		case "SET_SELECTED_INDEX_ID":
			return {
				...state,
				selectedIndexId: action.value,
			};
		case "SET_FIRST_PAGE_ID":
			return {
				...state,
				firstPageId: action.value,
			};
		case "SET_APP_CONFIG":
			return {
				...state,
				config: action.value,
			};
		default:
			return state;
	}
}
import { langs } from '@/store/initialState/langs.initialState';

const getClientLanguage = () => {
	const preferredLanguages = navigator.languages || [navigator.language];

	for (let lang of preferredLanguages) {
		const langCode = lang.split('-')[0].toLowerCase();
		const matchedLang = langs.find((l) => l.id === langCode);
		if (matchedLang) {
			return matchedLang.id;
		}
	}
	return langs[0].id;
};

const appInitialState = {
	langs,
	currentLang: getClientLanguage(),
	isMourning: false,
	isLoading: false,
	isError: false,
	isNotFound: false,
	cookiePermission: false,
	dict: {},
	viewer: {
		togglePanels: {
			osd: false,
			editor: false,
		},
		swapPanels: false,
		mobilePanels: 'osd',
	},
	OCRProfiles: [],
	modalConfirmInfo: "",
	modalDeleteTranscriptionPage: null,
	modalCommentDeleteCommentId: null,
	importedTranscription: null,
	questionnaireLink: null,
	selectedIndexId: null,
	firstPageId: null,
	config: {
		reactAppApi: null,
		accessibilityStatement: null,
	},
};

export default appInitialState;
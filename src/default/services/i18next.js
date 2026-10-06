import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import LanguageDetector from 'i18next-browser-languagedetector';

import en from "@/locales/en/default.json";
import pl from "@/locales/pl/default.json";
import de from "@/locales/de/default.json";
import en_custom from "@/locales/en/custom.json";
import pl_custom from "@/locales/pl/custom.json";
import de_custom from "@/locales/de/custom.json";

const merge = (def, custom) => {
	if (def == null) return custom;
	if (custom == null) return def;
	if (Array.isArray(custom) || typeof custom !== 'object')
		return custom;
	const merged = { ...def };
	for (const [key, value] of Object.entries(custom)) {
		merged[key] = merge(merged[key], value);
	}
	return merged;
};
	
const options = {
	order: ['querystring', 'localStorage', 'navigator'],
	lookupQuerystring: 'lang',
};

const resources = {
	en: {
		default: merge(en, en_custom),
	},
	pl: {
		default: merge(pl, pl_custom),
	},
	de: {
		default: merge(de, de_custom),
	},
};

i18n
	.use(LanguageDetector)
	.use(initReactI18next)
	.init({
		supportedLngs: ['pl', 'en', 'de'],
		fallbackLng: 'pl',
		detection: options,
		debug: true,
		defaultNS: 'default',
		ns: [ 
			'default',
		],
		load: 'languageOnly',
		resources: resources,
		interpolation: {
			escapeValue: false,
		},
	});

export default i18n;
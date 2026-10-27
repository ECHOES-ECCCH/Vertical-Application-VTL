import pl from '@/locales/pl/default.json';
import en from '@/locales/en/default.json';
import de from '@/locales/de/default.json';

export const langs = [
	{
		id: 'en',
		name: 'English',
	},
	{
		id: 'pl',
		name: 'Polski',
	},
	{
		id: 'de',
		name: 'Deutsch',
	},
];

export const dct = { ...{}, pl, en, de };
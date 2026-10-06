import "react-app-polyfill/ie9";
import "react-app-polyfill/ie11";
import "react-app-polyfill/stable";

import React from "react";
import { createRoot } from 'react-dom/client';
import { Router } from "react-router-dom";

import { Provider } from "react-redux";

import * as serviceWorker from "@/serviceWorker";
import { history } from "@/utils/history";

import store from '@/store/store';

import "normalize.css";
import "draft-js/dist/Draft.css";
import main from "@Styles/main.sass"; // eslint-disable-line

import App from "@Components/App/App";
import ModalWrapper from "@Elements/Modal/ModalWrapper";
import ScrollToTop from "@Components/App/ScrollToTop";

import en from "@/locales/en/default.json";
import pl from "@/locales/pl/default.json";
import de from "@/locales/de/default.json";

import '@/services/i18next';

const findDifferences = function(obj1, obj2, diffArray, parentKey) {
	const diffArrayChecked = diffArray || [];
	const parentKeyChecked = parentKey || "";
	const obj1Keys = Object.keys(obj1);

	for (let objKey of obj1Keys) {
		if ( typeof(obj1[objKey] !== 'undefined') && (typeof(obj2[objKey]) === 'undefined') )
			diffArrayChecked.push( parentKeyChecked? parentKeyChecked+"."+objKey : objKey);
		else if (typeof obj1[objKey] == "object" && typeof obj2[objKey] == "object") {
			findDifferences(obj1[objKey], obj2[objKey], diffArrayChecked, parentKeyChecked? parentKeyChecked+"."+objKey : objKey);
		}
	}
	return diffArrayChecked;
};

const languageDifferences = [];
findDifferences(pl, en, languageDifferences, 'en');
findDifferences(pl, de, languageDifferences, 'de');
if (languageDifferences && languageDifferences.length > 0 ) {
	console.error("Brakujące klucze tłumaczeń:\n"+languageDifferences.join('\n')); // eslint-disable-line
}

const root = createRoot(document.getElementById('root'));

root.render(
	<Provider store={ store }>
		<Router history={ history }>
			<ScrollToTop/>
			<App />
			<ModalWrapper />
		</Router>
	</Provider>,
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();

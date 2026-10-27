import axios from 'axios';

import store from '@/store/store';
import { history } from '@/utils/history';

import { authActions } from "@/store/actions/auth.actions";
import { appActions } from "@/store/actions/app.actions";

import url from "@/router/urls";

const api = axios.create({
	withCredentials: true,
	headers: {
		Accept: 'application/json',
		'Content-Type': 'application/json',
	},
	crossdomain: true,
});

api.interceptors.request.use( config => {
	config.baseURL = store.getState().app.config.reactAppApi;
	config.headers = { 
		...config.headers, 
		Authorization: store.getState().auth.credentials.token, 
		Language: store.getState().app.currentLang, 
		'X-CSRF-TOKEN': store.getState().auth.credentials.csrf, 
	};

	return config;
}, function (error) {
	return Promise.reject(error);
},
);

api.interceptors.response.use( response => {    
	return response;
}, error => {
	if (error.config.passAllErrors) {
		return Promise.reject(error.response);
	}
	if (error.response) {
		switch (error.response.status) {
			case 401:
			case 403:
				store.dispatch(appActions.setLoading(false));
				if (store.getState().auth.credentials.isLoggingEnd && !store.getState().auth.credentials.isLoggedIn) {
					store.dispatch(authActions.setLoginReturnUrl(history.location.pathname));

					const formsAutorization = store.getState().app.config.authorization == "FORMS";
					if(formsAutorization)
						history.push(url.auth.login);
					else {
						const authorizationLoginUrl = store.getState().app.config.reactAppApi + store.getState().app.config.authorizationLoginUrl;
						window.location = authorizationLoginUrl + "?returnUrl=" + encodeURIComponent(window.location);
					}
					break;
				}
				return Promise.reject(error.response);
			case 404:
				store.dispatch(appActions.setNotFound(true));
				store.dispatch(appActions.setLoading(false));
				break;
			case 500:
				store.dispatch(appActions.setError(true));
				store.dispatch(appActions.setLoading(false));
				return Promise.reject(error.response);
			case 502:
				store.dispatch(appActions.setError(true));
				store.dispatch(appActions.setLoading(false));
				return Promise.reject(error.response);
			default:
				return Promise.reject(error.response);
		}
	} else {
		store.dispatch(appActions.setError(true));
		store.dispatch(appActions.setLoading(false));
		return Promise.reject(error.response);
	}
	
});

export default api;

/* ---
*   How to use api service:
*   - import api service into a file -> import 'services/api.js';
*   - GET method -> api.get('url').then((response) => ...).catch((error) => ...)
*   - POST method -> api.post('url', { data }).then((response) => ...).catch((error) => ...)
*   - PUT method -> api.put('url', { data }).then((response) => ...).catch((error) => ...)
*
*   Remember: PUT for UPDATE operations, POST for CREATE operations.
* ---
*/

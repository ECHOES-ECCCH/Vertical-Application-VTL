import api from "@/services/api";
import url from "@/router/urls";
import { cookieService } from "@/services/cookie";
import { history } from "@/utils/history";

import { alertActions } from "@/store/actions/alert.actions";
import { appActions } from "@/store/actions/app.actions";

const logoutADAddress = process.env.REACT_APP_LOGIN_SP_AD_URL_LOGOUT;

const setAuthToken = token => {
	return {
		type: "SET_AUTH_TOKEN",
		token,
	};
};

const setAuthCSRFToken = token => {
	return {
		type: "SET_AUTH_CSRFTOKEN",
		token,
	};
};

const setAuthIsLoggedIn = isLoggedIn => {
	return {
		type: "SET_AUTH_ISLOGGEDIN",
		isLoggedIn,
	};
};

const setAuthId = id => {
	return {
		type: "SET_AUTH_ID",
		id,
	};
};

const setAuthName = name => {
	return {
		type: "SET_AUTH_NAME",
		name,
	};
};

const setAuthEmail = email => {
	return {
		type: "SET_AUTH_EMAIL",
		email,
	};
};

const setAuthSecurityGroup  = securityGroup  => {
	return {
		type: "SET_AUTH_SECURITYGROUP",
		securityGroup,
	};
};

const setAuthHasPassword = hasPassword => {
	return {
		type: "SET_AUTH_HAS_PASSWORD",
		hasPassword,
	};
};

const setAuthQuota = quota => {
	return {
		type: "SET_AUTH_QUOTA",
		quota,
	};
};

const setAuthUsedSpaceQuota = usedSpaceQuota => {
	return {
		type: "SET_AUTH_USED_SPACE_QUOTA",
		usedSpaceQuota,
	};
};

const setAuthMyGroups = myGroups => {
	return {
		type: "SET_AUTH_MY_GROUPS",
		myGroups,
	};
};

const setAuthOtherGroups = otherGroups => {
	return {
		type: "SET_AUTH_OTHER_GROUPS",
		otherGroups,
	};
};

const setSignOut = () => {
	return {
		type: "SET_AUTH_SIGNOUT",
	};
};

const setAuthIsLoggingEnd = isLoggingEnd => {
	return {
		type: "SET_AUTH_ISLOGGINGEND",
		isLoggingEnd,
	};
};

const setAuthPathIsProtected = isProtected => {
	return {
		type: "SET_AUTH_PATH_ISPROTECTED",
		isProtected,
	};
};

const checkAuth = () => {
	return dispatch => {
		if (cookieService.getToken()) {
			dispatch(setAuthToken(cookieService.getToken()));
		}

		api.put("/auth/token")
			.then((res) => {
				dispatch(setAuthIsLoggedIn(true));
				dispatch(setAuthIsLoggingEnd(true));
			})
			.catch(() => {
				dispatch(setAuthIsLoggedIn(false));
				cookieService.removeToken();
				dispatch(setAuthIsLoggingEnd(true));
			});

		api.get("/auth/csrf/token")
			.then((response) => {
				dispatch(setAuthCSRFToken(response.headers['csrf-token']));
			});
	};
};

const signIn = (data, returnUrl) => {
	return dispatch => {
		dispatch(setLoginIsLoading(true));

		api.post(`/auth/sign-in?remember-me=${data.rememberMe}`, {
			email: data.email,
			password: data.password,
		})
			.then(response => {
				dispatch(setLoginIsLoading(false));

				dispatch(setAuthIsLoggedIn(true));
				cookieService.setToken(response.headers.authorization);
				dispatch(setAuthToken(response.headers.authorization));

				if (data.rememberMe) {
					api.get("/auth/csrf/token")
						.then((resp) => {
							dispatch(setAuthCSRFToken(resp.headers['csrf-token']));
						});
				}

				history.push(returnUrl ? returnUrl : url.home);

				dispatch(setLoginReturnUrl(null));

			})
			.catch(() => {
				dispatch(setLoginIsLoading(false));
				dispatch(
					alertActions.setAlert({
						type: "danger",
						icon: "icon-circle-warning-empty",
						text: "auth.login.isNotValid",
						close: true,
					}),
				);
			});
	};
};

const fedSignIn = (email, returnUrl) => {
	return dispatch => {
		dispatch(setLoginIsLoading(true));
		dispatch(appActions.setLoading(true));

		api.post(`/auth/fed-sign-in?email=${email}`)
			.then(response => {
				dispatch(setLoginIsLoading(false));
				dispatch(appActions.setLoading(false));

				dispatch(setAuthIsLoggedIn(true));

				dispatch(setAuthToken(response.headers.authorization));
				cookieService.setToken(response.headers.authorization);
				
				history.push(returnUrl ? returnUrl : url.home);
				dispatch(setLoginReturnUrl(null));
			});
	};
};

const signOut = (reactAppApi, isProtected, formsAutorization) => {
	return dispatch => {
		if (!formsAutorization) {
			window.location = reactAppApi + '/auth/sign-out?post_logout_redirect_uri=' + window.location.origin;
			return;
		}
		
		dispatch(appActions.setLoading(true));

		api.get('/auth/sign-out')
			.then(() => {
				dispatch(appActions.setLoading(false));
				
				cookieService.removeToken();
				dispatch(setSignOut());
				
				if (isProtected) {
					history.push(url.home);
				}

			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
			});
	};
};

const deleteUser = (removePublished) => {
	return dispatch => {
		dispatch(appActions.setLoading(true));
		
		const redirectUri = encodeURIComponent(window.location.origin + "?userdeleted");
		api.delete(`/users/current?removePublished=${removePublished}&post_logout_redirect_uri=${redirectUri}`)
			.then((resp) => {
				dispatch(appActions.setLoading(false));
				
				cookieService.removeToken();
				dispatch(setSignOut());
				
				history.push(url.home);
				if (resp.headers.location) {
					window.location = resp.headers.location;
				} else {
					dispatch(
						alertActions.setAlert({
							type: "success",
							icon: "icon-accept",
							text: "auth.deleteUser.success",
							close: true,
						}),
					);
				}
			})
			.catch((resp) => {
				// eslint-disable-next-line
				console.log(resp.data);
				dispatch(appActions.setLoading(false));
			});
	};
};

const setLoginIsLoading = value => {
	return {
		type: "SET_LOGIN_ISLOADING",
		isLoading: value,
		value,
	};
};

const setLoginReturnUrl = value => {
	return {
		type: "SET_LOGIN_RETURNURL",
		value,
	};
};

export const authActions = {
	checkAuth,
	signIn,
	fedSignIn,
	signOut,
	deleteUser,
	setLoginReturnUrl,
	setAuthPathIsProtected,
	setAuthSecurityGroup,
	setAuthEmail,
	setAuthId,
	setAuthName,
	setAuthHasPassword,
	setAuthQuota,
	setAuthUsedSpaceQuota,
	setAuthMyGroups,
	setAuthOtherGroups,
};

import React, { Suspense, useEffect, useLayoutEffect, useState } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { Switch, useLocation, useHistory } from "react-router-dom";
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";
import ReactGA from 'react-ga4';
import { Tooltip } from 'react-tooltip';

import { appActions } from "@/store/actions/app.actions";
import { authActions } from "@/store/actions/auth.actions";

import api from "@/services/api";

import routes from "@/router/routes";
import RouteLeavingGuard from "@/router/routeLeavingGuard";
import AppRoute from "@Components/App/AppRoute";

import Cookies from "@Components/Cookies/Cookies";
import Spinner from "@Elements/Spinner/Spinner";

import AppHeader from "@Templates/header/AppHeader/AppHeader";
import AuthHeader from "@Templates/header/AuthHeader/AuthHeader";
import AppFooter from "@Templates/footer/AppFooter/AppFooter";
import Error500 from "@Pages/Error/Error500";
import Error404 from "@Pages/Error/Error404";

import useStorageState from "@/utils/useStorageState";

import 'moment/locale/pl';
import moment from 'moment';

const App = () => {
	moment().locale("pl");

	const { i18n } = useTranslation();
	const history = useHistory();
	const [ configLoaded, setConfigLoaded ] = useState(false);
	const dispatch = useDispatch();
	const location = useLocation();
	const currentAppState = useSelector((state) => state.app);
	const [isMourning, setIsMourning ] = useStorageState("mourn",currentAppState.isMourning);
	const currentLang = useSelector((state) => state.app.currentLang);
	const { t } = useTranslation();

	const isLoggedIn = useSelector((state) => state.auth.credentials.isLoggedIn);

	const googleAnalyticsTagId = useSelector((state) => state.app.config.googleAnalyticsTagId);

	const discoveredLang = localStorage.getItem(i18n.options.detection.lookupLocalStorage);
	if (discoveredLang !== currentLang) {
		localStorage.setItem(i18n.options.detection.lookupLocalStorage, currentLang);
		i18n.changeLanguage(currentLang).then(() => {});
	}

	const loadConfigFile = async () => {
		const response = await fetch(
			process.env.REACT_APP_CONFIG ? `/config/${process.env.REACT_APP_CONFIG}_config.json` : '/config/config.json',
			{
				method: 'GET',
			},
		);
		const config = await response.json();
		dispatch(appActions.setAppConfig(config));
		setConfigLoaded(true);
	};

	useLayoutEffect(() => {
		if (isLoggedIn === true) {
			api
				.get("/users/my-profile")
				.then(response => {
					const { user, myGroups, otherGroups } = response.data;
					dispatch(authActions.setAuthId(user.id));
					dispatch(authActions.setAuthName(user.name));
					dispatch(authActions.setAuthSecurityGroup(user.securityGroup));
					dispatch(authActions.setAuthEmail(user.email));
					dispatch(authActions.setAuthHasPassword(user.hasPassword));
					dispatch(authActions.setAuthQuota(user.quota));
					dispatch(authActions.setAuthUsedSpaceQuota(user.usedSpaceQuota));
					dispatch(authActions.setAuthMyGroups(myGroups));
					dispatch(authActions.setAuthOtherGroups(otherGroups));
				});
		}
	}, [ isLoggedIn ]);

	useEffect(() => {
		loadConfigFile();
		setIsMourning(false);
	}, []);

	useEffect(() => {
		document.title =t('title');
		document.documentElement.setAttribute('lang', currentLang);
	}, [ currentLang ]);

	useEffect(() => {
		dispatch(appActions.saveCookie());
	}, [ currentAppState ]);

	useEffect(() => {
		if (googleAnalyticsTagId)
			ReactGA.initialize(googleAnalyticsTagId);
	}, [ googleAnalyticsTagId ]);

	useEffect(() => {
		if(configLoaded)
		{
			if (location.search) {
				const email = new URLSearchParams(location.search).get("email");
				if (email) {
					dispatch(authActions.fedSignIn(email, location.pathname));
				}
			}

			dispatch(appActions.getOCRProfiles());
			dispatch(authActions.checkAuth());
			dispatch(appActions.getQuestionnaireLink());
		}
	}, [configLoaded]);

	useEffect(() => {
		document.querySelector("html").classList[isMourning ? "add" : "remove"]("mourn");
	}, [ isMourning ]);

	const handleBlockedNavigation = nextLocation => {
		if(!isLoggedIn && nextLocation.state?.isProtected)
			return true;

		return false;
	};

	useEffect(() => {
		if(currentAppState.isNotFound) {
			dispatch(appActions.setNotFound(false));
		}
	}, [ location.pathname ]);
	
	return (
		<>
			{(currentAppState.isError || currentAppState.isNotFound) ? (
				<>
					{currentAppState.isError &&
						<>
							<AuthHeader />
							<Error500 />
						</>}
					{currentAppState.isNotFound &&
						<>
							<AppHeader />
							<Error404 />
							<AppFooter />
						</>}
				</>
			) : (
				<>
					{ configLoaded &&
					<Suspense fallback={ <Spinner /> }>
						<RouteLeavingGuard
							navigate={path => {
								if(path.startsWith('https://'))
									window.location = path;
								else
									history.push(path);
							}}
							shouldBlockNavigation={handleBlockedNavigation}
						/>

						<Switch>
							{routes.map(route => (
								<AppRoute
									{ ...route }
									key={ route.id }
								/>
							))}
						</Switch>

					</Suspense>}
				</>
			)}

			{currentAppState.cookiePermission !== true &&
				<Cookies />}

			<Tooltip id='custom-tooltip' />
		</>
	);
};

App.propTypes = {
	routes: PropTypes.arrayOf(
		PropTypes.shape({
			id: PropTypes.number.isRequired,
			path: PropTypes.string,
			exact: PropTypes.bool,
			component: PropTypes.object,
		}).isRequired,
	),
};

export default App;
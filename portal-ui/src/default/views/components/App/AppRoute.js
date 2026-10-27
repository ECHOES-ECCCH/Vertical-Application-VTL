import React, { useState, useEffect } from "react";
import { useSelector, useDispatch } from 'react-redux';
import PropTypes from 'prop-types';
import { Route, Redirect } from "react-router-dom";
import { authActions } from "@/store/actions/auth.actions";

import url from "@/router/urls";

import Spinner from "@Elements/Spinner/Spinner";

const MulticomponentRoute = ({ header: HeaderComponent, main: MainComponent, footer: FooterComponent, ...rest }) => {
	return (
		<Route
			{ ...rest }
			render={ props => (
				<>
					{HeaderComponent ? (
						<header>
							<HeaderComponent { ...props } />
						</header>
					) : null}
					{MainComponent ? (<MainComponent { ...props } />) : null}
					{FooterComponent ? (
						<footer>
							<FooterComponent { ...props } />
						</footer>) : null}
				</>
			) }
		/>
	);
};
MulticomponentRoute.propTypes = {
	header: PropTypes.func,
	main: PropTypes.func,
	footer: PropTypes.func,
};

const MulticomponentProtectedRoute = ({ header: HeaderComponent, main: MainComponent, footer: FooterComponent, ...rest }) => {
	const isLoggedIn = useSelector((state) => state.auth.credentials.isLoggedIn);
	const isLoggingEnd = useSelector((state) => state.auth.credentials.isLoggingEnd);
	const formsAutorization = useSelector((state) => state.app.config.authorization == "FORMS");

	const reactAppApi = useSelector((state) => state.app.config.reactAppApi);
	const authorizationLoginUrl = useSelector((state) => state.app.config.authorizationLoginUrl);

	const [ isLogging, setIsLogging ] = useState(false);
	const dispatch = useDispatch();
	
	useEffect(() => {
		if (!isLoggedIn) {
			dispatch(authActions.setLoginReturnUrl(window.location.pathname));
		}
	}, []);

	useEffect(() => {
		setIsLogging(isLoggingEnd);
	}, [ isLoggingEnd ]);

	const getLoginUrl = () => {
		return reactAppApi + authorizationLoginUrl + "?returnUrl=" + encodeURIComponent(window.location);
	};

	return (
		<>
			{
				!isLogging ? (
					<Spinner />
				) : (
					<Route
						{ ...rest }
						render={ props => (
							
							isLoggedIn
								? (
									<>
										{HeaderComponent ? (
											<header>
												<HeaderComponent { ...props } />
											</header>
										) : null}
										{MainComponent ? (<MainComponent { ...props } />) : null}
										{FooterComponent ? (
											<footer>
												<FooterComponent { ...props } />
											</footer>) : null}
									</>
								) : ( 
									formsAutorization ?
										<Redirect to={url.auth.login} /> 
										:
										window.location = getLoginUrl()								
								)
						) }
					/>
				)
			}
		</>
		
	);
};
MulticomponentProtectedRoute.propTypes = {
	header: PropTypes.func,
	main: PropTypes.func,
	footer: PropTypes.func,
};

const AppRoute = ({ isProtected, exact, path, components }) => {
	const dispatch = useDispatch();
	useEffect(() => {
		dispatch(authActions.setAuthPathIsProtected(isProtected));
	}, [ isProtected ]);

	if (isProtected) return ( 
		<MulticomponentProtectedRoute
			exact={ exact }
			path={ path }
			header={ components.header }
			main={ components.main }
			footer={ components.footer }
		/> 
	);

	return (
		<MulticomponentRoute
			exact={ exact }
			path={ path }
			header={ components.header }
			main={ components.main }
			footer={ components.footer }
		/>
	);
};
AppRoute.propTypes = {
	isProtected: PropTypes.bool,
	exact: PropTypes.bool,
	path: PropTypes.string,
	components: PropTypes.any,
};

export default AppRoute;

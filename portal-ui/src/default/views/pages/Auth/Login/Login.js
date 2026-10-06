import React, { useEffect } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useParams } from "react-router";

import Container from "react-bootstrap/Container";

import api from "@/services/api";
import url from "@/router/urls";

import { alertActions } from '@/store/actions/alert.actions';
import { authActions } from '@/store/actions/auth.actions';

import LoginForm from "@Pages/Auth/Login/parts/LoginForm/LoginForm";
import LoginAlternate from "@Pages/Auth/Login/parts/LoginAlternate/LoginAlternate";

import styles from '@Pages/Auth/Login/Login.module.sass';

const Login = () => {
	const dispatch = useDispatch();
	const { token } = useParams();

	const returnUrl = useSelector((state) => state.auth.login.returnUrl);
	const previousUrl = document.referrer ? new URL(document.referrer) : null;

	const formsAutorization = useSelector((state) => state.app.config.authorization == "FORMS");
	const authorizationLoginUrl = useSelector((state) => state.app.config.reactAppApi + state.app.config.authorizationLoginUrl);
	const isLoggedIn = useSelector((state) => state.auth.credentials.isLoggedIn);
	const isLoggingEnd = useSelector((state) => state.auth.credentials.isLoggingEnd);
	
	useEffect(() => {
		if (!formsAutorization && isLoggingEnd) {
			const parentUrl = window.location.href.split('/').slice(0, -1).join('/');
			window.location = isLoggedIn ? parentUrl
				: authorizationLoginUrl + "?returnUrl=" + encodeURIComponent(parentUrl);
			return;
		}
	}, [isLoggingEnd]);

	useEffect(() => {
		if (token) {
			api.put(`/auth/activate?token=${token}`, { data: null })
				.then(() => {
					dispatch(alertActions.setAlert({
						type: 'success',
						icon: 'icon-accept',
						text: false,
						textHTML: 'auth.login.isTokenValid',
						close: true,
					}));
				}).catch(() => {
					dispatch(alertActions.setAlert({
						type: 'danger',
						icon: 'icon-circle-warning-empty',
						text: 'auth.login.isTokenNotValid',
						close: true,
					}));
				});
		}

		if (!returnUrl && previousUrl && previousUrl.pathname !== url.auth.login) {
			dispatch(authActions.setLoginReturnUrl(`${previousUrl.pathname}${previousUrl.search ? previousUrl.search : ''}`));
		}
	}, []);

	return (
		<main className={ styles["login"] }>
			<section>
				<Container>
					<LoginForm />
					<LoginAlternate />
				</Container>
			</section>
		</main>
	);
};

export default Login;

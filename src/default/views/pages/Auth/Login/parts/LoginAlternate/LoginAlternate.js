import React from "react";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Link } from "react-router-dom";

import url from "@/router/urls";

import Button from '@Elements/Button/Button';

import styles from '@Pages/Auth/Login/Login.module.sass';

const hostname = window.location.origin;

const LoginAlternate = () => {
	const { t } = useTranslation();

	const currentLang = useSelector((state) => state.app.currentLang);
	const returnUrl = useSelector((state) => state.auth.login.returnUrl);
	const returnURI = `${hostname}${returnUrl ? encodeURIComponent(returnUrl) : ''}`;
	
	const apiURL = useSelector((state) => state.app.config.reactAppApi);
	
	const serviceProviderAdURL = useSelector((state) => state.app.config.activeDirectoryLoginProviderUrl);
	const serviceProviderPionierIdURL = useSelector((state) => state.app.config.pionierIdLoginProviderUrl);
	const facebookLoginIsActive = useSelector((state) => state.app.config.facebookLoginEnabled);
	const googleLoginIsActive = useSelector((state) => state.app.config.googleLoginEnabled);

	return ( (serviceProviderAdURL || serviceProviderPionierIdURL || facebookLoginIsActive || googleLoginIsActive) &&
		<div className={ styles["login-alternative"] }>
			{/* <h2 className={ styles["login-alternative__header"] }>{t('auth.login.alternativeLoginHead')}</h2> */}
			<hr />
			<div className={ styles["login-alternative__button-list"] }>
				{ serviceProviderAdURL && 
				<Button
					variant='external'
					href={ `${serviceProviderAdURL}?lang=${currentLang}&returnUrl=${returnURI}` }
				>
					{t('auth.login.alternativeLogin.uwrad.title')}
					<i className='icon-external' />
				</Button>}
				{ serviceProviderPionierIdURL && 
				<Button
					variant='external'
					href={ `${serviceProviderPionierIdURL}?lang=${currentLang}&returnUrl=${returnURI}` }
				>
					{t('auth.login.alternativeLogin.pionierid.title')}
					<i className='icon-external' />
				</Button>}
				{ facebookLoginIsActive &&
				<Button
					variant='external'
					href={ `${apiURL}/oauth2/authorization/facebook?lang=${currentLang}&returnUrl=${returnURI}` }
				>
					{t('auth.login.alternativeLogin.facebook.title')}
					<i className='icon-external' />
				</Button>}
				{ googleLoginIsActive &&
				<Button
					variant='external'
					href={ `${apiURL}/oauth2/authorization/google?lang=${currentLang}&returnUrl=${returnURI}` }
				>
					{t('auth.login.alternativeLogin.google.title')}
					<i className='icon-external' />
				</Button>}
			</div>
			<h3 className={ styles["login-alternative__subheader"] }>
				<Link to={ url.privacyPolicy }>{t('auth.login.alternativeLoginPrivacy') }</Link>
				<Link to={ url.accessibilityStatement }>{t('auth.login.alternativeLoginAvailability') }</Link>
			</h3>
		</div>
	);
};

export default LoginAlternate;

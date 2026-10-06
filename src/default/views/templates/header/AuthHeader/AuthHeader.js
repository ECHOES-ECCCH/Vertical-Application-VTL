import React from 'react';
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import Container from "react-bootstrap/Container";
import { Link } from "react-router-dom";

import url from "@/router/urls";

import Spinner from "@Elements/Spinner/Spinner";

import styles from '@Templates/header/AuthHeader/AuthHeader.module.sass';

const AuthHeader = () => {
	const { t } = useTranslation();

	const currentAppState = useSelector((state) => state.app);

	return (
		<>
			<Spinner stateFromStore />
			<Container>
				<div className={ styles["auth-header"] }>
					{currentAppState.isError ? (
						<a href={ url.home }>
							<div
								className={ styles["auth-header__logo"] }
							>
								{t('headers.logoAlt') }
							</div>
						</a>
					) : (
						<Link to={ url.home }>
							<div
								className={ styles["auth-header__logo"] }
							>
								{t('headers.logoAlt') }
							</div>
						</Link>
					)}

				</div>
			</Container>
		</>
	);
};

export default AuthHeader;
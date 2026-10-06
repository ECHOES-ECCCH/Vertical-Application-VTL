import React from 'react';
import { useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Container, Row } from "react-bootstrap";

import { appActions } from '@/store/actions/app.actions';
import Button from "@Elements/Button/Button";

import styles from "@Components/Cookies/Cookies.module.sass";

import url from "@/router/urls";

const Cookies = () => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	const setCookiePermission = () => {
		dispatch(appActions.setCookiePermission());
	};
    
	return (
		<div className={ styles["cookies"] }>
			<Container>
				<Row>
					<div className={ styles["cookies__wrapper"] }>
						<div className={ styles["cookies__left"] }>
							<span className={ styles["cookies__left-text"] }>
								<i className='icon-cookies' />
								{t('cookies.text')}
							</span>
							<a
								className={ styles["cookies__left-link"] }
								href={ url.privacyPolicy }
							>
								{t('cookies.link')}
							</a>
						</div>
						<Button 
							className={ styles["cookies__btn"] }
							onClick={ () => setCookiePermission() }
						>
							{t('cookies.agree')}
						</Button>
					</div>
				</Row>
			</Container>
		</div>
	);
};

export default Cookies;
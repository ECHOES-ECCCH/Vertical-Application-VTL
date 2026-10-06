import React from "react";
import { useTranslation } from 'react-i18next';
import { useHistory } from "react-router-dom";
import { Container } from "react-bootstrap";

import Button from "@Elements/Button/Button";

import url from "@/router/urls";

import styles from '@Pages/Error/Errors.module.sass';

const Error403 = () => {
	const { t } = useTranslation();

	const history = useHistory();
	const location = new URL(window.location.href);

	const prevPage = () => {
		history.goBack();
	};

	return (
		<main>
			<Container>
				<div className='block'>
					<div className='block__header'>
						<h1 className='text--title'>{t('error.error403.title')}</h1>
					</div>
					<div className={ styles["nopage--content"] }>
						<p className='text--heading'>{t('error.error403.description')}</p>
					</div>
					<div className={ styles['nopage--links'] }>
						<p className='nopage--paragraph'>{t('error.error403.backText')}</p>
						<Button
							className={ styles['error-link'] }
							variant='link'
							type='button'
							onClick={ () => prevPage() }
						>{t('error.error403.backLinkText')}
						</Button>
						<br />
						<p className='nopage--paragraph'>{t('error.error403.homeText')}</p>
						<Button
							className={ styles['error-link'] }
							variant='link'
							type='button'
							href={ url.home }
						>{t('error.error403.homeLinkText')} {location.hostname}
						</Button>
					</div>
				</div>
			</Container>
		</main>
	);
};

export default Error403;

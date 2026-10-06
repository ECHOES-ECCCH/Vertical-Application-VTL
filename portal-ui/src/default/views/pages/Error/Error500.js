import React, { useEffect }  from "react";
import { useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { Container } from "react-bootstrap";

import { hideModal } from '@/store/actions/modal.actions';

import Button from "@Elements/Button/Button";

import styles from '@Pages/Error/Errors.module.sass';

const Error500 = () => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	useEffect(() => {
		dispatch(hideModal());
	}, [ dispatch ]);

	return (
		<main>
			<Container>
				<div className='block'>
					<div className={ `block__header ${styles['nopage--warning']}` }>
						<h1 className='text--title'>{t('error.error500.title')}</h1>
					</div>
					<div className={ styles["nopage--content"] }>
						<p className='text--heading'>{t('error.error500.description')}</p>
					</div>
					<div className={ styles['nopage--links'] }>
						<Button
							className={ styles['error-link'] }
							variant='link'
							type='button'
							onClick={ () => window.location.reload() }
						>
							{t('error.error500.reloadLinkText')}
						</Button>
					</div>
				</div>
			</Container>
		</main>
	);
};

export default Error500;

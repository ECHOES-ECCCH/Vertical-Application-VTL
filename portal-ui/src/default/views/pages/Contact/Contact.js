import React, { useEffect } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import Markdown from 'react-markdown';
import { Container, Row, Col, Image } from "react-bootstrap";

import { staticPageActions } from "@/store/actions/staticPage.actions";
import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";

import contactImage from '@Assets/images/contact/contactImage.png';
import "@Styles/_static-page.sass";

const Contact = () => {

	const currentLang = useSelector((state) => state.app.currentLang);
	const { t } = useTranslation();

	const contactPage = useSelector((state) => state.staticPage.contactPage);

	const dispatch = useDispatch();

	useEffect(() => {
		dispatch(staticPageActions.getContactPage());
	}, [ dispatch ]);

	const breadcrumbsList = [
		{ label: "home", id: 1 },
		{ label:t('staticPage.contact'), id: 2 },
	];

	return (
		<main className='static-page'>
			<Container>
				<Breadcrumbs breadcrumbsList={ breadcrumbsList } />
				<section className='static-page__content'>
					<Row>
						<Col lg={ 4 }>
							{contactPage !== '' &&
								<Markdown>{ contactPage[currentLang] }</Markdown>}
						</Col>
						<Col lg={ 8 }>
							<Image
								src={ contactImage }
								alt={t('staticPage.contact') }
								fluid
							/>
						</Col>
					</Row>
				</section>
			</Container>
		</main>
	);
};

export default Contact;

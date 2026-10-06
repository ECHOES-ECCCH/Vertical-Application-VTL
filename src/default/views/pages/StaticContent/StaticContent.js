import React, { useEffect, useState } from "react";
import { useLocation } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import Markdown from 'react-markdown';
import { Container, Row, Col } from "react-bootstrap";

import api from "@/services/api";

import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";

import "@Styles/_static-page.sass";

const Regulations = () => {
	
	const currentLang = useSelector((state) => state.app.currentLang);
	const { t } = useTranslation();

	const [ staticText, setStaticText ] = useState(null);

	const location = useLocation();
	const pathname = location.pathname;

	useEffect(() => {
		api.get(`/static-files${pathname}`)
			.then(response => {
				setStaticText(response.data);
			});
	}, [pathname]);

	const toCamelCase = (str) => {
		if (typeof str !== "string" || !str.length) return "";
		return str
			.replace(/[_/]+/g, "-") // normalize delimiters
			.split("-")
			.map((word, i) => i === 0 ? word.toLowerCase() : word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
			.join("");
	};

	const breadcrumbsList = [
		{ label: "home", id: 1 },
		{ label:t(`staticPage.${toCamelCase(pathname.substring(1))}`) , id: 2 },
	];

	return (
		<main className='static-page'>
			<Container>
				<Breadcrumbs breadcrumbsList={ breadcrumbsList } />
				<section className='static-page__content'>
					<Row>
						<Col>
							{ staticText &&
								<Markdown>{ staticText[currentLang] }</Markdown> }
						</Col>
					</Row>
				</section>
			</Container>
		</main>
	);
};

export default Regulations;
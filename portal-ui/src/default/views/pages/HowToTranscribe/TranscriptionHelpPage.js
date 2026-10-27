import React, { useState, useRef } from "react";
import { useTranslation } from 'react-i18next';
import { Container, Row, Col, Tabs, Tab } from "react-bootstrap";

import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";

import About from "@Pages/HowToTranscribe/parts/About";
import HowToStart from "@Pages/HowToTranscribe/parts/HowToStart";
import HowToTranscribe from "@Pages/HowToTranscribe/parts/HowToTranscribe";
import HowToVerify from "@Pages/HowToTranscribe/parts/HowToVerify";

import "@Styles/_static-page.sass";

const HowToTranscribeInfo = () => {
	const { t } = useTranslation();
	
	const [ key, setKey ] = useState('about');	

	const breadcrumbsList = [
		{ label: "home", id: 1 },
		{ label:t('transcriptionHelpPage.title') , id: 2 },
	];

	const scrollToTopRef = useRef();

	return (
		<main
			className='static-page'
			ref={ scrollToTopRef }
		>
			<Container 
				ref={ scrollToTopRef } 
				className=''
			>
				<Breadcrumbs breadcrumbsList={ breadcrumbsList } />

				<h1 className='static-page__header'>
					{t('transcriptionHelpPage.header')}
				</h1>
				<section className='static-page__content'>
					<Row>
						<Col>
							<Tabs
								id='controlled-tab-example'
								activeKey={ key }
								onSelect={ (k) => setKey(k) }
								className='static-page__content--tabs'
							>
								<Tab
									eventKey='about'
									title={t('transcriptionHelpPage.about.title') }
								>
									<About />
								</Tab>
								<Tab
									eventKey='howToStart'
									title={t('transcriptionHelpPage.howToStart.title') }
								>
									<HowToStart />
								</Tab>
								<Tab
									eventKey='howToTranscribe'
									title={t('transcriptionHelpPage.howToTranscribe.title') }
								>
									<HowToTranscribe />
								</Tab>
								<Tab
									eventKey='howToVerify'
									title={t('transcriptionHelpPage.howToVerify.title') }
								>
									<HowToVerify />
								</Tab>
							</Tabs>
							
						</Col>
					</Row>
				</section>
			</Container>
		</main>
	);
};

export default HowToTranscribeInfo;
  
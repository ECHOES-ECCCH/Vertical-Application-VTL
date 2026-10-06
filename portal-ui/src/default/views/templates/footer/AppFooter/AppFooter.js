import React, { useState, useEffect } from "react";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Link } from "react-router-dom";
import { Container } from "react-bootstrap";

import url from "@/router/urls";
import api from "@/services/api";

import styles from "@Templates/footer/AppFooter/AppFooter.module.sass";

const AppFooter = () => {
	const { t } = useTranslation();

	const [ uiBuild, setUiBuild ] = useState({});
	const [ blBuild, setBlBuild ] = useState({});

	const contactUrl = url.contact;
	const privacyPolicyUrl = url.privacyPolicy;
	const accessibilityStatementUrl = url.accessibilityStatement;
	const regulationsUrl = url.regulations;

	const fetchUiBuild = async () => {
		await fetch('/buildinfo.json').then(response => response.json()).then(response => setUiBuild({...response, buildDate: response.buildDate*1000})); 
	};

	useEffect(()=>{
		if(process.env.REACT_APP_DEV_MODE){
			fetchUiBuild();
			api.get('/healthcheck/buildinfo').then(response => setBlBuild({...response.data, buildDate: response.data.buildDate*1000})).catch(() => {});
		}
	},[true]);

	return (
		<footer className={ styles["footer"] } style={process.env.REACT_APP_DEV_MODE ? {position: "relative"} : null}  >
			<Container className={styles["footer__inner"]}>
				<div className={styles["footer__links"]}>
					<div>
						<div className={styles["footer__block--header"]}>
							<span>{t('headers.menu.transcriptions')}</span>
						</div>
						<div className={styles["footer__block"]}>
							<Link to={{pathname: url.transcriptionsUser, state: { isProtected: true } }}>{t('headers.menu.myProjects') }</Link>
						</div>
						<div className={styles["footer__block"]}>
							<Link to={{pathname: url.transcriptions, state: { isProtected: false } }}>{t('headers.menu.allProjects') }</Link>
						</div>
					</div>
					<div>
						<div className={styles["footer__block--header"]}>
							<span>{t('headers.informations')}</span>
						</div>
						<div className={styles["footer__block"]}>
							<a href={contactUrl}>{t('footer.links.contact')}</a>
						</div>
						<div className={styles["footer__block--light-header"]}>
							<span>{t('headers.informationsMenu.helpersInfo')}</span>
						</div>
						<div className={styles["footer__block"]}>
							<Link to={url.howToTranscribe}>{t('headers.informationsMenu.howTo')}</Link>
						</div>
						<div className={styles["footer__block"]}>
							<Link to={url.map}>{t('headers.informationsMenu.pageMap')}</Link>
						</div>
						<div className={styles["footer__block--light-header"]}>
							<span>{t('headers.informationsMenu.privacyInfo')}</span>
						</div>
						<div className={styles["footer__block"]}>
							<a href={privacyPolicyUrl}>{t('footer.links.privacyPolicy')}</a>
						</div>
						<div className={styles["footer__block"]}>
							<a href={accessibilityStatementUrl}>{t('headers.informationsMenu.availability')}</a>
						</div>
						<div className={styles["footer__block"]}>
							<a href={regulationsUrl}>{t('headers.informationsMenu.terms')}</a>
						</div>
					</div>

					{process.env.REACT_APP_DEV_MODE && 
						<div 
							className={styles["footer__build"]}
						>
							<div>
								<p>UI BUILD</p>
								<p>ID: {uiBuild.commitId}</p>
								<p>DATE: {new Date(parseInt(uiBuild.buildDate)).toLocaleString()}</p>
							</div>

							<div>
								<p>BL BUILD</p>
								<p>ID: {blBuild.commitId}</p>
								<p>DATE: {new Date(parseInt(blBuild.buildDate)).toLocaleString()}</p>								
							</div>
							
						</div>}
				</div>
				
			</Container>	
			<div className={styles["footer__rights"]}>
				<span>
					{t('footer.rights.copyright')} &copy; 2021 {t('footer.rights.client')} {t('footer.rights.createdBy')}
					<a
						href={t('footer.rights.pcss.url')}
						rel='noreferrer'
						target='_blank'
					>
						{t('footer.rights.pcss.title')}
					</a>
				</span>
			</div>		
		</footer>
	);
};

export default AppFooter;
import React from "react";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Container } from "react-bootstrap";

import url from "@/router/urls";

import LinkButton from "@Elements/Button/LinkButton";

import styles from "@Pages/Home/parts/MainDescription/MainDescription.module.sass";

const MainDescription = () => {

	const { t } = useTranslation();

	const formsAutorization = useSelector((state) => state.app.config.authorization == "FORMS");
	const authorizationRegisterUrl = useSelector((state) => state.app.config.reactAppApi + state.app.config.authorizationRegisterUrl);

	return (
		<>
			<section className={ styles["main"] }>
				<div className={styles["main__container"]}>
					<div className={styles["main__item--first"]}>
						<div className={styles["main__description--header"]}>
							<h1 className={styles["main__description--title"]}>{t('headers.title')}</h1>
						</div>
						<div className={ styles["main__description"] }>
							<p className={styles["main__description--text"]}>{t('headers.description')}</p>
							<hr />
							<Link
								className={styles["main__description--link"]}
								to={url.howToTranscribe}
							>
								{t('headers.help')}
							</Link>
						</div>
						<div className={styles["main__description--buttons"]}>
							<Link className={`${styles["main__description--upper-button"]} btn`} to={{pathname: url.transcription.new, state: { isProtected: true } }}>{t('headers.menu.newProject') }</Link>
						</div>

					</div>
				</div>

			</section>

			<section className={ styles["description"] }>
				<Container>
					<p className={styles["description__header"]}>{t('headers.descriptions.title')}</p>

					<div className={styles["description__items"]}>
						<div className={styles["description__item"]}>
							<span>1</span>
							<p>
								<a href={formsAutorization ? url.auth.register : authorizationRegisterUrl + "?returnUrl=" + encodeURIComponent(window.location)}>
									{t('headers.menu.createAccount')}
								</a>
								{t('headers.descriptions.item1')}
							</p>
						</div>
						<div className={styles["description__item"]}>
							<span>2</span>
							<p>
								{t('headers.descriptions.item2_1')}
								<a href={url.transcription.new}>
									{t('headers.menu.newProject')}
								</a>
								{t('headers.descriptions.item2_2')}
							</p>
						</div>
						<div className={styles["description__item"]}><span>3</span><p>{t('headers.descriptions.item3')}</p></div>
						<div className={styles["description__item"]}><span>4</span><p>{t('headers.descriptions.item4')}</p></div>
						<div className={styles["description__item"]}><span>5</span><p>{t('headers.descriptions.item5')}</p></div>
						<div className={styles["description__item"]}><span>6</span><p>{t('headers.descriptions.item6')}</p></div>
					</div>
					<LinkButton
						variant='primary'
						className={styles["main__description--down-button"]}
						to={url.howToTranscribe}
					>
						{t('headers.help')}
					</LinkButton>
				</Container>
			</section>
		</>	);
};

export default MainDescription;

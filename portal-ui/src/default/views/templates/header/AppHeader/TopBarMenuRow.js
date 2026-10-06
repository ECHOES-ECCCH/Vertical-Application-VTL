import React from "react";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Dropdown, Row } from 'react-bootstrap';
import { Link } from 'react-router-dom';

import url from "@/router/urls";

import HeaderMyAccount from "@Templates/header/AppHeader/HeaderMyAccount";

import styles from "@Templates/header/AppHeader/HeaderTopBar.module.sass";

const TopBarMenuRow = () => {
	const { t } = useTranslation();

	const contactUrl = url.contact;
	const privacyPolicyUrl = url.privacyPolicy;
	const accessibilityStatementUrl = url.accessibilityStatement;
	const regulationsUrl = url.regulations;

	return (
		<Row className={styles["top-bar__row--menu"]}>
			<div className={styles["top-bar__item"]}>
				<Dropdown className={styles["lang-dropdown"]} alignRight>

					<Dropdown.Toggle
						id='dropdown-custom-11'
						className={styles["dropdown__button"]}
						variant={styles["dropdown__button"]}
					>{t('headers.menu.projects')}
					</Dropdown.Toggle>

					<Dropdown.Menu className={styles["lang-dropdown__menu"]}>
						<Link
							className='dropdown-item'
							to={{pathname: url.transcriptionsUser, state: { isProtected: true } }}
						>{t('headers.menu.myProjects') }
						</Link>
						<Link
							className='dropdown-item'
							to={{pathname: url.transcriptions, state: { isProtected: false } }}
						>{t('headers.menu.allProjects') }
						</Link>
						<Dropdown.Item
							className={styles["dropdown__button--divider"]}
							disabled
						/>
						<Link
							className='dropdown-item'
							to={{pathname: url.transcription.new, state: { isProtected: true } }}
						>{t('headers.menu.newProject') }
						</Link>
					</Dropdown.Menu>
				</Dropdown>
			</div>

			<div className={styles["top-bar__item"]}>
				<Dropdown className={styles["lang-dropdown"]} alignRight>
					<Dropdown.Toggle
						id='dropdown-custom-11'
						className={styles["dropdown__button"]}
						variant={styles["dropdown__button"]}
					>{t('headers.informations')}
					</Dropdown.Toggle>

					<Dropdown.Menu className={styles["lang-dropdown__menu"]}>
						<div className={styles["dropdown__wrapper"]}>
							<Dropdown.Item
								disabled                                
								className={styles["dropdown__button--disabled-black"]}
							>
								{t('headers.informationsMenu.contactInfo')}
							</Dropdown.Item>
							<Dropdown.Item
								href={contactUrl}
							>
								{t('headers.informationsMenu.contact')}
							</Dropdown.Item>
						</div>

						<div className={styles["dropdown__wrapper"]}>
							<Dropdown.Item
								disabled
								className={styles["dropdown__button--disabled-black"]}
							>
								{t('headers.informationsMenu.helpersInfo')}
							</Dropdown.Item>
							<Dropdown.Item as={Link} to={url.howToTranscribe}>
								{t('headers.informationsMenu.howTo')}
							</Dropdown.Item>
							<Dropdown.Item as={Link} to={url.map}>
								{t('headers.informationsMenu.pageMap')}
							</Dropdown.Item>
						</div>

						<div className={styles["dropdown__wrapper"]}>
							<Dropdown.Item
								disabled
								className={styles["dropdown__button--disabled-black"]}
							>
								{t('headers.informationsMenu.privacyInfo')}
							</Dropdown.Item>
							<Dropdown.Item 
								href={privacyPolicyUrl} 
							>
								{t('headers.informationsMenu.privacy')}
							</Dropdown.Item>
							<Dropdown.Item 
								href={accessibilityStatementUrl} 
							>
								{t('headers.informationsMenu.availability')}
							</Dropdown.Item>
							<Dropdown.Item 
								href={regulationsUrl}
							>
								{t('headers.informationsMenu.terms')}
							</Dropdown.Item>
						</div>

					</Dropdown.Menu>
				</Dropdown>
			</div>

			<div className={styles["top-bar__item"]}>
				<HeaderMyAccount />
			</div>
		</Row>
	);
};

export default TopBarMenuRow;
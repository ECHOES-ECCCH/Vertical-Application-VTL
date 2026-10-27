import React from "react";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Container } from "react-bootstrap";
import { Link } from "react-router-dom";

import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import styles from "@Pages/Map/Map.module.css";

import url from "@/router/urls";
import { CheckSecurityGroupPermission } from "@/permissions/SecurityGroups";

const Map = () => {
	const { t } = useTranslation();

	const isLoggedIn = useSelector((state) => state.auth.credentials.isLoggedIn);
	const UserType = useSelector((state) => state.auth.credentials.securityGroup);

	const userProfile = url.auth.profile;

	const contactUrl = url.contact;
	const privacyPolicyUrl = url.privacyPolicy;
	const accessibilityStatementUrl = url.accessibilityStatement;
	const regulationsUrl = url.regulations;

	const breadcrumbsList = [
		{ label: "home", id: 1 },
		{ label:t('headers.informationsMenu.pageMap'), id: 2 },
	];

	return (
		<main className={styles["map"]}>
			<Container>
				<Breadcrumbs breadcrumbsList={ breadcrumbsList } />
				<section className={styles["tree"]}>
					<ul>{t('breadcrumbs.home')}
						<li>
							<ul>{t('headers.menu.transcriptions')}
								<li><Link to={{pathname: url.transcriptionsUser, state: { isProtected: true } }}>{t('headers.menu.myProjects') }</Link></li>
								<li><Link to={{pathname: url.transcriptions, state: { isProtected: false } }}>{t('headers.menu.allProjects') }</Link></li>
							</ul>
						</li>
						<li>
							<ul>{t('headers.informations')}
								<li><a href={contactUrl}>{t('footer.links.contact')}</a></li>
								<li>
									<ul>{t('headers.informationsMenu.helpersInfo')}
										<li><Link to={url.howToTranscribe}>{t('headers.informationsMenu.howTo')}</Link></li>
										<li><span className={styles["selected-page"]}>{t('headers.informationsMenu.pageMap')}</span></li>
									</ul>
								</li>
								<li>
									<ul>{t('headers.informationsMenu.privacyInfo')}
										<li><a href={privacyPolicyUrl}>{t('footer.links.privacyPolicy')}</a></li>
										<li><a href={accessibilityStatementUrl}>{t('headers.informationsMenu.availability')}</a></li>
										<li><a href={regulationsUrl}>{t('headers.informationsMenu.terms')}</a></li>
									</ul>
								</li>
							</ul>
						</li>
						<li>
							<ul>{t('headers.menu.myAccountMenu.myAccount')}
								<li>
									<ul>{t("default:headers.menu.myAccountMenu.general")}
										<li>
											<a href={userProfile}>
												{t('headers.menu.myAccountMenu.myProfile')}
											</a>
										</li>
									</ul>
								</li>
								<li>
									<ul>{t('headers.menu.myAccountMenu.historyTexts')}
										<li>
											<Link to={url.transcriptionsUser}>
												{t('headers.menu.myAccountMenu.historyTextsMyProjects')}
											</Link>
										</li>
										{isLoggedIn && CheckSecurityGroupPermission("canAccessProjectsDashboard", UserType) && 
										<li>
											<Link to={url.auth.admin.projects}>
												{t('headers.menu.myAccountMenu.serviceManagement')}
											</Link>
										</li>}
										{isLoggedIn && CheckSecurityGroupPermission("canAccessUsersDashboard", UserType) && 
										<li>
											<Link to={url.auth.admin.users}>
												{t('headers.menu.myAccountMenu.usersManagement')}
											</Link>
										</li>}
									</ul>
								</li>
							</ul>
						</li>
					</ul>
				</section>
			</Container>
		</main>
	);
};

export default Map;

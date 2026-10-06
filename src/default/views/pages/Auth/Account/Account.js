import React, { useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from 'react-i18next';
import { Container } from "react-bootstrap";

import url from "@/router/urls";

import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import SearchTranscriptions from "@Elements/Search/SearchTranscriptions";
import SearchTypeEnum from "@/utils/SearchTypeEnum";

import styles from "@Pages/Auth/Account/Account.module.sass";

import iconMenu1 from "@Assets/images/account/AccountMenu-1.svg";
import iconMenu3 from "@Assets/images/account/AccountMenu-3.svg";
import iconMenu6 from "@Assets/images/account/AccountMenu-6.svg";

const Account = () => {
	const { t } = useTranslation();

	const userProfile = url.auth.profile;

	const [ searchString, setSearchString ] = useState("");
	const [ useMetadata, setUseMetadata ] = useState(SearchTypeEnum.ALL.toString());

	const breadcrumbsList = [
		{ id: 1, label: "home" },
		{ id: 2, label:t('breadcrumbs.account') },
	];

	return (
		<main className={ styles["account"] }>
			<SearchTranscriptions
				setSearchString={ setSearchString }
				searchString={ searchString }
				setUseMetadata={ setUseMetadata }
			/>
			<Container className={ styles["container"] }>
				<Breadcrumbs breadcrumbsList={ breadcrumbsList } />

				<h1 className={ styles["account__header--title"] }>
					{t('breadcrumbs.account')} 
				</h1>

				<div className={ styles["account__grid"]}>
					<div className={ styles["account__grid--item"]}>
						<div>
							<img src={iconMenu1}/>
						</div>
						<div>
							<h4>{t('headers.menu.myAccountMenu.general')}</h4>
							<div>
								<a href={userProfile}>
									{t('headers.menu.myAccountMenu.profile')}
								</a>
							</div>
						</div>
					</div>
					<div className={ styles["account__grid--item"]}>
						<div>
							<img src={iconMenu3}/>
						</div>
						<div>
							<h4>{t('headers.menu.myAccountMenu.historyTexts')}</h4>
							<div>
								<Link to={url.transcriptionsUser}>
									{t('headers.menu.myAccountMenu.historyTextsMyProjects')}
								</Link>
							</div>
							<div>
								<Link to={url.auth.admin.projects}>
									{t('headers.menu.myAccountMenu.serviceManagement')}
								</Link>
							</div>
						</div>
					</div>
					<div className={ styles["account__grid--item"]}>
						<div>
							<img src={iconMenu6}/>
						</div>
						<div>
							<h4>{t('headers.menu.myAccountMenu.management')}</h4>
							<div>
								<Link to={url.auth.admin.users}>
									{t('headers.menu.myAccountMenu.usersManagement')}
								</Link>
							</div>
						</div>
					</div>
				</div>
			</Container>
		</main>
	);
};

export default Account;

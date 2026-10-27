import React from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Dropdown, Button } from 'react-bootstrap';

import { authActions } from "@/store/actions/auth.actions";
import { CheckSecurityGroupPermission } from "@/permissions/SecurityGroups";

import styles from '@Templates/header/AppHeader/HeaderMyAccount.module.sass';
import styles2 from '@Templates/header/AppHeader/HeaderTopBar.module.sass';
import url from "@/router/urls";

const HeaderMyAccount = () => {
	const { t } = useTranslation();
	const isLoggedIn = useSelector((state) => state.auth.credentials.isLoggedIn);
	const isProtected = useSelector((state) => state.auth.path.isProtected);
	const formsAutorization = useSelector((state) => state.app.config.authorization == "FORMS");
	const authorizationLoginUrl = useSelector((state) => state.app.config.reactAppApi + state.app.config.authorizationLoginUrl);
	const reactAppApi = useSelector((state) => state.app.config.reactAppApi);
	const authorizationRegisterUrl = useSelector((state) => state.app.config.reactAppApi + state.app.config.authorizationRegisterUrl);
	const UserType = useSelector((state) => state.auth.credentials.securityGroup);
	const dispatch = useDispatch();

	const userProfile = url.auth.profile;

	const signOut = () => {
		dispatch(authActions.signOut(reactAppApi, isProtected, formsAutorization));
	};

	return (
		<Dropdown className={ styles["myacount-dropdown"] } alignRight>

			<Dropdown.Toggle
				id='dropdown-custom-1'
				className={styles["myacount-dropdown__button"] }
				variant={styles["myacount-dropdown__button"] }
			>{isLoggedIn ? t('headers.menu.myAccount') : t('headers.menu.logIn')}
			</Dropdown.Toggle>

			<Dropdown.Menu className={styles["myacount-dropdown__menu"]}>
				{isLoggedIn ? (
					<>
						<div className={styles2["dropdown__wrapper"]}>
							<Dropdown.Item
								disabled
								className={styles2["dropdown__button--disabled-grey"]}
							>
								{t('headers.menu.myAccountMenu.general')}
							</Dropdown.Item>
							<Link
								className={`dropdown-item ${styles["myacount-dropdown__item"]}`}
								to={{pathname: url.auth.account, state: { isProtected: true } }}
							>
								{ t('headers.menu.myAccountMenu.myAccount') }
							</Link>
							<Dropdown.Item
								href={userProfile} 
								className={`${styles["myacount-dropdown__item"]}`}
							>
								{t('headers.menu.myAccountMenu.myProfile')}
							</Dropdown.Item>
						
						</div>
						
						<div className={styles2["dropdown__wrapper"]}>
							<Dropdown.Item
								disabled
								className={styles2["dropdown__button--disabled-grey"]}
							>
								{t('headers.menu.myAccountMenu.historyTexts')}
							</Dropdown.Item>
							<Dropdown.Item
								href={url.transcriptionsUser}
								className={`${styles["myacount-dropdown__item"]}`}
							>
								{t('headers.menu.myAccountMenu.historyTextsMyProjects')}
							</Dropdown.Item>
							{CheckSecurityGroupPermission("canAccessProjectsDashboard", UserType) &&
							<Dropdown.Item
								href={url.auth.admin.projects}
								className={`${styles["myacount-dropdown__item"]}`}
							>
								{t('headers.menu.myAccountMenu.serviceManagement')}
							</Dropdown.Item>}
							{CheckSecurityGroupPermission("canAccessUsersDashboard", UserType) &&
							<Dropdown.Item
								href={url.auth.admin.users}
								className={`${styles["myacount-dropdown__item"]}`}
							>
								{t('headers.menu.myAccountMenu.usersManagement')}
							</Dropdown.Item>}
						</div>

						<div className={`${styles["myacount-dropdown__item--logout"]}`}>
							<Button variant='secondary' onClick={() => signOut()}>
								{t('headers.menu.logOut')}
							</Button>
						</div>
					</>
				) : (
					<>
						<Dropdown.Item
							className={`${styles["myacount-dropdown__item"]}`}
							href={formsAutorization ? url.auth.login : authorizationLoginUrl + "?returnUrl=" + encodeURIComponent(window.location)}
						>
							{t('headers.menu.logIn')}
						</Dropdown.Item>
						<Dropdown.Item
							className={`${styles["myacount-dropdown__item"]}`}
							href={formsAutorization ? url.auth.register : authorizationRegisterUrl + "?returnUrl=" + encodeURIComponent(window.location)}
						>
							{t('headers.menu.createAccount')}
						</Dropdown.Item>
					</>
				)}
			</Dropdown.Menu>
		</Dropdown>
	);
};

export default HeaderMyAccount;
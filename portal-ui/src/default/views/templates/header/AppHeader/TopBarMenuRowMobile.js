import React, { useContext } from "react";
import { useTranslation } from 'react-i18next';
import { Row, useAccordionToggle } from "react-bootstrap";
import { useSelector, useDispatch } from 'react-redux';
import Accordion from 'react-bootstrap/Accordion';
import AccordionContext from 'react-bootstrap/AccordionContext';
import Card from 'react-bootstrap/Card';
import Button from 'react-bootstrap/Button';
import { Link } from 'react-router-dom';
import PropTypes from "prop-types";

import { authActions } from "@/store/actions/auth.actions";
import { CheckSecurityGroupPermission } from "@/permissions/SecurityGroups";

import url from "@/router/urls";

import styles from "@Templates/header/AppHeader/HeaderTopBar.module.sass";
import styles2 from '@Templates/header/AppHeader/HeaderMyAccount.module.sass';

const CustomToggle = ({ children, eventKey, callback }) => {
	const currentEventKey = useContext(AccordionContext);
  
	const decoratedOnClick = useAccordionToggle(
		eventKey,
		() => callback && callback(eventKey),
	);
  
	const isCurrentEventKey = currentEventKey === eventKey;
  
	return (
		<Button className={styles["card-header-toggle"]} variant='link' onClick={decoratedOnClick}>
			{ children }
			{ isCurrentEventKey ? <i className='icon-arrow-up-full' /> : <i className='icon-arrow-down-full' /> }
		</Button>
	);
};

CustomToggle.propTypes = {
	children: PropTypes.array, 
	eventKey: PropTypes.string, 
	callback: PropTypes.func, 
};

const TopBarMenuRowMobile = () => {
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

	const contactUrl = url.contact;
	const privacyPolicyUrl = url.privacyPolicy;
	const accessibilityStatementUrl = url.accessibilityStatement;
	const regulationsUrl = url.regulations;

	const signOut = () => {
		dispatch(authActions.signOut(reactAppApi, isProtected, formsAutorization));
	};

	return (
		<>
			<Row className={styles["top-bar__row--menu"] + " " + styles["mobile-top-bar"]}>
				<Accordion defaultActiveKey={null}>
					<Card className={styles["custom-card"]}>
						<Card.Header className={styles["custom-card-header"]}>
							<CustomToggle eventKey='0'>{t('headers.menu.projects')}</CustomToggle>
						</Card.Header>
						<Accordion.Collapse eventKey='0'>
							<Card.Body>
								<Link
									className='dropdown-item'
									to={{pathname: url.transcriptionsUser, state: { isProtected: true } }}
								>
									{t('headers.menu.myProjects') }
								</Link>
								<Link
									className='dropdown-item'
									to={{pathname: url.transcriptions, state: { isProtected: false } }}
								>
									{t('headers.menu.allProjects') }
								</Link>
								<hr className={styles["mobile-top-bar--divider"]}/>
								<Link
									className='dropdown-item'
									to={{pathname: url.transcription.new, state: { isProtected: true } }}
								>
									{t('headers.menu.newProject') }
								</Link>
							</Card.Body>
						</Accordion.Collapse>
					</Card>
					<Card className={styles["custom-card"]}>
						<Card.Header className={styles["custom-card-header"]}>
							<CustomToggle eventKey='1'>{t('headers.informations')}</CustomToggle>
						</Card.Header>
						<Accordion.Collapse eventKey='1'>
							<Card.Body>
								<div className={styles["dropdown__wrapper"]}>
									<p className={'dropdown-item ' + styles["mobile-top-bar--label"]}>
										{t('headers.informationsMenu.contactInfo')}
									</p>
									<a
										className='dropdown-item'
										href={contactUrl}
									>
										{t('headers.informationsMenu.contact')}
									</a>
									<p className={'dropdown-item ' + styles["mobile-top-bar--label"]}>
										{t('headers.informationsMenu.helpersInfo')}
									</p>
									<Link
										className='dropdown-item'
										to={{pathname: url.howToTranscribe, state: { isProtected: false } }}
									>
										{t('headers.informationsMenu.howTo')}
									</Link>
									<Link
										className='dropdown-item'
										to={{pathname: url.map, state: { isProtected: false } }}
									>
										{t('headers.informationsMenu.pageMap')}
									</Link>
									<p className={'dropdown-item ' + styles["mobile-top-bar--label"]}>
										{t('headers.informationsMenu.privacyInfo')}
									</p>
									<a
										className='dropdown-item'
										href={privacyPolicyUrl}
									>
										{t('headers.informationsMenu.privacy')}
									</a>
									<a
										className='dropdown-item'
										href={accessibilityStatementUrl}
									>
										{t('headers.informationsMenu.availability')}
									</a>
									<a
										className='dropdown-item'
										href={regulationsUrl}
									>
										{t('headers.informationsMenu.terms')}
									</a>
								</div>
							</Card.Body>
						</Accordion.Collapse>
					</Card>
					<Card className={styles["custom-card"]}>
						<Card.Header className={styles["custom-card-header"]}>
							<CustomToggle eventKey='4'>{isLoggedIn ? t('headers.menu.myAccount') : t('headers.menu.logIn')}</CustomToggle>
						</Card.Header>
						<Accordion.Collapse eventKey='4'>
							<Card.Body>
								{isLoggedIn ? (
									<>
										<div className={styles["dropdown__wrapper"]}>
											<p className={'dropdown-item ' + styles["mobile-top-bar--label"]}>
												{t('headers.menu.myAccountMenu.general')}
											</p>
											<Link
												className='dropdown-item'
												to={{pathname: url.auth.account, state: { isProtected: true } }}
											>
												{ t('headers.menu.myAccountMenu.myAccount') }
											</Link>
											<a
												className='dropdown-item'
												href={userProfile}
											>
												{ t('headers.menu.myAccountMenu.myProfile') }
											</a>
										</div>

										<div className={styles["dropdown__wrapper"]}>
											<p className={'dropdown-item ' + styles["mobile-top-bar--label"]}>
												{t('headers.menu.myAccountMenu.historyTexts')}
											</p>
											<Link
												className='dropdown-item'
												to={{pathname: url.transcriptionsUser}}
											>
												{t('headers.menu.myAccountMenu.historyTextsMyProjects')}
											</Link>
											{CheckSecurityGroupPermission("canAccessProjectsDashboard", UserType) &&
											<Link
												className='dropdown-item'
												to={{pathname: url.auth.admin.projects}}
											>
												{t('headers.menu.myAccountMenu.serviceManagement')}
											</Link>}
											{CheckSecurityGroupPermission("canAccessUsersDashboard", UserType) &&
											<Link
												className='dropdown-item'
												to={{pathname: url.auth.admin.users}}
											>
												{t('headers.menu.myAccountMenu.usersManagement')}
											</Link>}
										</div>

										<div className={`${styles2["myacount-dropdown__item--logout"]}`}>
											<Button variant='secondary' onClick={() => signOut()}>
												{t('headers.menu.logOut')}
											</Button>
										</div>
									</>
								) : (
									<>
										<a
											className='dropdown-item'
											href={formsAutorization ? url.auth.login : authorizationLoginUrl + "?returnUrl=" + encodeURIComponent(window.location)}
										>
											{t('headers.menu.logIn')}
										</a>
										<a
											className='dropdown-item'
											href={formsAutorization ? url.auth.register : authorizationRegisterUrl + "?returnUrl=" + encodeURIComponent(window.location)}
										>
											{t('headers.menu.createAccount')}
										</a>
									</>
								)}
							</Card.Body>
						</Accordion.Collapse>
					</Card>
				</Accordion>
			</Row>
		</>
	);
};

export default TopBarMenuRowMobile;
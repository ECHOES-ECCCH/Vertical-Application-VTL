import React, { useState, useEffect } from "react";
import { useLocation, Link } from "react-router-dom";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';

import url from "@/router/urls";

import HeaderTopBar from "@Templates/header/AppHeader/HeaderTopBar";
import Spinner from "@Elements/Spinner/Spinner";

import styles from "@Templates/header/AppHeader/AppHeader.module.sass";
import "@Styles/_navbar.sass";

const AppHeader = () => {
	const { t } = useTranslation();

	const isError = useSelector((state) => state.app.isError);
	const location = useLocation();

	const [ collapsed, setCollapsed ] = useState(true);

	const [isMobile, setIsMobile] = useState(window.innerWidth > 991.98);

	const updateMedia = () => {
		setIsMobile(window.innerWidth > 991.98);
	};
  
	useEffect(() => {
		window.addEventListener("resize", updateMedia);
		return () => window.removeEventListener("resize", updateMedia);
	});

	const toggleNavbar = () => {
		setCollapsed(!collapsed);
	};

	const pathName = location.pathname;

	return (
		<>
			<Spinner stateFromStore />

			<div 
				className={ pathName !== "/" || isError ? `${styles["header__subpage"]}` : `${styles["test"]}`}
			>
				<div className={ `${styles["header"]}` }>
					<div className={styles["header__inner"]}>
						<a href='https://lab.dariah.pl/'>
							<div
								className={ styles["header__logo"] }
							>
								{t('headers.logoAlt')}
							</div>
						</a>
						<Link to={url.home} style={{ "marginRight": "auto" }}>
							<div
								className={ styles["header__logo-wlt"] }
							>
								{t('headers.wltLogoAlt')}
							</div>
						</Link>

						<div
							className={!isMobile ? styles["header__ue-logo"] : ""}
						/>

						<HeaderTopBar collapsed={collapsed} />

						<button
							type='button'
							aria-label='Toggle navigation'
							className='navbar__menu--hamburger'
							onClick={toggleNavbar}
						>
							<span />
							<span />
							<span />
						</button>
					</div>
				</div>
			</div>
		</>
	);
};

export default AppHeader;
import React, { useEffect, useState } from "react";
import PropTypes from 'prop-types';
import { Row, Container } from "react-bootstrap";

import HeaderTopLang from "@Templates/header/AppHeader/HeaderTopLang";

import styles from "@Templates/header/AppHeader/HeaderTopBar.module.sass";

import TopBarMenuRow from "@Templates/header/AppHeader/TopBarMenuRow";
import TopBarMenuRowMobile from "@Templates/header/AppHeader/TopBarMenuRowMobile";

const HeaderTopBar = ({ setLang, collapsed }) => {
	const [isMobile, setIsMobile] = useState(window.innerWidth > 991.98);

	const updateMedia = () => {
		setIsMobile(window.innerWidth > 991.98);
	};

	useEffect(() => {
		window.addEventListener("resize", updateMedia);
		return () => window.removeEventListener("resize", updateMedia);
	});

	return (
		<div className={ `${collapsed ? styles["top-bar__collapsed"] : styles["top-bar"]
            }` }
		>
			<Container className={ styles["top-bar__container"] }>
				<Row className={ styles["top-bar__row"] }>
					<div className={ styles["top-bar__item--access"] } />
					<div className={ styles["top-bar__item"] + " " + styles["top-bar__item--lang"] }>
						<HeaderTopLang setLang={ setLang } />
					</div>

					<div
						className={isMobile ? styles["top-bar__ue-logo"] : ""}
					/>
				</Row>

				{isMobile ? <TopBarMenuRow/> : <TopBarMenuRowMobile/>}
			</Container>
		</div>
	);
};

HeaderTopBar.propTypes = {
	setLang: PropTypes.func,
	collapsed: PropTypes.bool,
};

export default HeaderTopBar;

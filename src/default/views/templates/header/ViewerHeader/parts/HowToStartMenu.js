import React from 'react';
import { Dropdown } from 'react-bootstrap';
import { useTranslation } from 'react-i18next';

import { Link } from "react-router-dom";

import url from "@/router/urls";

const HowToStartMenu = () => {
	
	const { t } = useTranslation();
	
	return (
		<Dropdown.Menu className='viewer-header__how-to-start-dropdown__menu'>
			<Link
				className='dropdown-item viewer-header__how-to-start-dropdown__item'
				to={{ pathname: url.howToTranscribe }}
			>{t('transcriptionHelpPage.howToTranscribe.title')}
			</Link>
			<Link
				className='dropdown-item viewer-header__how-to-start-dropdown__item'
				to={{ pathname: url.contact }}
			>{t('headers.informationsMenu.contact')}
			</Link>
		</Dropdown.Menu>
	);
};

export default HowToStartMenu;
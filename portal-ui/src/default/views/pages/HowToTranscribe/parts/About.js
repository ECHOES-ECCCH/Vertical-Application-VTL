import React from "react";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import parse from "html-react-parser";

import "@Styles/_static-page.sass";

const About = () => {

	const currentLang = useSelector((state) => state.app.currentLang);
	const { t } = useTranslation();
	
	return (
		<div className='static-page__content--instruction'>
			<p>{parse(t('transcriptionHelpPage.howToTranscribe.instruction_1'))}</p>
			<p>{parse(t('transcriptionHelpPage.howToTranscribe.instruction_2'))}</p>
			<p>{parse(t('transcriptionHelpPage.howToTranscribe.instruction_3'))}</p>
			<p>{parse(t('transcriptionHelpPage.howToTranscribe.instruction_4'))}</p>
			<p>{parse(t('transcriptionHelpPage.howToTranscribe.instruction_5'))}</p>
		</div>
	);
};

export default About;
  
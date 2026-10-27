import React from "react";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Link } from "react-router-dom";

import url from "@/router/urls";

import "@Styles/_static-page.sass";

import start1pl from '@Assets/images/transcriptionHelpPage/howToStart/step1pl.png';
import start1en from '@Assets/images/transcriptionHelpPage/howToStart/step1en.png';
import start1de from '@Assets/images/transcriptionHelpPage/howToStart/step1de.png';

import start2pl from '@Assets/images/transcriptionHelpPage/howToStart/step2pl.png';
import start2en from '@Assets/images/transcriptionHelpPage/howToStart/step2en.png';
import start2de from '@Assets/images/transcriptionHelpPage/howToStart/step2de.png';

import start3pl from '@Assets/images/transcriptionHelpPage/howToStart/step3pl.png';
import start3en from '@Assets/images/transcriptionHelpPage/howToStart/step3en.png';
import start3de from '@Assets/images/transcriptionHelpPage/howToStart/step3de.png';

import start4pl from '@Assets/images/transcriptionHelpPage/howToStart/step4pl.png';
import start4en from '@Assets/images/transcriptionHelpPage/howToStart/step4en.png';
import start4de from '@Assets/images/transcriptionHelpPage/howToStart/step4de.png';

import start5pl from '@Assets/images/transcriptionHelpPage/howToStart/step5pl.png';
import start5en from '@Assets/images/transcriptionHelpPage/howToStart/step5en.png';
import start5de from '@Assets/images/transcriptionHelpPage/howToStart/step5de.png';

import start6pl from '@Assets/images/transcriptionHelpPage/howToStart/step6pl.png';
import start6en from '@Assets/images/transcriptionHelpPage/howToStart/step6en.png';
import start6de from '@Assets/images/transcriptionHelpPage/howToStart/step6de.png';

import start7pl from '@Assets/images/transcriptionHelpPage/howToStart/step7pl.png';
import start7en from '@Assets/images/transcriptionHelpPage/howToStart/step7en.png';
import start7de from '@Assets/images/transcriptionHelpPage/howToStart/step7de.png';

import start8pl from '@Assets/images/transcriptionHelpPage/howToStart/step8pl.png';
import start8en from '@Assets/images/transcriptionHelpPage/howToStart/step8en.png';
import start8de from '@Assets/images/transcriptionHelpPage/howToStart/step8de.png';

const HowToStart = () => {

	const currentLang = useSelector((state) => state.app.currentLang);
	const { t } = useTranslation();
    
	const imagesStart = {
		step1: { pl: start1pl, en: start1en , de: start1de },
		step2: { pl: start2pl, en: start2en , de: start2de },
		step3: { pl: start3pl, en: start3en , de: start3de },
		step4: { pl: start4pl, en: start4en , de: start4de },
		step5: { pl: start5pl, en: start5en , de: start5de },
		step6: { pl: start6pl, en: start6en , de: start6de },
		step7: { pl: start7pl, en: start7en , de: start7de },
		step8: { pl: start8pl, en: start8en , de: start8de },
	};
    
	const howToStartSteps = [
		{ title: 'step1title', desc: 'step1desc', image: imagesStart.step1, link: url.auth.register, linkText: 'step1link' },
		{ title: 'step2title', desc: 'step2desc', image: imagesStart.step2, link: url.auth.login , linkText: 'step2link' },
		{ title: 'step3title', desc: 'step3desc', image: imagesStart.step3, link: url.transcription.new, linkText: 'step3link' },
		{ title: 'step4title', desc: 'step4desc', image: imagesStart.step4 },
		{ title: 'step5title', desc: 'step5desc', image: imagesStart.step5 },
		{ title: 'step6title', desc: 'step6desc'},
		// { title: 'step7title', desc: 'step7desc', image: imagesStart.step7 },
	];
	
	return (
		<div className='static-page__content--instruction'>
			<ol>
				{howToStartSteps.map(item =>
					<li key={ item.title }>
						<h3>	
							{ item.link &&
								<Link to={ item.link }>
									{t('transcriptionHelpPage.howToStart.step.'+item.linkText)}
								</Link>}
							{t('transcriptionHelpPage.howToStart.step.'+item.title)}
						
						</h3>
						{ item.desc && 
						<p>{t('transcriptionHelpPage.howToStart.step.'+item.desc)}</p>}
						{ item.image &&
						<img
							src={ item.image[currentLang] }
							alt={ item.title }
						/>}
					</li>,
				)}
			</ol>
			<ul>
				<li><b>{t('transcriptionHelpPage.howToStart.step.preEndDesc1')}</b> {t('transcriptionHelpPage.howToStart.step.endDesc1')}</li>
				<li><b>{t('transcriptionHelpPage.howToStart.step.preEndDesc2')}</b>{t('transcriptionHelpPage.howToStart.step.endDesc2')}</li>
				<li><b>{t('transcriptionHelpPage.howToStart.step.preEndDesc3')}</b>{t('transcriptionHelpPage.howToStart.step.endDesc3')}</li>
			</ul>
			
			<img src={ imagesStart.step6[currentLang] } alt={t('transcriptionHelpPage.howToStart.step.step6title') } />
		</div>
	);
};

export default HowToStart;
  
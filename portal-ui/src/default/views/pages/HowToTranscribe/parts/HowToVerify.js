import React from "react";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';

import "@Styles/_static-page.sass";

import assignVerifierRole1pl from '@Assets/images/transcriptionHelpPage/howToVerify/assignVerifierRole1pl.png';
import assignVerifierRole1en from '@Assets/images/transcriptionHelpPage/howToVerify/assignVerifierRole1en.png';
import assignVerifierRole1de from '@Assets/images/transcriptionHelpPage/howToVerify/assignVerifierRole1de.png';

import assignVerifierRole2pl from '@Assets/images/transcriptionHelpPage/howToVerify/assignVerifierRole2pl.png';
import assignVerifierRole2en from '@Assets/images/transcriptionHelpPage/howToVerify/assignVerifierRole2en.png';
import assignVerifierRole2de from '@Assets/images/transcriptionHelpPage/howToVerify/assignVerifierRole2de.png';

import startVeryficationSteps1pl from '@Assets/images/transcriptionHelpPage/howToVerify/startVeryficationSteps1pl.png';
import startVeryficationSteps1en from '@Assets/images/transcriptionHelpPage/howToVerify/startVeryficationSteps1en.png';
import startVeryficationSteps1de from '@Assets/images/transcriptionHelpPage/howToVerify/startVeryficationSteps1de.png';

import startVeryficationSteps2pl from '@Assets/images/transcriptionHelpPage/howToVerify/startVeryficationSteps2pl.png';
import startVeryficationSteps2en from '@Assets/images/transcriptionHelpPage/howToVerify/startVeryficationSteps2en.png';
import startVeryficationSteps2de from '@Assets/images/transcriptionHelpPage/howToVerify/startVeryficationSteps2de.png';

const HowToVerify = () => {

	const currentLang = useSelector((state) => state.app.currentLang);
	const { t } = useTranslation();

	const imagesAssignVerifierRole = {
		step1: { pl: assignVerifierRole1pl, en: assignVerifierRole1en, de: assignVerifierRole1de },
		step2: { pl: assignVerifierRole2pl, en: assignVerifierRole2en, de: assignVerifierRole2de },
	};
    
	const assignVerifierRoleSteps = [
		{ title: 'step1', desc: 'step1desc', image: imagesAssignVerifierRole.step1 },
		{ title: 'step2', desc: 'step2desc', image: imagesAssignVerifierRole.step2 },
	];

	const imagesStartVeryficationSteps = {
		step1: { pl: startVeryficationSteps1pl, en: startVeryficationSteps1en, de: startVeryficationSteps1de },
		step2: { pl: startVeryficationSteps2pl, en: startVeryficationSteps2en, de: startVeryficationSteps2de },
	};

	const startVeryficationSteps = [
		{ title: 'step1', desc: 'step1desc', image: imagesStartVeryficationSteps.step1 },
		{ title: 'step2', desc: 'step2desc', image: imagesStartVeryficationSteps.step2 },
	];

	const veryficationIcons = [
		{ icon: 'icon-accept green', text: 'correct'},
		{ icon: 'icon-close red', text: 'incorrect' },
		{ icon: 'empty', text: 'untranscribed' },
	];

	const selectionIcons = [
		{ icon: '<i class="icon-accept-double green" />', text: 'firstClick'},
		{ icon: '<i class="icon-close red" />', text: 'secondClick' },
		{ icon: '<i class="icon-accept green"></i><i class="icon-close red"></i><i class="empty"></i>', text: 'thirdClick' },
	];
	
	return (
		<div className='static-page__content--instruction'>
			<p className='static-page__content--instruction-desc'>
				{t('transcriptionHelpPage.howToVerify.desc')}
			</p>
			<ul className='descList'>
				<li>{t('transcriptionHelpPage.howToVerify.desc_1')}</li>
				<li>{t('transcriptionHelpPage.howToVerify.desc_2')}</li>
				<li>{t('transcriptionHelpPage.howToVerify.desc_3')}</li>
			</ul>
			<section className='static-page__content--section'>
				<h2>{t('transcriptionHelpPage.howToVerify.section.howToAssignRole')}</h2>
				<p>{t('transcriptionHelpPage.howToVerify.section.howToAssignRole_desc')}</p>
				<ol>
					{assignVerifierRoleSteps.map(item =>
						<li key={ item.title }>
							<h3 dangerouslySetInnerHTML={ { __html:t('transcriptionHelpPage.howToVerify.assignRolesSteps.'+item.title) } } />
							<p>{t('transcriptionHelpPage.howToVerify.assignRolesSteps.'+item.desc)}</p>
							<img
								src={ item.image[currentLang] }
								alt={ item.title }
							/>
						</li>,
					)}
				</ol>
			</section>

			<section className='static-page__content--section'>
				<h2>{t('transcriptionHelpPage.howToVerify.section.howToStart')}</h2>
				<p>{t('transcriptionHelpPage.howToVerify.section.howToStart_desc')}</p>
				<ol>
					{startVeryficationSteps.map(item =>
						<li key={ item.title }>
							<h3 dangerouslySetInnerHTML={ { __html:t('transcriptionHelpPage.howToVerify.startVeryficationSteps.'+item.title) } } />
							<p>{t('transcriptionHelpPage.howToVerify.startVeryficationSteps.'+item.desc)}</p>
							<img
								src={ item.image[currentLang] }
								alt={ item.title }
							/>
						</li>,
					)}
				</ol>
			</section>

			<section className='static-page__content--section'>
				<h2>{t('transcriptionHelpPage.howToVerify.section.verification')}</h2>
				<p>{t('transcriptionHelpPage.howToVerify.section.verificationDescription1')}</p>

				<ul className='static-page__content--icons-list'>
					{veryficationIcons.map(item =>
						<li
							className='static-page__content--icons-item'
							key={ item.text }
						>
							<i className={ item.icon } />
							<span className='static-page__content--icons-item--text'>
								{t('transcriptionHelpPage.howToVerify.veryficationIcons.'+item.text)}
							</span>
						</li>,
					)}
				</ul>

				<p>{t('transcriptionHelpPage.howToVerify.section.verificationDescription2')}</p>
				{selectionIcons.map(item =>
					<li
						className='static-page__content--icons-item'
						key={ item.text }
					>
						<span 
							className='static-page__content--icons-item--set'
							dangerouslySetInnerHTML={ { __html: item.icon} }
						/>
						<span
							dangerouslySetInnerHTML={ { __html:t('transcriptionHelpPage.howToVerify.selectionIcons.'+item.text) } }
						/>
					</li>,
				)}
				<p>{t('transcriptionHelpPage.howToVerify.section.verificationDescription3')}</p>
			</section>
            
		</div>
	);
};

export default HowToVerify;
  
import React from "react";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';

import "@Styles/_static-page.sass";

import transcribe1pl from '@Assets/images/transcriptionHelpPage/howToTranscribe/step1pl.png';
import transcribe1en from '@Assets/images/transcriptionHelpPage/howToTranscribe/step1en.png';
import transcribe1de from '@Assets/images/transcriptionHelpPage/howToTranscribe/step1de.png';

import transcribe2pl from '@Assets/images/transcriptionHelpPage/howToTranscribe/step2pl.png';
import transcribe2en from '@Assets/images/transcriptionHelpPage/howToTranscribe/step2en.png';
import transcribe2de from '@Assets/images/transcriptionHelpPage/howToTranscribe/step2de.png';

import transcribe3pl from '@Assets/images/transcriptionHelpPage/howToTranscribe/step3pl.png';
import transcribe3en from '@Assets/images/transcriptionHelpPage/howToTranscribe/step3en.png';
import transcribe3de from '@Assets/images/transcriptionHelpPage/howToTranscribe/step3de.png';

import transcribe4pl from '@Assets/images/transcriptionHelpPage/howToTranscribe/step4pl.png';
import transcribe4en from '@Assets/images/transcriptionHelpPage/howToTranscribe/step4en.png';
import transcribe4de from '@Assets/images/transcriptionHelpPage/howToTranscribe/step4de.png';

import transcribe5pl from '@Assets/images/transcriptionHelpPage/howToTranscribe/step5pl.png';
import transcribe5en from '@Assets/images/transcriptionHelpPage/howToTranscribe/step5en.png';
import transcribe5de from '@Assets/images/transcriptionHelpPage/howToTranscribe/step5de.png';

import transcribe6pl from '@Assets/images/transcriptionHelpPage/howToTranscribe/step6pl.png';
import transcribe6en from '@Assets/images/transcriptionHelpPage/howToTranscribe/step6en.png';
import transcribe6de from '@Assets/images/transcriptionHelpPage/howToTranscribe/step6de.png';

const HowToTranscribe = () => {

	const currentLang = useSelector((state) => state.app.currentLang);
	const { t } = useTranslation();

	const step1extra = () => {
		return (
			<ul className='descList'>
				<li><b>{t('transcriptionHelpPage.howToTranscribe.steps.step1extra_1p')}</b>{t('transcriptionHelpPage.howToTranscribe.steps.step1extra_1')}</li>
				<li><b>{t('transcriptionHelpPage.howToTranscribe.steps.step1extra_2p')}</b>{t('transcriptionHelpPage.howToTranscribe.steps.step1extra_2')}</li>
			</ul>);
	};

	const step3extra = () => {
		return <p><b>{t('transcriptionHelpPage.howToTranscribe.steps.step3extra_1')}</b>{t('transcriptionHelpPage.howToTranscribe.steps.step3extra_2')}</p>;
	};

	const imagesWorkWithFile = {
		step1: { pl: transcribe1pl, en: transcribe1en, de: transcribe1de },
		step2: { pl: transcribe2pl, en: transcribe2en, de: transcribe2de },
		step3: { pl: transcribe3pl, en: transcribe3en, de: transcribe3de },
		step4: { pl: transcribe4pl, en: transcribe4en, de: transcribe4de },
		step5: { pl: transcribe5pl, en: transcribe5en, de: transcribe5de },
		step6: { pl: transcribe6pl, en: transcribe6en, de: transcribe6de },
	};

	const workWithFile = [
		{ title: 'step1title', desc: 'step1desc', image: imagesWorkWithFile.step1, extra: step1extra },
		{ title: 'step2title', desc: 'step2desc', image: imagesWorkWithFile.step2 },
		{ title: 'step3title', desc: 'step3desc', image: imagesWorkWithFile.step3, extra: step3extra },
		{ title: 'step4title', desc: 'step4desc', image: imagesWorkWithFile.step4 },
		{ title: 'step5title', desc: 'step5desc', image: imagesWorkWithFile.step5 },
		{ title: 'step6title', desc: 'step6desc', image: imagesWorkWithFile.step6 },
	];
    
	const workOnWindowWithObject = [
		{ icon: 'icon-add-area', text: 'addTranscriptionArea', textInfo: 'addTranscriptionAreaInfo' },
		{ icon: 'icon-zoom-in', text: 'zoomIn' },
		{ icon: 'icon-zoom-out', text: 'zoomOut' },
		{ icon: 'icon-full-screen', text: 'fullScreen'},
		{ icon: 'icon-fit-to-window', text: 'fitToWindow'},
	];

	const workWithTranscriptionWindow = [
		{ icon: 'icon-italic', text: 'italic' },
		{ icon: 'icon-bold', text: 'bold' },
		{ icon: 'icon-symbol', text: 'symbol' },
		{ icon: 'icon-quote', text: 'quote'},
		{
			icon: 'icon-list-end-1',
			text: 'listEnd',
			subIcons: [
				{ icon: 'icon-list-item-end', text: 'listElementEnd' },
				{ icon: 'icon-list-end', text: 'listEnd' },
			],
		},
		{
			icon: 'icon-verses-desc',
			text: 'page',
			subIcons: [
				{ icon: 'icon-verse-header', text: 'pageHeader' },
				{ icon: 'icon-verse-footer', text: 'pageFooter'},
				{ icon: 'icon-page-number', text: 'pageNumber'},
			],
		},
		{
			icon: 'icon-header',
			text: 'header',
			subIcons: [
				{ icon: 'icon-header', text: 'header1'},
				{ icon: 'icon-header2', text: 'header2'},
				{ icon: 'icon-header3', text: 'header3'},
			],
		},
		{
			icon: 'icon-image-description',
			text: 'image',
			subIcons: [
				{ icon: 'icon-image', text: 'image' },
				{ icon: 'icon-image-description', text: 'imageDescription' },
			],
		},
		{
			icon: 'icon-table',
			text: 'table',
			subIcons: [
				{ icon: 'icon-table-cell-end', text: 'tableCellEnd' },
				{ icon: 'icon-table-verse-end', text: 'tableVerseEnd' },
				{ icon: 'icon-table-end', text: 'tableEnd' },
			],
		},
		{ icon: 'icon-verse-auto-transcription', text: 'verseAutoTranscription'},
		{ icon: 'icon-verse-comment', text: 'verseComment'},
		{ icon: 'icon-verse-delete', text: 'verseDelete' },
		{ icon: 'icon-verse-clear', text: 'verseClear'},
		{ icon: 'icon-verse-up', text: 'verseUp' },
		{ icon: 'icon-verse-down', text: 'verseDown' },
	];

	return (
		<div className='static-page__content--instruction'>
			<p className='static-page__content--instruction-desc'>
				{t('transcriptionHelpPage.howToTranscribe.desc')}
			</p>
			<ul className='descList'>
				<li>{t('transcriptionHelpPage.howToTranscribe.descPoint1')}</li>
				<li>{t('transcriptionHelpPage.howToTranscribe.descPoint2')}</li>
				<li>{t('transcriptionHelpPage.howToTranscribe.descPoint3')}</li>
			</ul>
			<section className='static-page__content--section'>
				<h2>{t('transcriptionHelpPage.howToTranscribe.section.workWithFile')}</h2>
				<ol>
					{workWithFile.map(item =>
						<li key={ item.title }>
							<h3 dangerouslySetInnerHTML={ { __html:t('transcriptionHelpPage.howToTranscribe.steps.'+item.title) } } />
							{item.desc &&
							<p>{t('transcriptionHelpPage.howToTranscribe.steps.'+item.desc)}</p>}
							{ item.extra && item.extra()}
							<img
								src={ item.image[currentLang] }
								alt={ item.title }
							/>
						</li>,
					)}
				</ol>
			</section>

			<section className='static-page__content--section'>
				<h2>{t('transcriptionHelpPage.howToTranscribe.section.workOnWindow')}</h2>
				<p>{t('transcriptionHelpPage.howToTranscribe.section.workOnWindowDescription')}</p>
				<ul className='static-page__content--icons-list'>
					{workOnWindowWithObject.map(item =>
						<li
							className='static-page__content--icons-item'
							key={ item.text }
						>
							<i className={ item.icon } />
							<span className='static-page__content--icons-item--text'>
								{t('transcriptionHelpPage.howToTranscribe.icons.'+item.text)}
								{item.textInfo &&
								<span className='static-page__content--icons-item--subtext'>
									{t('transcriptionHelpPage.howToTranscribe.icons.'+item.textInfo)}
								</span>}
							</span>
						</li>,
					)}
				</ul>
			</section>

			<section className='static-page__content--section'>
				<h2>{t('transcriptionHelpPage.howToTranscribe.section.workWithWindow')}</h2>
				<p>{t('transcriptionHelpPage.howToTranscribe.section.workWithWindowDescription')}</p>
				<ul className='static-page__content--icons-list'>
					{workWithTranscriptionWindow.map(item =>
						<li
							className='static-page__content--icons-item'
							key={ item.text }
						>
							<i className={ item.icon } />
							<span className='static-page__content--icons-item--text'>
								{t('transcriptionHelpPage.howToTranscribe.icons.workWithTranscriptionWindow.'+item.text)}
								{item.subIcons &&
								<span className='static-page__content--icons-item--subtext'>
									{t('transcriptionHelpPage.howToTranscribe.icons.workWithTranscriptionWindow.'+(item.textInfo || item.text ))}
								</span>}
								<ul className='static-page__content--icons-item--sublist'>
									{item.subIcons && item.subIcons.map(subIcon =>
										<li key={ subIcon.text }>
											<i className={ subIcon.icon } />
											{t('transcriptionHelpPage.howToTranscribe.icons.workWithTranscriptionWindow.'+subIcon.text)}
										</li>,
									)}
								</ul>
							</span>
						</li>,
					)}
				</ul>
			</section>
		</div>
	);
};

export default HowToTranscribe;
  
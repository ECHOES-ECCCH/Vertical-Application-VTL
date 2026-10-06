import React, { memo } from "react";
import PropTypes from "prop-types";
import { useTranslation } from 'react-i18next';

const VerseExpand = ({ hasActiveWord, activateFirstWord, setActiveWord }) => {
	const { t } = useTranslation();

	return (
		!(hasActiveWord) ? 
			<div className='verse__expand-words' onClick={() => activateFirstWord()}>{t('viewer.words.expand')}<i className='icon-arrow-down-full'/></div> 
			: 
			<div className='verse__expand-words' onClick={() => { if(hasActiveWord) { setActiveWord(null); } }}>{t('viewer.words.collapse')}<i className='icon-arrow-up-full'/></div>
	);
};

VerseExpand.propTypes = {
	hasActiveWord: PropTypes.bool,
	activateFirstWord: PropTypes.func,
	setActiveWord: PropTypes.func,
};

export default memo(VerseExpand);
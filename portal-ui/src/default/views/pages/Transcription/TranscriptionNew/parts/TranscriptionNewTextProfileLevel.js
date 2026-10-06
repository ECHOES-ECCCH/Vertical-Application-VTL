import React from "react";
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";
import { useFormContext } from "react-hook-form";

import FormInputCustom from "@Elements/Form/FormInputCustom";

import FormTextarea from "@Elements/Form/FormTextarea";

const TranscriptionNewTextProfileLevel = ({ isDisabled }) => {
	const { register } = useFormContext();
	const { t } = useTranslation();

	return (
		<div className='transcription-new__level'>
			<h3 className='text--level-title'>
				{t('transcription.details.textLevel') }
			</h3>
			<span className='text--level-desc'>
				{t('transcription.details.textLevelDescription') }
			</span>
			<div className='transcription-new__form--row'>
				<FormInputCustom
					register={ register }
					label={t('transcription.form.keywords') }
					name='profileDesc.keywords'
					placeholder={t('transcription.form.keywordsPlaceholder') }
					inputContainerClassName='transcription-new__form--inline-medium'
					isDisabled={ isDisabled }
				/>
			</div>

			<div className='transcription-new__form--row'>
				<FormTextarea
					name='profileDesc.summary'
					register={ register }
					label={t('transcription.form.summary') }
					isDisabled={ isDisabled }
				/>
			</div>
		</div>
	);
};

TranscriptionNewTextProfileLevel.propTypes = {
	isDisabled: PropTypes.bool, 
};

export default TranscriptionNewTextProfileLevel;
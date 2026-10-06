import React from "react";
import { useTranslation } from 'react-i18next';
import { useFormContext } from "react-hook-form";
import PropTypes from "prop-types";

import FormInput from "@Elements/Form/FormInput";
import CommonDropdownForm from '@Elements/Dropdown/CommonDropdownForm';

import AuthorNameRoleArray from '@Pages/Transcription/TranscriptionNew/elements/AuthorNameRoleArray';

const TranscriptionNewAnalyticalLevel = ({ languageOptions, roleOptions, isDisabled }) => {
	const {
		register,
		watch,
		formState: {
			errors,
		},
    } = useFormContext();  // eslint-disable-line
	const { t } = useTranslation();
	const secondFieldValue = watch('monographicInfo.title');
	
	return (
		<div className='transcription-new__level'>
			<h3 className='text--level-title'>
				{t('transcription.details.analyticLevel')}
			</h3>
			<span className='text--level-desc'>
				{t('transcription.details.analyticLevelDescription')}
			</span>

			<div className='transcription-new__form--row'>
				<FormInput
					name='analyticInfo.title'
					register={ register }
					label={ t('transcription.form.title') }
					placeholder={ t('transcription.form.titlePlaceholder') }
					validateData={
						{
							validate: value => value !== '' || secondFieldValue !== '' || t('form.messages.minumumOnetitle'),
						}
					}
					isDisabled={ isDisabled }
					isRequired
					errorMessage={ errors.analyticInfo && errors.analyticInfo.title && errors.analyticInfo.title.message ? errors.analyticInfo.title.message : '' }
				/>
			</div>

			<AuthorNameRoleArray
				linkedObject='analyticInfo.actors'
				isDisabled={ isDisabled }
				roleOptions={ roleOptions }
			/>

			<div className='transcription-new__form--inline-row'>
				<div className='transcription-new__form--inline-select'>
					<CommonDropdownForm
						label={ t('transcription.form.language') }
						placeholder={ t('transcription.form.languageSelect') }
						fieldName='analyticInfo.language'
						options={ languageOptions }
						isDisabled={ isDisabled }
					/>
				</div>
			</div>
		</div>
	);
};

TranscriptionNewAnalyticalLevel.propTypes = {
	languageOptions: PropTypes.array,
	roleOptions: PropTypes.array,
	isDisabled: PropTypes.bool,
};

export default TranscriptionNewAnalyticalLevel;
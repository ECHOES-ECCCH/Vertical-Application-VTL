import React from "react";
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";
import { useFormContext } from "react-hook-form";

import FormInputCustom from "@Elements/Form/FormInputCustom";

const TranscriptionNewIndentifierLevel = ({ isDisabled }) => {
	const { register } = useFormContext();
	const { t } = useTranslation();

	return (
		<div className='transcription-new__level'>
			<h3 className='text--level-title'>
				{t('transcription.details.identifierLevel')}
			</h3>
			<div className='transcription-new__form--row'>
				<label className='transcription-new__form--label'>
					{t('transcription.details.importAdress')}
					<span
						className='transcription__tooltip'
						data-tooltip-id='custom-tooltip' data-tooltip-content={t('transcription.details.importAdressDesc') }
						data-place='right'
					>
						<i className='icon-question'  />
					</span>
				</label>
				<FormInputCustom
					register={ register }
					name='externalUrl'
					inputContainerClassName='transcription-new__form--inline-medium'
					isDisabled={ isDisabled }
				/>
			</div>
			<div className='transcription-new__form--row'>
				<label className='transcription-new__form--label'>
					{t('transcription.details.externalId')}
					<span
						className='transcription__tooltip'
						data-tooltip-id='custom-tooltip' data-tooltip-content={t('transcription.details.externalIdDesc') }
						data-place='right'
					>
						<i className='icon-question'  />
					</span>
				</label>
				<FormInputCustom
					register={ register }
					name='externalId'
					inputContainerClassName='transcription-new__form--inline-medium'
					isDisabled={ isDisabled }
				/>
			</div>

		</div>
	);
};

TranscriptionNewIndentifierLevel.propTypes = {
	isDisabled: PropTypes.bool,
};

export default TranscriptionNewIndentifierLevel;
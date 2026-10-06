import React, { useState, useEffect, memo } from 'react';
import { useTranslation } from 'react-i18next';
import { useFormContext } from "react-hook-form";
import PropTypes from "prop-types";

import CommonDropdown from '@Elements/Dropdown/CommonDropdown';

const RoleDropdown = ({ roleOptions, fieldName, isDisabled }) => {
	const { t } = useTranslation();

	const { register, setValue, getValues } = useFormContext();

	const [selectedOption, setSelectedOption] = useState(roleOptions.findIndex(it => it.value === getValues(fieldName)));

	useEffect(() => {
		if (selectedOption > -1) {
			setValue(fieldName, roleOptions[selectedOption].value);
		} else {
			setSelectedOption(0);
		}
	}, [selectedOption]);

	return (
		<div className='transcription-new__form--inline-select'>
			<CommonDropdown
				options={roleOptions.map(item => item.label)}
				selectedOption={selectedOption}
				setSelectedOption={setSelectedOption}
				label={t('transcription.form.role')}
				isDisabled={isDisabled}
			/>
			<label className='d-inline'>
				<span className='d-none'>{t('transcription.form.role')}</span>

				<input {...register(fieldName)} style={{ display: 'none' }} />
			</label>
		</div>
	);
};

RoleDropdown.propTypes = {
	fieldName: PropTypes.string,
	isDisabled: PropTypes.bool,
	roleOptions: PropTypes.array,
};

export default memo(RoleDropdown);
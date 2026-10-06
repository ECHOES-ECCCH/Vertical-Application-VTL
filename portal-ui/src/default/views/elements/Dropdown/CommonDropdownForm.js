import React, { useState, useEffect } from "react";
import PropTypes from "prop-types";
import { useFormContext, useWatch } from "react-hook-form";

import CommonDropdown from '@Elements/Dropdown/CommonDropdown';

const CommonDropdownForm = ({ options, fieldName, placeholder = '', isDisabled, label }) => {
	const { setValue, getValues } = useFormContext();
	const [selectedOption, setSelectedOption] = useState(-1);

	useEffect(() => {
		setSelectedOption( options.findIndex(element => element.value === getValues(fieldName)) ?? -1 );
	}, []);

	useEffect(() => {
		if (selectedOption > -1) {
			if (getValues(fieldName) && getValues(fieldName).value && (selectedOption === options.findIndex(element => element.value === getValues(fieldName).value))) {
				return;
			}
			setValue(fieldName, options[selectedOption].value);
		}
	}, [selectedOption]);

	return (
		<>
			<CommonDropdown
				options={options.map(item => item.label)}
				selectedOption={selectedOption}
				setSelectedOption={setSelectedOption}
				label={label}
				placeholder={placeholder}
				isDisabled={isDisabled}
			/>
			<label>
				<span className='d-none'>{label}</span>
			</label>
		</>
	);
};

CommonDropdownForm.propTypes = {
	options: PropTypes.array,
	fieldName: PropTypes.string,
	placeholder: PropTypes.string,
	isDisabled: PropTypes.bool,
	label: PropTypes.string,
};

export default React.memo(CommonDropdownForm);
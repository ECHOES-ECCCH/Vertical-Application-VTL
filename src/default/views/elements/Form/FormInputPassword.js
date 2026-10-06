import React from 'react';
import PropTypes from "prop-types";
import { useTranslation } from 'react-i18next';

import FormInput from "@Elements/Form/FormInput";

const FormInputPassword = ({ register, errors, name = 'password', skipPatternValidation = false }) => {
	const { t } = useTranslation();
	let pattern = (skipPatternValidation) ? (((/^.*$/))) : (((/^(?=.{8,})(?=.*[a-z])(?=.*[A-Z])((?=.*[!"#$%&'()*\+,\-\./:;<=>?@\[\]\\\^_`{\|}~])|(?=.*[0-9])).*$/))); // eslint-disable-line
	return (
		<FormInput
			name={ name }
			type='password'
			label={ name !== 'password' ?t('form.'+name) :t('form.password') }
			register={ register }
			validateData={ {
				required: {
					value: true,
					message:t('form.messages.fillPassword'),
				},
				pattern: {
					value: pattern,
					message:t('form.messages.incorrectPassword'),
				},
			} }
			errorMessage={ errors[name] && errors[name].message }
		/> );
};

FormInputPassword.propTypes = {
	register: PropTypes.func,
	errors: PropTypes.object,
	name: PropTypes.string,
	skipPatternValidation: PropTypes.bool,
};

export default FormInputPassword;

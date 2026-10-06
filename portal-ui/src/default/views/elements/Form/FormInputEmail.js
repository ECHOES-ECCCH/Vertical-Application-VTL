import React from 'react';
import PropTypes from "prop-types";
import { useTranslation } from 'react-i18next';

import FormInput from "@Elements/Form/FormInput";

const FormInputEmail = ({ register, errors, name = 'email', novalidate }) => {
	const { t } = useTranslation();
	return novalidate ? (
		<FormInput
			register={register}
			name={name}
			label={t('form.email')}
			type='text'
			errorMessage={errors && errors.email && errors.email.message}
		/>
	) : (
		<FormInput
			register={register}
			name={name}
			label={t('form.email')}
			type='text'
			validateData={{
				required: {
					value: true,
					message: t('form.messages.emptyEmail'),
				},
				pattern: {
					value: /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/, // eslint-disable-line
					message: t('form.messages.invalidEmail'),
				},
			}}
			errorMessage={errors && errors.email && errors.email.message}
		/>
	);
};

FormInputEmail.propTypes = {
	register: PropTypes.func,
	errors: PropTypes.object,
	name: PropTypes.string,
	novalidate: PropTypes.bool,
};

export default FormInputEmail;
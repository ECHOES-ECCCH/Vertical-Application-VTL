import React from 'react';
import PropTypes from "prop-types";
import { useTranslation } from 'react-i18next';

import FormInput from "@Elements/Form/FormInput";

const FormInputPasswordRepeated = ({ register, errors, name = 'repeatedPassword', getValues }) => {
	const { t } = useTranslation();
	return (
		<FormInput
			name={name}
			type='password'
			label={name !== 'repeatedPassword' ? t('form.'+name) : t('form.repeatedPassword')}
			register={register}
			validateData={
				{
					required: {
						value: true,
						message: t('form.messages.fillPassword'),
					},
					validate: {
						matchesPreviousPassword: (value) => {
							if (name === 'repeatedNewPassword') {
								const { newPassword } = getValues;
								return newPassword === value || t('form.messages.differentPasswords');
							} else {
								const { password } = getValues;
								return password === value || t('form.messages.differentPasswords');
							}
						},
					},
				}
			}
			errorMessage={errors[name] && errors[name].message}
		/>
	);
};
FormInputPasswordRepeated.propTypes = {
	register: PropTypes.func,
	errors: PropTypes.object,
	name: PropTypes.string,
	getValues: PropTypes.object,
};

export default FormInputPasswordRepeated;

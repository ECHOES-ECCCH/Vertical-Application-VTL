import React from 'react';
import PropTypes from "prop-types";
import Form from "react-bootstrap/Form";
import { useTranslation } from 'react-i18next';

import styles from '@Elements/Form/Form.module.sass';

const FormInputRange = ({ register, name='pages', disabled, errorMessage, label, placeholder }) => {
	const { t } = useTranslation();
	return (
		<>
			<Form.Control
				/* ref={register(
					{
						pattern: {
							value: /^[0-9,-]*$/, // eslint-disable-line
							message: t('form.messages.invalidRange'),
						},
					},
				)} */
				className={errorMessage ? styles["form__input--incorrect"] : null}
				type='text'
				autoComplete='off'
				name={name}
				disabled={disabled}
				placeholder={placeholder}
				{...register(name, {
					pattern: {
						value: /^[0-9,-]*$/, // eslint-disable-line
						message: t('form.messages.invalidRange'),
					},
				})}
			/>
			{errorMessage ?
				(
					<div className={styles["form__message--invalid"]}><i className='icon-close' />{errorMessage}</div>
				) : (
					<Form.Label >{label}</Form.Label>
				)}

		</>
	);
};

FormInputRange.propTypes = {
	register: PropTypes.func,
	name: PropTypes.string,
	errorMessage: PropTypes.string,
	label: PropTypes.string,
	disabled: PropTypes.bool,
	placeholder: PropTypes.string,
};

export default FormInputRange;
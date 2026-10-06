import React, { useState } from "react";
import PropTypes from 'prop-types';
import Form from "react-bootstrap/Form";

import styles from '@Elements/Form/Form.module.sass';

const FormInput = ({ name, label, sublabel, type, placeholder, register, validateData, errorMessage, correctMessage, isDisabled, isRequired }) => {
	const [hidePassword, setPasswordVisibility] = useState(true);
	const togglePasswordHandler = () => {
		setPasswordVisibility(!hidePassword);
	};

	let icon = (type === 'password' ?
		<i
			className={`${(hidePassword) ? 'icon-eye-disabled' : 'icon-eye'} ${styles["form__input-password"]}`}
			onClick={togglePasswordHandler}
		/> : null);

	return (
		<div className={styles["form__input-container"]}>
			{type === 'RichTextHidden' || label == null ||
				<div className={styles["form__input-row"]}>
					<Form.Label>{label}</Form.Label>
					{isRequired && <span className='asterisk'>*</span>}
					{sublabel && <p className={styles["form-label"]}>{sublabel}</p>}
				</div>}

			<Form.Row className={styles["form__input-row"]}>
				<input
					className={`form-control ${styles["form__input"]}
						${errorMessage ? styles["form__input--incorrect"] : null}
						${correctMessage ? styles["form__input--correct"] : null}
					` }
					type={(type === 'password') ? (hidePassword ? 'password' : 'text') : (type === "RichTextHidden" ? 'hidden' : type)}
					name={name}
					id={name}
					autoComplete='off'
					placeholder={placeholder}
					// ref={register(validateData)}
					disabled={isDisabled}
					{...register(name, validateData)}
				/>
				{icon}
				{errorMessage ? <div className={styles["form__message--invalid"]}><i className='icon-close' />{errorMessage}</div> : null}
			</Form.Row>

		</div>
	);
};
FormInput.propTypes = {
	name: PropTypes.string,
	label: PropTypes.string,
	sublabel: PropTypes.string,
	type: PropTypes.string,
	placeholder: PropTypes.string,
	register: PropTypes.func,
	validateData: PropTypes.object,
	errorMessage: PropTypes.string,
	correctMessage: PropTypes.string,
	isDisabled: PropTypes.bool,
	isRequired: PropTypes.bool,
};

export default FormInput;

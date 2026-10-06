import React from "react";
import PropTypes from 'prop-types';

import Form from "react-bootstrap/Form";

import styles from '@Elements/Form/Form.module.sass';

const FormCheckbox = ({ children, name, label, register, validateData, errorMessage }) => {
	return (
		<>
			<Form.Control
				name={name}
				id={name}
				className={styles["form__checkbox"]}
				type='checkbox'
				isInvalid={false}
				{...register(name, validateData)}
			/>
			<Form.Label >
				{label ? (
					<>{label}</>
				) : (
					<span className='d-none'>checkbox</span>
				)}
			</Form.Label>
			{children}
			{errorMessage ? <div className={styles["form__message--invalid"]}><i className='icon-close' />{errorMessage}</div> : null}
		</>
	);
};

FormCheckbox.propTypes = {
	children: PropTypes.any,
	name: PropTypes.string,
	label: PropTypes.string,
	register: PropTypes.func,
	validateData: PropTypes.object,
	errorMessage: PropTypes.string,
};

export default FormCheckbox;

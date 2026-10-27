import React from "react";
import PropTypes from 'prop-types';
import Form from "react-bootstrap/Form";
import styles from '@Elements/Form/Form.module.sass';

const FormRadioGroup = ({ label, name, options, register, className='', labelClassName, errors, errorMessage, disabled }) => {
	return (
		<Form.Group className={className}>
			<Form.Label className={labelClassName}>{label}</Form.Label>
			{options.map((option) =>
				<Form.Check
					type='radio'
					key={option.label}
					label={option.label}
					id={option.label}
					value={option.value}
					disabled={disabled}
					{...register(name)}
				/>,
			)}
			{errorMessage ? <div className={styles["form__message--invalid"]}><i className='icon-close' />{errorMessage}</div> : null}
			{(errors && errors[name] && errors[name].message) ? <div className={styles["form__message--invalid"]}><i className='icon-close' />{errors[name].message}</div> : null}
		</Form.Group>
	);
};

FormRadioGroup.propTypes = {
	label: PropTypes.string,
	name: PropTypes.string,
	options: PropTypes.arrayOf(
		PropTypes.shape({
			label: PropTypes.string,
			value: PropTypes.string,
		}),
	).isRequired,
	register: PropTypes.func.isRequired,
	className: PropTypes.string,
	labelClassName: PropTypes.string,
	errors: PropTypes.object,
	errorMessage: PropTypes.string,
	disabled: PropTypes.bool,
};

export default FormRadioGroup;

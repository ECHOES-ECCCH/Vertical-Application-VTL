import React from "react";
import PropTypes from "prop-types";
import Form from "react-bootstrap/Form";

import styles from "@Elements/Form/Form.module.sass";

const FormTextarea = ({
	name,
	label,
	placeholder,
	register,
	validateData,
	errorMessage,
	correctMessage,
	isDisabled,
	maxLength,
}) => {
	return (
		<>
			<Form.Label htmlFor={name}>{label}</Form.Label>
			<Form.Row className={styles["form__textarea-row"]}>
				<textarea
					className={`form-control 
								${errorMessage ? styles["form__input--incorrect"] : null}
								${correctMessage ? styles["form__input--correct"] : null}
							`}
					type='text'
					name={name}
					id={name}
					autoComplete='off'
					placeholder={placeholder}
					disabled={isDisabled}
					// ref={register(validateData)}
					maxLength={maxLength}
					{...register(name, validateData)}
				/>

				{errorMessage ? (
					<div className={styles["form__message--invalid"]}>
						<i className='icon-close' />
						{errorMessage}
					</div>
				) : null}
			</Form.Row>
		</>
	);
};
FormTextarea.propTypes = {
	name: PropTypes.string,
	label: PropTypes.string,
	placeholder: PropTypes.string,
	register: PropTypes.func,
	validateData: PropTypes.object,
	errorMessage: PropTypes.string,
	correctMessage: PropTypes.string,
	isDisabled: PropTypes.bool,
	maxLength: PropTypes.number,
};

export default FormTextarea;

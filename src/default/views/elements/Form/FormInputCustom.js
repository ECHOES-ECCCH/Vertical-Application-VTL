import React, { memo } from "react";
import { useTranslation } from 'react-i18next';
import PropTypes from 'prop-types';
import Form from "react-bootstrap/Form";

import styles from '@Elements/Form/Form.module.sass';
import Button from "@Elements/Button/Button";

const FormInputWithDelete = ({ name, label, type, placeholder, register, validateData, errorMessage, correctMessage, inputContainerClassName, onDeleteFunction, onChange, onBlur, onFocus, isDisabled }) => {
	const { t } = useTranslation();

	return (
		<div className={`
			${styles["form__input-container"]}
			${inputContainerClassName ? inputContainerClassName : null}
			` }
		>
			{(onDeleteFunction) ?
				(
					<div className={styles["form__input--delete-container"]}>
						<Form.Label>{label}</Form.Label>
						{(!isDisabled) && (
							<Button
								variant='link'
								onClick={onDeleteFunction}
								className={styles["form__input--delete"]}
							>
								{t('form.delete')}
							</Button>
						)}
					</div>
				)
				:
				(<Form.Label >{label}</Form.Label>)}
			<Form.Row className={styles["form__input-row"]}>
				<input
					className={`form-control 
						${errorMessage ? styles["form__input--incorrect"] : null}
						${correctMessage ? styles["form__input--correct"] : null}
					` }
					type={type ? type : 'text'}
					name={name}
					id={name}
					autoComplete='off'
					placeholder={placeholder}
					onChange={onChange}
					disabled={isDisabled}
					onBlurCapture={onBlur}
					onFocus={onFocus}
					{...register(name, validateData)}
				/>
				{errorMessage ? (
					<div className={styles["form__message--invalid"]}>
						<i className='icon-close' />{errorMessage}
					</div>)
					: null}
			</Form.Row>
		</div>
	);
};
FormInputWithDelete.propTypes = {
	name: PropTypes.string,
	label: PropTypes.string,
	type: PropTypes.string,
	placeholder: PropTypes.string,
	register: PropTypes.func,
	validateData: PropTypes.object,
	errorMessage: PropTypes.string,
	correctMessage: PropTypes.string,
	inputContainerClassName: PropTypes.string,
	onDeleteFunction: PropTypes.func,
	onChange: PropTypes.func,
	onBlur: PropTypes.func,
	onFocus: PropTypes.func,
	isDisabled: PropTypes.bool,
};

export default memo(FormInputWithDelete);

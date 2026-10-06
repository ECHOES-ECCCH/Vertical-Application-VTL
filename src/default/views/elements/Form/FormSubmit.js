import React from "react";
import PropTypes from 'prop-types';

import Button from "@Elements/Button/Button";

const FormSubmit = ({ label, className, variant = 'primary', disabled }) => {
	return (
		<Button
			variant={ variant }
			type='submit'
			className={ className }
			disabled={ disabled }
		>
			{label}
		</Button>
	);
};

FormSubmit.propTypes = {
	label: PropTypes.string,
	className: PropTypes.string,
	variant: PropTypes.string,
	disabled: PropTypes.bool,
};

export default FormSubmit;

import React, { memo } from "react";
import PropTypes from "prop-types";
import ButtonBootstrap from "react-bootstrap/Button";

import '@Elements/Button/Button.sass';
import '@Elements/Button/ButtonViewer.sass';

const Button = React.forwardRef(({
	id,
	disabled = false,
	active = false,
	variant = "primary",
	size,
	children,
	href,
	target,
	className,
	onClick,
	onBlur,
	onMouseDown,
	type = "button",
}, ref) => (
	<ButtonBootstrap
		variant={ variant }
		id={ id }
		type={ type }
		disabled={ disabled }
		size={ size }
		active={ active }
		href={ href }
		target={ target }
		className={ className }
		onClick={ onClick }
		onBlur={ onBlur }
		ref={ ref }
		onMouseDown={ onMouseDown }
	>
		{children}
	</ButtonBootstrap>
));
Button.displayName = "Button";
Button.propTypes = {
	id: PropTypes.string,
	disabled: PropTypes.oneOfType([
		PropTypes.bool,
		PropTypes.number,
	]),
	active: PropTypes.bool,
	variant: PropTypes.string,
	type: PropTypes.string,
	size: PropTypes.string,
	href: PropTypes.string,
	target: PropTypes.string,
	className: PropTypes.string,
	onClick: PropTypes.func,
	onBlur: PropTypes.func,
	onMouseDown: PropTypes.func,
	children: PropTypes.any,
};

export default memo(Button);
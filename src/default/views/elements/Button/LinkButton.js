import React, { memo } from "react";
import PropTypes from "prop-types";
import ButtonBootstrap from "react-bootstrap/Button";
import { Link } from "react-router-dom";

import '@Elements/Button/Button.sass';

/** @description Zwróci Button opakowany w Link,
 * można użyć i to i href - dla kompatybilności z komponentem Button
 */
const LinkButton = React.forwardRef(({
	id,
	disabled = false,
	active = false,
	variant = "primary",
	size,
	children,
	to,
	href,
	target,
	className = '',
	onClick,
	onBlur,
	onMouseDown,
	type = "button",
}, ref) => (
	<ButtonBootstrap
		as={Link}
		to={disabled? "#" : (to || href)}
		variant={ variant }
		id={ id }
		type={ type }
		disabled={ disabled }
		size={ size }
		active={ active }
		target={ target }
		className={ className + ( disabled ? " disabled" : "")}
		onClick={ onClick }
		onBlur={ onBlur }
		ref={ ref }
		onMouseDown={ onMouseDown }
	>
		{children}
	</ButtonBootstrap>
));

LinkButton.propTypes = {
	id: PropTypes.string,
	disabled: PropTypes.oneOfType([
		PropTypes.bool,
		PropTypes.number,
	]),
	active: PropTypes.bool,
	variant: PropTypes.string,
	type: PropTypes.string,
	size: PropTypes.string,
	to: PropTypes.string,
	href: PropTypes.string,
	target: PropTypes.string,
	className: PropTypes.string,
	onClick: PropTypes.func,
	onBlur: PropTypes.func,
	onMouseDown: PropTypes.func,
	children: PropTypes.any,
};

LinkButton.displayName = 'LinkButton';

export default memo(LinkButton);
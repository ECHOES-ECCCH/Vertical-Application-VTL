import React, { memo } from 'react';
import { Dropdown } from 'react-bootstrap';
import PropTypes from "prop-types";

const DropdownBox = ({ tooltip, disabled, icon, children, visible = true }) => {
	return (
		visible &&
		<Dropdown
			drop='right'
		>
			<div
				data-tooltip-id='custom-tooltip' data-tooltip-content={ tooltip }
				data-place='right'
			>
				<Dropdown.Toggle
					variant='viewer-button'
					className='viewer__button--menu'
					disabled={ disabled }
					id='button'
				>
					<span className='d-none'>{icon}</span>
					<i className={ `icon-${icon}` } />
				</Dropdown.Toggle>
			</div>

			<Dropdown.Menu>
				{children}
			</Dropdown.Menu>
		</Dropdown>
	);
};

DropdownBox.propTypes = {
	tooltip: PropTypes.string,
	disabled: PropTypes.oneOfType([
		PropTypes.bool,
		PropTypes.number,
	]),
	icon: PropTypes.string,
	children: PropTypes.any,
	visible: PropTypes.bool,
};

export default memo(DropdownBox);
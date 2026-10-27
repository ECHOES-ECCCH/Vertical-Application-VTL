import React from "react";
import { Dropdown } from "react-bootstrap";
import PropTypes from "prop-types";

const CommonDropdown = ({
	options,
	selectedOption = 0,
	setSelectedOption,
	placeholder = '',
	isDisabled,
	label,
	name,
	className,
	isRequired,
}) => {

	return (
		<>
			{(label && label.length > 1) && (
				<>
					<label htmlFor={name}>{label}</label>
					{isRequired && <span className='asterisk'>*</span>}
				</>
			)}
			<Dropdown
				bsPrefix={`dropdown-common ${selectedOption < 0 && placeholder && 'dropdown-common__placeholder'} ${className}`}
				drop='down'
			>
				<Dropdown.Toggle
					variant='dropdown'
					disabled={isDisabled}
					id='common'
				>
					{selectedOption < 0 ? placeholder : options[selectedOption]}
				</Dropdown.Toggle>
				<Dropdown.Menu>
					{options.map((item, index) => (
						<Dropdown.Item
							onClick={() => {
								setSelectedOption(index);
							}}
							className={`${index === selectedOption ? "dropdown-item__active" : ""}`}
							key={`${item}-${index + 1}`}
						>
							{(index === selectedOption) && <i className='icon-accept' />}
							{item}
						</Dropdown.Item>
					))}

				</Dropdown.Menu>
			</Dropdown>
		</>
	);
};

CommonDropdown.propTypes = {
	options: PropTypes.array,
	selectedOption: PropTypes.number,
	setSelectedOption: PropTypes.func,
	placeholder: PropTypes.string,
	name: PropTypes.string,
	isDisabled: PropTypes.bool,
	label: PropTypes.string,
	className: PropTypes.string,
	isRequired: PropTypes.bool,
};

export default CommonDropdown;
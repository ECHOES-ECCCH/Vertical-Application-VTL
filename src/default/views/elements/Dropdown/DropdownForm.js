import React from "react";
import { Controller } from 'react-hook-form';
import { Dropdown } from "react-bootstrap";
import PropTypes from "prop-types";

const DropdownForm = ({
	name,
	options,
	control,
	label,
	isRequired = false,
	className,
	...props
}) => {
	return (
		<Controller
			name={name}
			control={control}
			render={({ field }) => (
				<>
					{(label && label.length > 1) && (
						<>
							<label htmlFor={name}>{label}</label>
							{isRequired && <span className='asterisk'>*</span>}
						</>
					)}
					<Dropdown
						className={className}
						drop='down'
						{...props}
					>
						<Dropdown.Toggle variant='dropdown' id={name} {...field}>
							{field?.value?.label === undefined ? field.value : field.value.label}
						</Dropdown.Toggle>
						<Dropdown.Menu>
							{options.map((item, index) => {
								const isActive = item.value === undefined ? item === field.value : item.value === field.value.value;
								return (
									<Dropdown.Item
										key={`${item}-${index + 1}`}
										onClick={() => field.onChange(item)}
										className={isActive ? 'dropdown-item__active' : ''}
									>
										{isActive && <i className='icon-accept' />}
										{item.label === undefined ? item : item.label}
									</Dropdown.Item>
								);
							})}
						</Dropdown.Menu>
					</Dropdown>
				</>
			)}
		/>
	);
};

DropdownForm.propTypes = {
	name: PropTypes.string.isRequired,
	control: PropTypes.any.isRequired,
	options: PropTypes.oneOfType([
		PropTypes.arrayOf(
			PropTypes.shape({
				value: PropTypes.string.isRequired,
				label: PropTypes.string.isRequired,
			}),
		),
		PropTypes.arrayOf(PropTypes.string),
	]).isRequired,
	label: PropTypes.string,
	className: PropTypes.string,
	isRequired: PropTypes.bool,
};

export default DropdownForm;
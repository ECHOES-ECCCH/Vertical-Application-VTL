import React, { useState, useEffect, useRef } from "react";
import { Dropdown } from "react-bootstrap";
import PropTypes from "prop-types";

const CommonDropdownValue = ({ options, selectedValue, setSelectedValue, placeholder, isDisabled, label, className }) => {
	const [ showPlaceholder, setShowPlaceholder ] = useState("");
	const [ typedCharacters, setTypedCharacters ] = useState("");
	const dropdownRef = useRef(null);
	const dropdownMenuRef = useRef(null);
	const clearTypedCharactersTimeout = useRef(null);

	const selectedIndex = options.findIndex(option => option.value === selectedValue);

	useEffect( () => {
		if(placeholder && placeholder.length > 0){
			setShowPlaceholder(placeholder);
		}
	}, [ placeholder ]);
    
	const changeSelectedIndex = (index) => {
		setSelectedValue(options[index].value);
		if (dropdownMenuRef.current) {
			const itemElement = dropdownMenuRef.current.children[index];
			if (itemElement) {
				const menuHeight = dropdownMenuRef.current.clientHeight;
				const itemHeight = itemElement.clientHeight;
				const scrollTo = itemElement.offsetTop - (menuHeight - itemHeight) / 2;
				dropdownMenuRef.current.scrollTop = scrollTo;
			}
		}
	};

	const handleKeyDown = (e) => {
		if (!dropdownRef.current)
			return;
		if (e.key.length === 1) {
			const typedChars = typedCharacters + e.key;
			const index = options.findIndex(option => option.label.toLowerCase().startsWith(typedChars.toLowerCase()));
			if (index >= 0) {
				changeSelectedIndex(index);
			}
			setTypedCharacters(typedChars);
			clearTimeout(clearTypedCharactersTimeout.current);
			clearTypedCharactersTimeout.current = setTimeout(() => {
				setTypedCharacters("");
			}, 1000);
			return;
		}
		switch (e.key) {
			case "ArrowDown":
				e.preventDefault();
				changeSelectedIndex((selectedIndex + 1) % options.length);
				break;
			case "ArrowUp":
				e.preventDefault();
				changeSelectedIndex((selectedIndex - 1 + options.length) % options.length);
				break;
		}
	};

	return (
		<>
			{ (label && label.length > 1) && (
				<label>{label}</label>
			)}
			<Dropdown
				ref={ dropdownRef }
				onKeyDown={ handleKeyDown }
				bsPrefix={ `dropdown-common ${ showPlaceholder && 'dropdown-common__placeholder'} ${className}` }
				drop='down'
			>
				<Dropdown.Toggle
					variant='dropdown'
					disabled={ isDisabled }
					id='common'
				>
					{ showPlaceholder ? showPlaceholder : options[selectedIndex]?.label }
				</Dropdown.Toggle>
				<Dropdown.Menu ref={ dropdownMenuRef }>
					{ options.map((item, index) => (
						<Dropdown.Item
							onClick={ () => { 
								setShowPlaceholder('');
								setSelectedValue(item.value);
							} }
							className={ `${index === selectedIndex ? "dropdown-item__active" : ""}` }
							key={ `${item.value}-${index+1}` }
							value={ item.value }
						>
							{ (index === selectedIndex) && <i className='icon-accept' /> }
							{ item.label }
						</Dropdown.Item>
					))}
					
				</Dropdown.Menu>
			</Dropdown>
		</>
	);
};

CommonDropdownValue.propTypes = {
	options: PropTypes.array,
	selectedValue: PropTypes.any,
	setSelectedValue: PropTypes.func,
	placeholder: PropTypes.string, 
	isDisabled: PropTypes.bool,
	label: PropTypes.string,
	className: PropTypes.string,
};

export default CommonDropdownValue;
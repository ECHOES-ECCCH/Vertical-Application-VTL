import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';

import CommonDropdown from '@Elements/Dropdown/CommonDropdown';

const PaginationsItemsPerPage = ( {count, setCount, title}) => {
	const [ dropdownOption, setDropdownOption ] = useState(0);
	
	const values = [
		{
			value: 30,
			label: "30",
		},
		{
			value: 60,
			label: "60",
		},
		{
			value: 90,
			label: "90",
		},
	];

	const onSelectOption = (index) => {

		setCount(values[index].value);
	};

	useEffect( () => {
		if (count){
			setDropdownOption(values.findIndex(el => el.value === count));
		}		
	}, [ count ]);

	return (
		<div className='pagination-top__item'>
			<div className='pagination-top--perpage'>{title}</div>
			<CommonDropdown
				options={
					values.map( it => it.label)
				} 
				selectedOption={ dropdownOption }
				setSelectedOption={ onSelectOption }
				className='dropdown-new'
			/>
		</div>
	);
};

PaginationsItemsPerPage.propTypes = {
	count: PropTypes.number,
	setCount: PropTypes.func,
	title: PropTypes.string,
};

export default PaginationsItemsPerPage;
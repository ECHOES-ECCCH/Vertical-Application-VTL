import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import PropTypes from 'prop-types';

import CommonDropdown from '@Elements/Dropdown/CommonDropdown';

const TranscriptionsSortSelect = ({ order, setOrder, isTextSearch }) => {
	const { t } = useTranslation();

	const [ dropdownOption, setDropdownOption ] = useState(0);
	
	const orderTypes = [
		{
			value: "last-modification-date",
			label:t('transcriptions.sort.editionDate'),
		},
		{
			value: "date-created",
			label:t('transcriptions.sort.publicationDate'),
		},
		{
			value: "title",
			label:t('transcriptions.sort.sortTitles'),
		},
		{
			value: "percent-of-transcribed",
			label:t('transcriptions.sort.advancement'),
		},
	];

	if(isTextSearch) {
		orderTypes.push(
			{
				value: "hit-count",
				label:t('transcriptions.sort.hitCount'),
			},
		);
	}

	const onSelectOption = (index) => {
		setOrder(orderTypes[index].value);
	};

	useEffect( () => {
		if (order){
			setDropdownOption(orderTypes.findIndex(el => el.value === order));
		}		
	}, [ order ]);

	return (
		<div className='pagination-top__item'>
			<div className='text-nowrap'>{t('transcriptions.sort.sortBy')}</div>
			<CommonDropdown 
				options={
					orderTypes.map( it => it.label)
				} 
				selectedOption={ dropdownOption }
				setSelectedOption={ onSelectOption }
				className='dropdown-new'
			/>
		</div>
	);
};

TranscriptionsSortSelect.propTypes = {
	order: PropTypes.string,
	setOrder: PropTypes.func,
	isTextSearch: PropTypes.bool,
};

export default TranscriptionsSortSelect;
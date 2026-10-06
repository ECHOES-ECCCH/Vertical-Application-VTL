import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import PropTypes from 'prop-types';

import CommonDropdown from '@Elements/Dropdown/CommonDropdown';

const TranscriptionsMyScopeSelect = ({ scope, setScope, visibility, setVisibility }) => {
	const { t } = useTranslation();

	const [ dropdownScopeOption, setDropdownScopeOption ] = useState(0);
	const [ dropdownVisibilityOption, setDropdownVisibilityOption ] = useState(0);
	
	const scopes = [
		{
			value: "creator_or_member",
			label:t('transcriptions.scope.myAll'),
		},
		{
			value: "creator",
			label:t('transcriptions.scope.creatorOnly'),
		},
		{
			value: "member",
			label:t('transcriptions.scope.memberOnly'),
		},
	];

	const visibilities = [
		{
			value: "all",
			label:t('transcriptions.scope.publicAndPrivate'),
		},
		{
			value: "public",
			label:t('transcriptions.scope.publicOnly'),
		},
		{
			value: "hidden",
			label:t('transcriptions.scope.privateOnly'),
		},
	];

	const onSelectedScopeOption = (index) => {
		setScope(scopes[index].value);
	};
	
	const onSelectedVisibilityOption = (index) => {
		setVisibility(visibilities[index].value);
	};

	useEffect( () => {
		if (scope){
			setDropdownScopeOption(scopes.findIndex(el => el.value === scope));
		}		
	}, [ scope ]);
	
	useEffect( () => {
		if (visibility){
			setDropdownVisibilityOption(visibilities.findIndex(el => el.value === visibility));
		}		
	}, [ visibility ]);

	return (
		<>
			<div className='pagination-top__item'>
				<div className='text-nowrap'>{t('transcriptions.scope.select')}</div>
				<CommonDropdown 
					options={
						scopes.map( it => it.label)
					}
					selectedOption={ dropdownScopeOption }
					setSelectedOption={ onSelectedScopeOption }
					className='dropdown-new'
				/>
			</div>
			{ setVisibility &&
			<div className='pagination-top__item'>
				<CommonDropdown 
					options={
						visibilities.map( it => it.label)
					}
					selectedOption={ dropdownVisibilityOption }
					setSelectedOption={ onSelectedVisibilityOption }
					className='dropdown-new'
				/>
			</div> }
		</>);
};

TranscriptionsMyScopeSelect.propTypes = {
	scope: PropTypes.string,
	setScope: PropTypes.func,
	visibility: PropTypes.string,
	setVisibility: PropTypes.func,
};

export default TranscriptionsMyScopeSelect;

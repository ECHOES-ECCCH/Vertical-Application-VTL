import React, { useState, useRef, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import PropTypes from 'prop-types';

import Button from '@Elements/Button/Button';

import styles from '@Pages/Auth/Group/Group.module.sass';

const GroupName = ({ propsGroupName = '', onAcceptName }) => {
	const { t } = useTranslation();
	const currentLang = useSelector((state) => state.app.currentLang);
	const [ isEditMode, setEditMode ] = useState(false);
	const [ inputValue, setInputValue ] = useState('');
	const [ placeholder, setPlaceholder ] = useState('');
	const nameInput = useRef(null);

	const editGroupName = () => {
		setEditMode(true);
	};

	useEffect( () => {
		setPlaceholder(t('group.groupNameInputPlaceholder'));
	}, [ currentLang ]);

	useEffect(() => {
		if (propsGroupName === '') {
			setEditMode(true);
		} else {
			setInputValue(propsGroupName);
			setEditMode(false);
		}
	}, [ propsGroupName, placeholder ]);

	useEffect(() => {
		nameInput.current.value = inputValue;
	}, [ inputValue ]);

	useEffect(() => {
		if (isEditMode) {
			nameInput.current.focus();
			if (inputValue === placeholder) {
				nameInput.current.value = '';
			}
		}
	}, [ isEditMode ]);

	const addNameCancel = () => {
		nameInput.current.value = inputValue;
		setEditMode(false);
	};

	const onNameAccept = () => {
		if (nameInput.current.value === '' || nameInput.current.value === placeholder) {
			setInputValue(placeholder);           
		} else {
			setInputValue(nameInput.current.value);
			onAcceptName(nameInput.current.value);
		}
		setEditMode(false);
	};
	const handleKeyDown = (ev) => {
		if (ev.key === 'Enter') {
			onNameAccept();
		}
	};

	return (
		<section className={ styles["group__name"] }>
			<div className={ styles["group__name--title"] }>
				{t('group.groupName')}
			</div>
			<div>
				<label className={ `input-group ${styles["group__name--form"]}` }>
					<input
						type='text'
						ref={ nameInput }
						disabled={ !isEditMode }
						onKeyDown={ handleKeyDown }
						className={ `form-control ${styles["group__name--input"]}` }
					/>
					<span className='d-none'>{t('group.groupName')}</span>
				
					{!isEditMode &&
					<div className='input-group-append'>
						<Button 
							onClick={ editGroupName }
							variant='secondary icon icon-xl'
						>
							<span className='d-none'>Edit</span>
							<i className='icon-edit' />
						</Button>
					</div>}

					{isEditMode &&
					<div className='input-group-append'>
						<Button 
							onClick={ addNameCancel }
							variant='secondary icon'
						>
							<i className='icon-close' />
						</Button>
						<Button 
							onClick={ onNameAccept }
							variant='primary icon'
						>
							<i className='icon-accept' />
						</Button>
					</div>}
				</label>
				
			</div>
		</section>
	);
};

GroupName.propTypes = {
	propsGroupName: PropTypes.string,
	onAcceptName: PropTypes.func,
};

export default GroupName;

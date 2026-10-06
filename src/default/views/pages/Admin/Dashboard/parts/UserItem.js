import React, { useState, useRef, useEffect } from "react";
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";
import { useHistory } from 'react-router';
import { reverse } from 'named-urls';

import Button from "@Elements/Button/Button";

import url from "@/router/urls";

import styles from '@Pages/Admin/Dashboard/Dashboard.module.sass';

const UserItem = ({ orderNum, id, email, quota, usedSpaceQuota, onQuotaChange }) => {
	const history = useHistory();
	const { t } = useTranslation();

	const [ editMode, setEditMode ] = useState(false);
	const nameInput = useRef(null);
	const [ inputValue, setInputValue ] = useState(quota);

	useEffect(() => {
		nameInput.current.value = inputValue;
	}, [ inputValue ]);

	const showUserTranscriptions = () => {
		history.push(reverse(url.auth.admin.userTranscriptions, { id: id }));
	};

	const changeQuotaSubmit = () => {
		setInputValue(nameInput.current.value);
		onQuotaChange(id, nameInput.current.value);
		setEditMode(false);
	};
	const changeQuotaCancel = () => {
		nameInput.current.value = inputValue;
		setEditMode(false);
	};

	const handleKeyDown = (ev) => {
		if (ev.key === 'Enter') {
			changeQuotaSubmit();
		}
	};
	return (
		<div className={ styles["userlist__item"] + " " + styles["userlist__user-item"] }>
			<div className={ styles["userlist__item--order"] }>{orderNum + 1}.</div>
			<div className={ styles["userlist__item--email"] }>
				<Button
					onClick={ () => showUserTranscriptions() }
					variant='link'
					className={ styles["userlist__item--text-elipsis"] + " " + styles["userlist__item--link"] }
				>
					{email}
				</Button>
			</div>
			<div className={ styles["userlist__item--used"] }>
				<span className={styles["userlist__item--placeholder"]}>{t('admin.dashboard.used')}</span>
				{usedSpaceQuota !== null ? usedSpaceQuota : 0}
				<span className={styles["userlist__item--quota-text"]}>{t('auth.account.mb')}</span>
			</div>
			<div className={styles["userlist__item--limit"]}>
				<input
					type='text'
					ref={ nameInput }
					disabled={ !editMode }
					onKeyDown={ handleKeyDown }
					className={ styles["userlist__item--input"] }
				/>
				<span className={ styles["userlist__item--quota-text"] }>{t('auth.account.mb')}</span>
				{editMode ? (
					<div className='input-group-append'>
						<Button
							onClick={ () => changeQuotaCancel() }
							variant='editQuota icon'
							className={ styles["userlist__item--quota-btn-cancel"] }
						>
							<i className='icon-close' />
						</Button>
						<Button
							className={styles["userlist__item--quota-btn"]} 
							onClick={ changeQuotaSubmit }
							variant='quotaAccept icon'
						>
							<i className='icon-accept' />
						</Button>
					</div>
				) : (
					<Button
						variant='link'
						onClick={ () => setEditMode(true) }
						className={ styles["userlist__item--quota-btn"] }
					>
						{t('admin.dashboard.changeLimit')}
					</Button>
				)}
			</div>			
		</div>
	);
};

UserItem.propTypes = {
	id: PropTypes.number,
	orderNum: PropTypes.number,
	email: PropTypes.string,
	usedSpaceQuota: PropTypes.number,
	quota: PropTypes.number,
	onQuotaChange: PropTypes.func,
};

export default UserItem;
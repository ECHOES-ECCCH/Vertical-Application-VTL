import React, { useState, useRef, useEffect } from "react";
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";
import { useHistory } from "react-router";
import { reverse } from "named-urls";

import Button from "@Elements/Button/Button";

import url from "@/router/urls";

import styles from "@Pages/Admin/Dashboard/Dashboard.module.sass";

const ProjectItem = ({ orderNum, id, readOnly, title, email, quota, usedQuota, onQuotaChange }) => {
	const history = useHistory();
	const { t } = useTranslation();

	const [editMode, setEditMode] = useState(false);
	const nameInput = useRef(null);
	const [ inputValue, setInputValue ] = useState(quota || 0);

	useEffect(() => {
		nameInput.current.value = inputValue;
	}, [ inputValue ]);

	const openTranscription = (transcriptionId) => {
		history.push(reverse(url.transcription.details, { transcriptionId: transcriptionId }));
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
	return (
		<div className={styles["userlist__item"] + " " + styles["userlist__project-item"]}>
			<div className={styles["userlist__item--order"]}>{orderNum + 1}</div>
			<div className={styles["userlist__item--label"]}>{t('admin.dashboard.project')}</div>

			<Button
				variant='link'
				type='button'
				onClick={() => openTranscription(id)}
				className={styles["userlist__item--project"] + " " + styles["userlist__item--link"]}
			>
				<div className={styles["userlist__item--text-elipsis"]}>{title}</div>
			</Button>
			<div className={styles["userlist__item--label"]}>{t('admin.dashboard.superadministrator')}</div>
			<div className={styles["userlist__item--email"]}>
				<div className={styles["userlist__item--text-elipsis"]}>{email}</div>
			</div>
			<div className={styles["userlist__item--label"]}>{t('admin.dashboard.usedHeader')}</div>
			<div className={styles["userlist__item--used"]}>
				<span className={styles["userlist__item--placeholder"]}>{t('admin.dashboard.used')}</span>
				{usedQuota !== null ? usedQuota : 0}
				<span className={styles["userlist__item--quota-text"]}>{t('auth.account.mb')}</span>
			</div>
			<div className={styles["userlist__item--label"]}>{t('admin.dashboard.projectLimits')}</div>
			<div className={styles["userlist__item--limit"]}>
				<input 
					className={ styles["userlist__item--input"] } 
					type='text'
					ref={ nameInput }
					disabled={ !editMode }
				/>
				<span className={styles["userlist__item--quota-text"]}>{t('auth.account.mb')}</span>
				{editMode ? (
					<div className='input-group-append'>
						<Button
							variant='editQuota icon'
							className={styles["userlist__item--quota-btn-cancel"]}
							onClick={() => changeQuotaCancel()}
						>
							<i className='icon-close' />
						</Button>
						<Button 
							className={styles["userlist__item--quota-btn"]} 
							variant='quotaAccept icon' 
							onClick={() => changeQuotaSubmit()}
						>
							<i className='icon-accept' />
						</Button>
					</div>
				) : (
					!readOnly && 
					<Button
						variant='link'
						onClick={() => setEditMode(true)}
						className={styles["userlist__item--quota-btn"]}
					>
						{t('admin.dashboard.changeLimit')}
					</Button>
				)}
			</div>
		</div>
	);
};

ProjectItem.propTypes = {
	id: PropTypes.number,
	readOnly: PropTypes.bool,
	orderNum: PropTypes.number,
	email: PropTypes.string,
	title: PropTypes.string,
	quota: PropTypes.number,
	usedQuota: PropTypes.number,
	onQuotaChange: PropTypes.func,
};

export default ProjectItem;

import React, { useState, useEffect } from "react";
import { useSelector } from "react-redux";
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router';
import PropTypes from "prop-types";
import { Dropdown } from "react-bootstrap";

import { CheckViewModePermission } from "@/permissions/ViewModes";

import api from "@/services/api";
import Button from "@Elements/Button/Button";

import styles from "@Pages/Transcription/TranscriptionPermissions/TranscriptionPermissions.module.sass";

const TranscriptionPermissionsUserItem = ({ orderNum, email, userId, role, onDeleteUserProps, summarizeData, viewMode }) => {
	const { currentLang } = useSelector(state => state.app);
	const { t } = useTranslation();

	const { transcriptionId } = useParams();

	const isLoggedIn = useSelector(state => state.auth.credentials.isLoggedIn);
	const [ userRole, setRole ] = useState(role);
	const [ userRoleName, setRoleName ] = useState("");
	const [ availableRoles, setAvailableRoles ] = useState([]);

	useEffect(() => {
		let currentRole = availableRoles.filter(item => item.code === userRole)[0];
		if (currentRole) {
			setRoleName(currentRole[currentLang]);
		}
        
	}, [ userRole, role, availableRoles, currentLang ]);

	useEffect(() => {
		if (isLoggedIn) {
			getAvailableRoles();
		}
	}, [ isLoggedIn ]);

	const getAvailableRoles = () => {
		api.get("/user-roles")
			.then(res => {
				setAvailableRoles(res.data);
			});
	};

	const changeRole = roleCode => {
		setRole(roleCode);
		api.put(`/transcriptions/${transcriptionId}/allowed-users`, {
			userId: userId,
			roleCode: roleCode,
		});
	};
	const onDeleteUser = (id) => {
		onDeleteUserProps(id);
	};

	const roleSelector = (
		<Dropdown className={ styles["permissions__userlist--dropdown"] }>
			<Dropdown.Toggle
				id={ `roleSelector${orderNum}` }
				className={ styles["permissions__userlist--dropdown-role"] }
			>
				{userRoleName}
			</Dropdown.Toggle>
			<Dropdown.Menu className={ styles["permissions__userlist--dropdown-list"] }>
				{availableRoles.map(item => (
					<Dropdown.Item
						key={ item.ord }
						onClick={ () => changeRole(item.code) }
						className={`
                            ${styles["permissions__userlist--dropdown-item"]}
                            ${item[currentLang] === userRoleName ? styles["permissions__userlist--dropdown-item--active"] : ''}
                        `}
					>
						{item[currentLang] === userRoleName && <i className='icon-accept' />}
						{item[currentLang]}
					</Dropdown.Item>
				))}
			</Dropdown.Menu>
		</Dropdown>
	);

	const getAdminName = () => {
		const adminObject = availableRoles.filter(item => item.code === "admin")[0];

		return adminObject ? adminObject[currentLang] : null;
	};

	const getRoleName = () => {
		return userRoleName;
	};

	return (
		<div
			key={ email }
			className={ orderNum === 0 ? styles["permissions__userlist--item--admin"] : styles["permissions__userlist--item"] }
		>
			<div className={ styles["permissions__userlist--item-order"] }>
				{orderNum + 1} 
				
			</div>
			<span className={ styles["permissions__userlist--item-descplaceholder"] }>	{t('transcription.permissions.emailAddress')}</span>
			<a href={`mailto:${email}`} className={ styles["permissions__userlist--item-email"] }>
				{email}
			</a>
			<span className={ styles["permissions__userlist--item-descplaceholder"] }>	{t('transcription.permissions.role')} </span>
			<span className={ styles["permissions__userlist--item-descplaceholder"] }>	{getAdminName()} </span>
			{orderNum === 0 &&
				<div className={ styles["permissions__userlist--item-role--admin"] }>
					{getAdminName()}
				</div>}

			{orderNum !== 0 &&
				<div className={ styles["permissions__userlist--item-role"] }>
					{ CheckViewModePermission("canChangeUserRole", viewMode) && roleSelector }
					{ !CheckViewModePermission("canChangeUserRole", viewMode) && <span style={ { textTransform: "capitalize" } }>{ getRoleName() }</span> }
				</div>}
			
			<div className={ styles["permissions__userlist--item-activity"] }>
				{summarizeData &&
					<>
						<span className={ styles["permissions__userlist--item-descplaceholder"] }>	{t('transcription.permissions.activity')} </span>
						<span className={ styles["permissions__userlist--item-activity--item"] }>
							{t('transcription.permissions.filled') + summarizeData.filled } 
						</span>
						<span className={ styles["permissions__userlist--item-activity--item"] }>
							{t('transcription.permissions.corrected') + summarizeData.corrected }
						</span> 
						<span className={ styles["permissions__userlist--item-activity--item"] }>
							{t('transcription.permissions.accepted') + summarizeData.accepted }
						</span>
						<span className={ styles["permissions__userlist--item-activity--item"] }>
							{t('transcription.permissions.rejected') + summarizeData.rejected }
						</span>
					</>}
			</div>
			
			{CheckViewModePermission("canRemoveUser", viewMode) && orderNum !== 0 &&
				<Button
					variant='tetriary'
					type='button'
					onClick={ () => onDeleteUser(userId) }
					className={ styles["permissions__userlist--item-button"] }
				>
					{t('group.removeUser')}
				</Button>}
		</div>
	);
};

TranscriptionPermissionsUserItem.propTypes = {
	orderNum: PropTypes.number,
	email: PropTypes.string,
	userId: PropTypes.number,
	role: PropTypes.string,
	summarizeData: PropTypes.object,
	viewMode: PropTypes.string,
	onDeleteUserProps: PropTypes.func,
};

export default TranscriptionPermissionsUserItem;

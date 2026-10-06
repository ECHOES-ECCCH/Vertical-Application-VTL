import React, { useState, useEffect } from "react";
import { useSelector } from "react-redux";
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";
import { Dropdown } from "react-bootstrap";

import api from "@/services/api";
import Button from "@Elements/Button/Button";

import styles from "@Pages/Auth/Group/Group.module.sass";

const GroupUserItem = ({ groupId, orderNum, email, userId, role, onDeleteUserProps }) => {
	const { currentLang } = useSelector(state => state.app);
	const { t } = useTranslation();

	const isLoggedIn = useSelector(state => state.auth.credentials.isLoggedIn);
	const myUserId = useSelector(state => state.auth.credentials.id);
	const [ userRole, setRole ] = useState(role);
	const [ userRoleName, setRoleName ] = useState("");
	const [ availableRoles, setAvailableRoles ] = useState([]);

	useEffect(() => {
		let currentRole = availableRoles.filter(item => item.code === userRole)[0];
		if (currentRole) {
			setRoleName(currentRole[currentLang]);
		}
	}, [ userRole, role, availableRoles ]);

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
		api.put(`/users-groups/${groupId}`, {
			user: {
				id: userId,
				email: email },
			role: {
				code: roleCode,
			},
		});
	};

	const deleteUser = id => {
		onDeleteUserProps(id);
	};

	const roleSelector = (
		<Dropdown className={ styles["group__role--dropdown"] }>
			<Dropdown.Toggle
				id={ "roleSelector" + orderNum }
				className={ styles["group__role--dropdown-btn"] }
			>
				{userRoleName}
			</Dropdown.Toggle>
			<Dropdown.Menu>
				{availableRoles.map(item => (
					<Dropdown.Item
						key={ item.ord }
						onClick={ () => changeRole(item.code) }
					>
						{item[currentLang]}
					</Dropdown.Item>
				))}
			</Dropdown.Menu>
		</Dropdown>
	);

	const getAdminName = () => {
		const adminObject = availableRoles.filter(item => item.code === "admin")[0];
		if (adminObject) {
			return adminObject[currentLang];
		}
		return null;
	};

	return (
		<div className={ styles["group__userlist--item"] }>
			<div className={ styles["group__userlist--item-order"] }>{orderNum + 1}.</div>
			<div className={ styles["group__userlist--item-email"] }>
				<span className={ styles["group__userlist--item-placeholder"] }>{t('group.groupUserEmail')}</span>
				{email}
			</div>
			{orderNum !== 0 && (
				<div className={ styles["group__userlist--item-role"] }>
					<span className={ styles["group__userlist--item-placeholder"] }>{t('group.groupUserRole')}</span>
					{roleSelector}
				</div>
			)}
			{orderNum === 0 && (
				<div className={ styles["group__role--admin"] }>
					<span className={ styles["group__userlist--item-placeholder"] }>{t('group.groupUserRole')}</span>
					{getAdminName()}
				</div>
			)}

			{!( (orderNum === 0) || (userId === myUserId)) && (
				<Button
					variant='link'
					type='button'
					onClick={ () => deleteUser(userId) }
					className={ styles["group__userlist--item-button"] }
				>
					{t('group.removeUser')}
				</Button>
			)}
		</div>
	);
};

GroupUserItem.propTypes = {	
	groupId: PropTypes.number,
	orderNum: PropTypes.number,
	email: PropTypes.string,
	userId: PropTypes.number,
	role: PropTypes.string,
	onDeleteUserProps: PropTypes.func,
};

export default GroupUserItem;

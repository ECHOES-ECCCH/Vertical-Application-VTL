import React, { useState, useEffect } from 'react';
import { useParams, useHistory } from "react-router-dom";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Container } from 'react-bootstrap';
import { reverse } from "named-urls";

import api from "@/services/api";
import url from "@/router/urls";

import GroupUserItem from "@Pages/Auth/Group/elements/GroupUserItem";
import GroupUserAdd from "@Pages/Auth/Group/elements/GroupUserAdd";
import GroupName from "@Pages/Auth/Group/elements/GroupName";

import Error403 from "@Pages/Error/Error403";

import Button from "@Elements/Button/Button";
import { appActions } from '@/store/actions/app.actions';

import styles from "@Pages/Auth/Group/Group.module.sass";
import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";

import ModalBaseCancelConfirm from '@Elements/Modal/ModalBaseCancelConfirm';

const Group = () => {
	const { t } = useTranslation();
	const dispatch = useDispatch();
	const isLoggedIn = useSelector((state) => state.auth.credentials.isLoggedIn);
	const history = useHistory();
	const { id: groupId } = useParams();

	const [isOwner, setOwner] = useState(false);
	const [groupName, setGroupName] = useState("");
	const [groupMembers, setGroupMembers] = useState([]);

	const [groupNameExists, setGroupNameExists] = useState("");

	const [noAccess, setNoAccess] = useState(false);

	const [deleteUserId, setDeleteUserId] = useState(null);
	const [deleteCurrentGroup, setDeleteCurrentGroup] = useState(false);	

	const breadcrumbsList = [
		{ id: 1, label: "home", link: url.home },
		{ id: 2, label: t('breadcrumbs.accountMyProfile'), link: url.auth.profile },
		{ id: 3, label: t('breadcrumbs.group') },
	];

	useEffect(() => {
		if (isLoggedIn) {
			if (!groupId) {
				setOwner(true);
			} else {
				getGroupData();
			}
		}
	}, [isLoggedIn, groupId]);

	const onAcceptName = name => {
		if (!groupId) {
			createGroup(name);
		} else {
			changeGroupName(name);
		}
	};

	const getGroupData = () => {
		if (!groupId) return;
		api.get("/groups/" + groupId)
			.then(response => {
				setOwner(response.data.owner);
				setGroupName(response.data.name);
				setGroupMembers(response.data.members);
			}).catch(() => {
				setNoAccess(true);
			});
	};

	const createGroup = name => {
		api.post("/groups", { name: name })
			.then(response => {
				history.replace(reverse(url.auth.group.edit, { id: response.data.id }));
			})
			.catch((error) => {
				if (error.data.code === "groups.error.groupAlreadyExists") {
					setGroupNameExists(t('group.groupNameExists'));
				}
			});
	};
	const changeGroupName = name => {
		setGroupNameExists("");
		if (groupName !== name) {
			api.put("/groups/" + groupId, { name: name })
				.catch((error) => {
					if (error.data.code === "groups.error.groupAlreadyExists") {
						setGroupNameExists(t('group.groupNameExists'));
					}
				});
		}
	};

	const onAddUser = () => {
		getGroupData();
	};

	const onDeleteUser = (userId) => {
		setDeleteUserId(userId);
	};

	const onDeleteUserConfirmed = async () => {
		dispatch(appActions.setLoading(true));
		try {
			await api.delete(`/users-groups/${groupId}/user/${deleteUserId}`);
			getGroupData();
			setDeleteUserId(null);
		} catch (error) {
			// eslint-disable-next-line
			console.log(error);
		} finally {
			dispatch(appActions.setLoading(false));
		}
	};

	const onDeleteGroupConfirmed = async () => {
		dispatch(appActions.setLoading(true));
		try {
			await api.delete(`/groups/${groupId}`);
			dispatch(appActions.setLoading(false));
			history.replace(url.auth.profile);
		} catch (error) {
			// eslint-disable-next-line
			console.log(error);
		} finally {
			dispatch(appActions.setLoading(false));
		}
	};

	return (!noAccess ? (
		<>
			{/* DELETE USER MODAL */}
			<ModalBaseCancelConfirm
				modalVisible={!!deleteUserId}
				setModalVisible={() => setDeleteUserId(null)}
				title={t('modals.groupDeleteUser.title')}
				bodyHTML={t('modals.groupDeleteUser.bodyHTML')}
				cancelButtonName={t('form.cancel')}
				confirmButtonName={t('modals.groupDeleteUser.button')}
				onConfirm={onDeleteUserConfirmed}
			/>
			{/* DELETE GROUP MODAL */}
			<ModalBaseCancelConfirm
				modalVisible={deleteCurrentGroup}
				setModalVisible={() => setDeleteCurrentGroup(false)}
				title={t('modals.groupDeleteGroup.title')}
				bodyHTML={t('modals.groupDeleteGroup.bodyHTML')}
				cancelButtonName={t('form.cancel')}
				confirmButtonName={t('modals.groupDeleteGroup.button')}
				onConfirm={onDeleteGroupConfirmed}
			/>
			<main className={`${styles["group"]}`}>
				<Container>
					<Breadcrumbs breadcrumbsList={breadcrumbsList} />
					<div className={styles["group__divider"]} />

					<div className='block block--1020'>
						<div className='block__header block__header--space-between'>
							<h1 className='text--title'>{groupId ? t('group.headerGroup') : t('group.headerGroupCreateNewOne')}</h1>
							{isOwner && groupId && (
								<Button
									variant='link'
									type='button'
									className={`${styles["account__button"]}`}
									onClick={() => setDeleteCurrentGroup(true)}
								>
									{t('group.deleteGroup')}
								</Button>
							)}
						</div>
						<div className='block__content'>
							{isOwner &&
								<GroupName
									propsGroupName={groupName}
									onAcceptName={onAcceptName}
								/>}
							{(groupNameExists.length > 1) && (
								<div className={styles["group__add-user--error"]}>
									<i className='icon-close' />
									{groupNameExists}
								</div>
							)}

							{!isOwner && (
								<section className={styles["group__name"]}>
									{t('group.groupName')} {groupName}
								</section>
							)}

							<div className={styles["group__userlist"]}>
								{groupId !== 0 &&
									groupMembers.map((user, index) => (
										<GroupUserItem
											orderNum={index}
											groupId={groupId}
											key={user.user.email}
											email={user.user.email}
											userId={user.user.id}
											role={user.role.code}
											onDeleteUserProps={onDeleteUser}
										/>
									))}
							</div>

							{groupId &&
								<GroupUserAdd
									groupId={groupId}
									onAddUserProps={onAddUser}
								/>}
						</div>
					</div>
				</Container>
			</main>
		</>
	) : (
		<Error403 />
	));
};

export default Group;

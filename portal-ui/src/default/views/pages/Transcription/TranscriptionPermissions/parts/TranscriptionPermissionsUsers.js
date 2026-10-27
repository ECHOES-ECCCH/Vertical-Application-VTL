import React, { useState, useEffect } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { useParams } from 'react-router';
import PropTypes from "prop-types";

import { useShowModal } from '@Elements/Modal/ModalHooks';

import api from "@/services/api";
import { appActions } from '@/store/actions/app.actions';

import FormSubmit from "@Elements/Form/FormSubmit";
import FormInput from "@Elements/Form/FormInput";

import { CheckViewModePermission } from "@/permissions/ViewModes";

import TranscriptionPermissionsUserItem from "@Pages/Transcription/TranscriptionPermissions/parts/TranscriptionPermissionsUserItem";
import TranscriptionPermissionsUserItemHeader from "@Pages/Transcription/TranscriptionPermissions/parts/TranscriptionPermissionsUserItemHeader";

import styles from "@Pages/Transcription/TranscriptionPermissions/TranscriptionPermissions.module.sass";

const TranscriptionPermissionsUsers = ({ users, getTranscriptionPermissionsData, summarizeData, viewMode }) => {
	const { t } = useTranslation();

	const { transcriptionId } = useParams();
	const dispatch = useDispatch();
	const modalConfirmInfo = useSelector((state) => state.app.modalConfirmInfo);
	const [isSendingEmail, setIsSendingEmail] = useState(false);
	const [deleteUserId, setDeleteUserId] = useState(null);

	const defaultValues = {
		email: '',
	};

	const { register, handleSubmit, formState: { errors, isDirty, isValid }, setError, reset } = useForm({
		defaultValues: defaultValues,
		mode: 'onChange',
	});

	const modalDeleteUserFromTranscription = useShowModal({
		title: t('modals.permissionsDeleteUser.title'),
		name: 'deleteGroup',
		componentPath: './parts/ModalDeleteUserFromTranscription',
	});

	const onDeleteUser = (userId) => {
		setDeleteUserId(userId);
		modalDeleteUserFromTranscription();
	};

	useEffect(() => {
		if (modalConfirmInfo === "USER_DELETE_CONFIRMED") {
			api.delete(`/transcriptions/${transcriptionId}/allowed-users/user/${deleteUserId}`)
				.then(() => {
					getTranscriptionPermissionsData();
				});
		}
		dispatch(appActions.setModalConfirmInfo(null));
	}, [modalConfirmInfo]);

	const onAddUserSubmit = (data) => {
		setIsSendingEmail(true);
		api.post(`/transcriptions/${transcriptionId}/allowed-users`, { email: data.email })
			.then(() => {
				reset();
				setIsSendingEmail(false);
				getTranscriptionPermissionsData();
			}).catch((error) => {
				const errorCode = error.data.code.split('.')[2];
				setError('email', { type: errorCode, message: t('form.messages.' + errorCode) });
				setIsSendingEmail(false);
			});
	};

	return (
		<div>
			<div className={styles["permissions__userlist-header"]}>{t('transcription.permissions.users') + ` (${users.length})`}</div>
			<div className={styles["permissions__userlist"]}>
				<TranscriptionPermissionsUserItemHeader />
				{users && (
					users.map((user, index) => (
						<TranscriptionPermissionsUserItem
							orderNum={parseInt(index)}
							key={user.user.email}
							email={user.user.email}
							userId={parseInt(user.user.id)}
							role={user.role.code}
							getTranscriptionPermissionsData={getTranscriptionPermissionsData}
							onDeleteUserProps={onDeleteUser}
							summarizeData={summarizeData.filter((element) => element.userId === parseInt(user.user.id))[0]}
							viewMode={viewMode}
						/>
					))
				)}

			</div>
			{CheckViewModePermission("canAddUser", viewMode) &&
				<>
					<h3 className={styles["permissions__additional-text"]}>{t('transcription.permissions.addUserTitle')}</h3>
					<p className='text--small'>{t('transcription.permissions.addUserEmail')} <span style={{ color: "#005ca9" }}>*</span></p>
					<p className='text--small' style={{ color: "#606060", fontSize: "0.8rem" }}>{t('transcription.permissions.addUserInfo')}</p>
					<div className={styles["permissions__add--container"]}>
						<form
							className={styles["permissions__add--form"]}
							onSubmit={handleSubmit(onAddUserSubmit)}
						>
							<FormInput
								name='email'
								register={register}
								dict={null}
								placeholder={t('transcription.permissions.addUserPlaceholder')}
								validateData={
									{
										pattern: {
											value: /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/, // eslint-disable-line
											message: t('form.messages.invalidEmail'),
										},
									}
								}
								errorMessage={errors && errors.email && errors.email.message}
								inputContainerClassName='permissions__add--form-input'
							/>
							<div style={{ display: "flex", alignItems: "center" }}>
								<div style={{ color: "#606060", fontSize: "0.8rem" }}><span style={{ color: "#005ca9" }}>*</span> {t('transcription.permissions.requiredFields')}</div>
								<FormSubmit
									variant='submit'
									className={styles["permissions__add--form-submit"]}
									label={t('transcription.permissions.addUserButton')}
									disabled={isSendingEmail || !isDirty || !isValid}
								/>
							</div>
						</form>
					</div>
				</>}
		</div>
	);
};

TranscriptionPermissionsUsers.propTypes = {
	users: PropTypes.array,
	viewMode: PropTypes.string,
	summarizeData: PropTypes.array,
	getTranscriptionPermissionsData: PropTypes.func,
};

export default TranscriptionPermissionsUsers;

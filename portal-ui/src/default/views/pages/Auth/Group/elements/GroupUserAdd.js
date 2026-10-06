import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import PropTypes from 'prop-types';

import api from "@/services/api";

import Button from '@Elements/Button/Button';

import styles from '@Pages/Auth/Group/Group.module.sass';

const GroupUserAdd = ({ groupId, onAddUserProps }) => {
	const { t } = useTranslation();

	const {
		register,
		handleSubmit,
		formState,
		reset,
		setError,
	} = useForm({
		mode: 'onBlur',
		defaultValues: {
			email: '',
		},
	});

	const {
		errors,
	} = formState;

	const [isSendingEmail, setIsSendingEmail] = useState(false);

	const onAddUser = (data) => {
		setIsSendingEmail(true);
		api.post(`/users-groups/${groupId}`, { email: data.email })
			.then(() => {
				onAddUserProps();
				reset();
				setIsSendingEmail(false);
			}).catch((error) => {
				if (error.data.code === "groups.error.noUserWithGivenEmail") {
					setError('email', { type: 'notFound', message: t('form.messages.emailNotExists') });
				} else if (error.data.code === "groups.error.userAlreadyExistsInGroup") {
					setError('email', { type: 'notFound', message: t('form.messages.emailExistsInGroup') });
				}
				setIsSendingEmail(false);
			});
	};

	return (
		<section className={styles["group__add-user"]}>
			<h2 onClick={() => setError('email', { type: 'manual', message: 'Nieprawidłowy adres email' })} className={styles["group__add-user--header"]}>
				{t('group.addUserHead')}
			</h2>
			<p className={styles["group__add-user--description"]}>
				{t('group.addUserText')}
			</p>
			<form
				onSubmit={handleSubmit(onAddUser)}
				noValidate
			>
				<label className={`input-group ${styles["group__add-user--tf"]}`}>
					<input
						{...register('email', {
							pattern: {
								value: /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/, // eslint-disable-line
								message: t('form.messages.invalidEmail'),
							},
						})}
						type='email'
						placeholder={t('group.addUserPlaceholder')}
						className={
							`${styles["group__add-user--field"]}
						${errors.email && styles["group__add-user--field-error"]}
						`
						}
					/>
					<div className='input-group-append'>
						<Button
							type='submit'
							disabled={isSendingEmail || !formState.isDirty || !formState.isValid}
						>
							{t('group.addToButton')}
						</Button>
					</div>
					{errors.email && (
						<div className={styles["group__add-user--error"]}>
							<i className='icon-close' />
							{errors.email.message}
						</div>
					)}
				</label>
			</form>
		</section>
	);
};

GroupUserAdd.propTypes = {
	groupId: PropTypes.number,
	onAddUserProps: PropTypes.func,
};

export default GroupUserAdd;
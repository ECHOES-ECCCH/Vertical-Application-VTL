import React from "react";
import { useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { useForm } from "react-hook-form";
import { useParams, useHistory } from "react-router";
import { Form, Container } from "react-bootstrap";

import api from "@/services/api";
import url from "@/router/urls";

import { alertActions } from "@/store/actions/alert.actions";

import Alert from "@Elements/Alert/Alert";
import FormInputPassword from "@Elements/Form/FormInputPassword";
import FormInputPasswordRepeated from "@Elements/Form/FormInputPasswordRepeated";
import FormSubmit from "@Elements/Form/FormSubmit";

import styles from "@Pages/Auth/ChangePassword/ChangePassword.module.sass";

const ChangePassword = () => {
	const { t } = useTranslation();

	const { token } = useParams();
	const dispatch = useDispatch();
	const history = useHistory();

	const defaultValues = {
		newPassword: "",
		repeatedNewPassword: "",
	};

	const {
		register,
		handleSubmit,
		formState,
		setError,
		getValues,
	} = useForm({
		defaultValues: defaultValues,
		mode: "onChange",
	});

	const {
		errors,
	} = formState;

	const onSubmit = data => {
		data.token = token;
		api
			.post("/auth/change-password", {
				...data,
			})
			.then(() => {
				dispatch(
					alertActions.setAlert({
						type: "success",
						icon: "icon-accept",
						text: "auth.passwordChange.success",
						close: true,
						important: true,
					}),
				);
				history.replace(url.auth.login);
			})
			.catch(error => {
				for (const errorField in error.data) {
					if (error.data[errorField]) {
						const errorCode = error.data[errorField];
						setError(errorField, errorCode, t('form.messages.'+errorCode));
					}
				}
				dispatch(
					alertActions.setAlert({
						type: "danger",
						icon: "icon-circle-warning-empty",
						text: "auth.passwordChange.error",
						close: true,
					}),
				);
			});
	};

	return (
		<main>
			<Container>
				<div className='block block--650'>
					<Alert />
					<h1 className='text--title'>{t('auth.passwordChange.title')}</h1>
					<h2 className={ styles["change__instruction"] }>{t('auth.passwordChange.instruction')}</h2>
					<form
						onSubmit={ handleSubmit(onSubmit) }
						noValidate
						className='auth-form__fields auth-form__fields--small-margin'
					>
						<Form.Group controlId='newPassword'>
							<FormInputPassword
								register={ register }
								dict={ null }
								errors={ errors }
								name='newPassword'
							/>
						</Form.Group>
						<Form.Group controlId='repeatedNewPassword'>
							<FormInputPasswordRepeated
								register={ register }
								dict={ null }
								errors={ errors }
								getValues={ getValues() }
								name='repeatedNewPassword'
							/>
						</Form.Group>
						<Form.Group
							controlId='registerFormSubmit'
							className={ styles["change__submit"] }
						>
							<FormSubmit
								label={t('auth.account.changePassword') }
								variant='submit'
								disabled={ !formState.isDirty || !formState.isValid }
							/>
						</Form.Group>
					</form>
				</div>
			</Container>
		</main>
	);
};

export default ChangePassword;

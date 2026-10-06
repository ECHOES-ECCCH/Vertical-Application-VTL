import React from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';

import { useForm } from 'react-hook-form';
import { Link } from "react-router-dom";
import { Row, Col, Form } from 'react-bootstrap';

import { authActions } from "@/store/actions/auth.actions";

import FormInputPassword from '@Elements/Form/FormInputPassword';
import FormInputEmail from '@Elements/Form/FormInputEmail';
import FormCheckbox from "@Elements/Form/FormCheckbox";
import FormSubmit from "@Elements/Form/FormSubmit";

import Alert from "@Elements/Alert/Alert";

import url from "@/router/urls";

import styles from "@Pages/Auth/Login/Login.module.sass";

const defaultValues = {
	email: '',
	password: '',
	rememberMe: false,
};

const LoginForm = () => {
	const dispatch = useDispatch();

	const { t } = useTranslation();

	const isLoginLoading = useSelector((state) => state.auth.login.isLoading);
	const returnUrl = useSelector((state) => state.auth.login.returnUrl);

	const {
		register,
		handleSubmit,
		formState: {errors, isValid},
	} = useForm({ defaultValues: defaultValues, mode: 'onBlur' });

	const onSubmit = data => {
		dispatch(authActions.signIn(data, returnUrl));
	};

	return (
		<div className={styles["login-form"]}>
			<Alert className={styles["login-alert"]} />
			<h1 className={styles["login-form__text--header"]}>{t('auth.login.title')}</h1>
			<form
				onSubmit={handleSubmit(onSubmit)}
				className={styles["login-form__fields"]}
			>
				<Form.Group controlId='loginFormEmail'>
					<FormInputEmail
						register={register}
						dict={null}
						errors={errors}
					/>
				</Form.Group>

				<Form.Group controlId='loginFormPassword'>
					<FormInputPassword
						register={register}
						dict={null}
						errors={errors}
						skipPatternValidation
					/>
				</Form.Group>

				<Form.Group
					controlId='loginFormRemember'
					className={styles["login-rememberme"]}
				>
					<Row>
						<Col
							sm={6}
							className={styles["login-rememberme--container"]}
						>
							<FormCheckbox
								name='rememberMe'
								label={t('form.rememberMe')}
								register={register}
							/>
						</Col>
						<Col
							sm={6}
							className={styles["login-form__remind-password"]}
						>
							<Link
								className={styles["login-form__remind-password--link"]}
								to={url.auth.remindPassword}
							>
								{t('form.forgotPass')}
							</Link>
						</Col>
					</Row>
				</Form.Group>

				<Form.Group
					controlId='loginFormSubmit'
					className={styles["login-submit"]}
				>
					<Row className={styles["login-submit__row"]}>
						<Col className={styles["login-submit__col--createaccount"]}>
							<Link
								className={styles["login-submit__col--createaccount-link"]}
								to={url.auth.register}
							>
								{t('form.signUp')}
							</Link>
						</Col>
						<Col sm='auto'>
							<FormSubmit
								label={t('form.signIn')}
								className={styles["login-submit__button"]}
								variant='submit'
								disabled={isLoginLoading || !isValid}
							/>
						</Col>
					</Row>
				</Form.Group>
			</form>
		</div>
	);
};

export default LoginForm;
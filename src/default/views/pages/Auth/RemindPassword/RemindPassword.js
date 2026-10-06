import React, { useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { useForm } from "react-hook-form";
import { Form } from "react-bootstrap";
import { Link } from "react-router-dom";
import Container from "react-bootstrap/Container";

import api from "@/services/api";

import { alertActions } from "@/store/actions/alert.actions";
import url from "@/router/urls";
import redirectUrls from "@/router/redirectUrls";

import FormInputEmail from "@Elements/Form/FormInputEmail";
import FormSubmit from "@Elements/Form/FormSubmit";
import Alert from "@Elements/Alert/Alert";
import Button from "@Elements/Button/Button";

import styles from "@Pages/Auth/RemindPassword/RemindPassword.module.sass";

const defaultValues = {
	email: "",
};

const RemindPassword = () => {
	const dispatch = useDispatch();

	const { t } = useTranslation();

	const {
		register,
		handleSubmit,
		formState: {
			errors,
		},
	} = useForm({ defaultValues: defaultValues });

	const currentLang = useSelector(state => state.app.currentLang);
	const [isChanged, setIsChanged] = useState(false);

	const onSubmit = data => {
		data.lang = currentLang;
		data.clientRedirectUrl = redirectUrls.passwordChanged;
		api.post("/auth/reset-password", {
			...data,
		})
			.then(() => {
				setIsChanged(true);
			})
			.catch(() => {
				dispatch(
					alertActions.setAlert({
						type: "danger",
						icon: "icon-circle-warning-empty",
						text: "auth.remind.error",
						close: true,
					}),
				);
			});
	};

	return (
		<main>
			<Container>
				<div className='block block--650'>
					{!isChanged ? (
						<>
							<Alert />
							<h1 className='text--title'>{t('auth.remind.title')}</h1>
							<h2 className={styles["remind__instruction"]}>{t('auth.remind.instruction')}</h2>
							<form onSubmit={handleSubmit(onSubmit)}>
								<Form.Group controlId='email'>
									<FormInputEmail
										register={register}
										dict={null}
										errors={errors}
									/>
								</Form.Group>
								<Form.Group
									controlId='loginFormSubmit'
									className={styles["remind__submit"]}
								>
									<Link
										to={url.auth.login}
										className={styles["remind__submit--link"]}
									>
										{t('auth.remind.cancel')}
									</Link>
									<FormSubmit
										label={t('auth.remind.send')}
										variant='submit'
									/>
								</Form.Group>
							</form>
						</>
					) : (
						<>
							<h1 className='text--title'>{t('auth.remind.changeMessage')}</h1>
							<h2 className={styles["remind__instruction"]}>{t('auth.remind.changedDescription')}</h2>
							<div className={styles["remind__submit"]}>
								<Button
									variant='primary'
									href={url.home}
								>
									{t('auth.remind.backToHome')}
								</Button>
							</div>
						</>
					)}
				</div>
			</Container>
		</main>
	);
};

export default RemindPassword;

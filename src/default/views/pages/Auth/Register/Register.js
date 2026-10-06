import React, { useState, useEffect } from "react";
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';

import { Link } from "react-router-dom";
import { useForm } from 'react-hook-form';
import { Row, Col, Form, Container } from 'react-bootstrap';

import FormInputPassword from '@Elements/Form/FormInputPassword';
import FormInputPasswordRepeated from '@Elements/Form/FormInputPasswordRepeated';
import FormInputEmail from '@Elements/Form/FormInputEmail';
import FormCheckbox from "@Elements/Form/FormCheckbox";
import FormSubmit from "@Elements/Form/FormSubmit";
import Button from "@Elements/Button/Button";

import url from "@/router/urls";
import api from "@/services/api";
import redirectUrl from "@/router/redirectUrls";

import styles from '@Pages/Auth/Register/Register.module.sass';

const defaultValues = {
	email: '',
	password: '',
	repeatedPassword: '',
	acceptedRegulations: false,
};

const Register = () => {
	const { t } = useTranslation();

	const [ captchaToken, setCaptchaToken ] = useState('');
	const [ isRegisterLoading, setIsRegisterLoading ] = useState(false);
	const [ emailSent, setEmailSent ] = useState(false);

	const captchaSiteKey = useSelector((state) => state.app.config.googleReCaptchaV3SiteKey);

	const {
		register,
		handleSubmit,
		formState,
		setError,
		getValues,
	} = useForm({
		defaultValues: defaultValues,
		mode: 'onChange',
	});

	const {
		errors,
	} = formState;

	// Load reCAPTCHA v3 script when component mounts
	useEffect(() => {
		// Load the reCAPTCHA v3 script
		const loadRecaptchaScript = () => {
			const script = document.createElement('script');
			script.src = `https://www.google.com/recaptcha/api.js?render=${captchaSiteKey}`;
			script.async = true;
			script.defer = true;
			document.body.appendChild(script);
		};

		loadRecaptchaScript();

		return () => {
			// Clean up script if component unmounts
			const scripts = document.getElementsByTagName('script');
			for (let i = 0; i < scripts.length; i++) {
				if (scripts[i].src.includes('recaptcha')) {
					document.body.removeChild(scripts[i]);
				}
			}
		};
	}, []);

	// Function to execute reCAPTCHA and get token
	const executeRecaptcha = async (action) => {
		return new Promise((resolve) => {
			if (window.grecaptcha) {
				window.grecaptcha.ready(() => {
					window.grecaptcha.execute(captchaSiteKey, { action })
						.then(token => {
							setCaptchaToken(token);
							resolve(token);
						});
				});
			} else {
				// If grecaptcha isn't loaded yet, retry after a delay
				setTimeout(() => {
					executeRecaptcha(action).then(resolve);
				}, 500);
			}
		});
	};

	const onSubmit = async data => {
		setIsRegisterLoading(true);
		
		// Execute reCAPTCHA before submitting
		const token = await executeRecaptcha('register');
		
		data.captcha = token;
		data.clientRedirectURL = redirectUrl.afterRegister;

		api.post('/auth/sign-up', {
			...data,
		}).then(() => {
			setIsRegisterLoading(false);
			setEmailSent(true);

		}).catch((error) => {
			setIsRegisterLoading(false);
			for (const errorField in error.data) {
				const errorCode = error.data[errorField].split('.')[2];
				setError(errorField, {type: errorCode, message: t('form.messages.'+errorCode)});
			}
		});
	};

	return (
		<main className={ styles["register"] }>
			<section>
				<Container>
					{emailSent ? (
						<div className={ styles["register-info"] }>
							<h1 className={ styles["register-info__header"] }>{t('auth.register.emailSent.head')}</h1>
							<p className={ styles["register-info__text"] }>
								{t('auth.register.emailSent.text')}<br />
								<strong>{t('auth.register.emailSent.info')}</strong>
							</p>
							<Button
								variant='submit'
								href={ url.home }
								className={ styles["register-info__button"] }
							>
								{t('auth.register.emailSent.backToHome')}
							</Button>
						</div>
					) : (
						<div className={ styles["register-form"] }>
							<h1 className='text--title'>{t('auth.register.title')}</h1>

							<form
								onSubmit={ handleSubmit(onSubmit) }
								noValidate
								className={ styles["register-form__fields"] }
							>
								<Form.Group controlId='registerFormEmail'>
									<FormInputEmail
										register={ register }
										dict={ null }
										errors={ errors }
									/>
								</Form.Group>

								<div className={ styles["register-form__passwordtip"] }>
									<span className={ styles["register-form__text--passwordtip"] }>
										{t('form.passwordTip')}
									</span>
								</div>

								<Form.Group controlId='registerFormPassword'>
									<FormInputPassword
										register={ register }
										dict={ null }
										errors={ errors }
									/>
								</Form.Group>

								<Form.Group controlId='registerFormRepeatedPassword'>
									<FormInputPasswordRepeated
										register={ register }
										dict={ null }
										errors={ errors }
										getValues={ getValues() }
									/>
								</Form.Group>

								<Form.Group controlId='registerFormAcceptedRegulations'>
									<Row>
										<Col>
											<FormCheckbox
												name='acceptedRegulations'
												register={ register }
												validateData={ {
													required: {
														value: true,
														message:t('form.messages.private_policy'),
													},
												} }
												errorMessage={ errors.acceptedRegulations && errors.acceptedRegulations.message }
											>
												{t('auth.register.acceptedRegulations.t1')}
												<Link
													to={ url.privacyPolicy }
													target='_blank'
													rel='noopener noreferrer'
												>
													{t('auth.register.acceptedRegulations.link')}
												</Link>
												{t('auth.register.acceptedRegulations.t2')}
											</FormCheckbox>
										</Col>
									</Row>
								</Form.Group>

								<div className={ styles["register-form__info"] }>
									<span className={ styles["register-form__text--info"] }>
										{t('auth.register.info')}
									</span>
								</div>

								<Form.Group
									controlId='registerFormSubmit'
									className={ styles["register-submit"] }
								>
									<Row className={ styles["register-submit__row"] }>
										<Col className={ styles["register-submit__col--login"] }>
											<Link
												variant='primary'
												to={ url.auth.login }
											>
												{t('form.signIn')}
											</Link>
										</Col>
										<Col sm='auto'>
											<FormSubmit
												label={t('form.signUp') }
												className={ styles["register-submit__button"] }
												variant='submit'
												disabled={ isRegisterLoading || !formState.isDirty || !formState.isValid }
											/>
										</Col>
									</Row>
								</Form.Group>
							</form>
						</div>
					)}
				</Container>
			</section>
		</main>
	);
};

export default Register;
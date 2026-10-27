import React, { useEffect, useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import { reverse } from "named-urls";
import { Row, Col, Form, Container } from "react-bootstrap";
import { useForm } from "react-hook-form";

import api from "@/services/api";
import url from "@/router/urls";

import { alertActions } from "@/store/actions/alert.actions";

import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import FormInputPassword from "@Elements/Form/FormInputPassword";
import FormInputPasswordRepeated from "@Elements/Form/FormInputPasswordRepeated";
import FormSubmit from "@Elements/Form/FormSubmit";
import Button from "@Elements/Button/Button";
import Alert from "@Elements/Alert/Alert";

import styles from "@Pages/Auth/Profile/Profile.module.sass";

const MyGroupItem = (item, index, dict) => (
	<div
		key={item.name + index}
		className={styles["account__adminlist--item"]}
	>
		<div className={styles["account__adminlist--item-lp"]}>{index + 1}.</div>
		<div className={styles["account__adminlist--item-name"]}>{item.name}</div>
		<div className={styles["account__adminlist--item-button"]}>
			<Button
				variant='link'
				type='button'
				className={styles["account__button"]}
				href={reverse(url.auth.group.edit, { id: item.id })}
			>
				{dict.auth.account.groupEdit}
			</Button>
		</div>
	</div>
);

const OtherGroupItem = (item, index, dict) => (
	<li
		key={item.name + index}
		className={styles["account__list--item"]}
	>
		<div className={styles["account__list--item-lp"]}>{index + 1}.</div>
		<div className={styles["account__list--item-name"]}>{item.name}</div>
		<div className='text--gray'>
			({dict.auth.account.groupCreator}: {item.creator.email})
		</div>
	</li>
);

const Profile = () => {
	const dict = useSelector(state => state.app.dict);
	const dispatch = useDispatch();
	const accountUrl = useSelector(state => state.app.config.accountUrl);

	const UserEmail = useSelector(state => state.auth.credentials.email);
	const UserType = useSelector(state => state.auth.credentials.securityGroup);
	const UserMyGroups = useSelector(state => state.auth.credentials.myGroups);
	const UserOtherGroups = useSelector(state => state.auth.credentials.otherGroups);
	const UserHasPassword = useSelector(state => state.auth.credentials.hasPassword);
	const UserQuota = useSelector(state => state.auth.credentials.quota);
	const UserUsedSpaceQuota = useSelector(state => state.auth.credentials.usedSpaceQuota);

	const [UserPasswordForm, setUserPasswordForm] = useState(false);
	const [UserPasswordFormSubmit, setUserPasswordFormSubmit] = useState(false);

	const defaultValues = {
		currentPassword: "",
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
		setUserPasswordFormSubmit(true);
		api
			.put("/users/my-password", {
				...data,
			})
			.then(() => {
				setUserPasswordFormSubmit(false);
				setUserPasswordForm(false);

				dispatch(
					alertActions.setAlert({
						type: "success",
						icon: "icon-accept",
						text: "auth.account.passwordChangeAlert",
						close: true,
					}),
				);
			})
			.catch(error => {
				setUserPasswordFormSubmit(false);
				for (const errorField in error.data) {
					const errorCode = error.data[errorField].split(".")[2];
					setError(errorField, {type: errorCode, message: dict.form.messages[errorCode]});
				}
			});
	};

	const breadcrumbsList = [
		{ id: 1, label: "home" },
		{ id: 2, label: dict.breadcrumbs.accountMyProfile },
	];

	return (
		<main className={styles["account"]}>
			<Container>
				<Breadcrumbs breadcrumbsList={breadcrumbsList} />
				<div className='block block--650'>
					<Alert />
					<div className='block__header'>
						<h1 className='text--title'>{dict.auth.account.title}</h1>
					</div>
					<div className='block__content'>
						<div className={styles["account__info"]}>
							{UserType !== "USER" && (
								<>
									<div className={styles["account__table"]}>
										<div className={styles["account__table--row"]}>
											<div className={styles["account__table--label"]}>
												<strong>{dict.auth.account.function}:</strong>
											</div>
											<div className={styles["account__table--value-split"]}>
												<span>{dict.functionList[UserType]}</span>
											</div>
										</div>
									</div>
									<hr className='block__line block__line--m35' />
								</>
							)}
							<div className={styles["account__table"]}>
								<div className={styles["account__table--row"]}>
									<div className={styles["account__table--label"]}>
										<strong>{dict.auth.account.contactEmail}:</strong>
									</div>
									<div className={styles["account__table--value"]}>
										<span>{UserEmail}</span>
									</div>
								</div>
							</div>
							<hr className='block__line block__line--m35' />
							<div className={styles["account__table"]}>
								<div className={styles["account__table--row"]}>
									<div className={styles["account__table--label"]}>
										<strong>{dict.auth.account.email}:</strong>
									</div>
									<div className={styles["account__table--value"]}>
										<span>{UserEmail}</span>
									</div>
								</div>

								{(!UserPasswordForm && UserHasPassword) && (
									<div className={styles["account__table--row"]}>
										<div className={styles["account__table--label"]}>
											<strong>{dict.auth.account.password}:</strong>
										</div>
										<div className={styles["account__table--value"]}>
											<Button
												variant='link'
												type='button'
												className={styles["account__button"]}
												onClick={() => setUserPasswordForm(true)}
											>
												{dict.auth.account.changePassword}
											</Button>
										</div>
									</div>
								)}
							</div>
							{accountUrl && (
								<>
									<hr className='block__line block__line--m35' />
									<div className={styles["account__table"]}>
										<div className={styles["account__table--row"]}>
											<div className={styles["account__table--label"]}>
												<strong>{dict.auth.account.manageSSOAccount}:</strong>
											</div>
											<div className={styles["account__table--value"]}>
												<a href={ accountUrl } target='_blank' rel='noreferrer noopener'>{dict.auth.account.manageSSOAccountLink}</a>
												<i 
													style={ { marginLeft: "8px" } }
													data-tooltip-id='custom-tooltip' 
													data-tooltip-content={dict.auth.account.manageSSOAccountLinkTooltip} 
													className='icon-info' 
												/>
											</div>
										</div>
									</div>
								</>
							)}
						</div>

						{UserPasswordForm && (
							<>
								<div className={styles["account__password"]}>
									<hr className='block__line block__line--m35' />
									<div className={styles["account__password-form"]}>
										<h3 className='text--heading'>{dict.auth.account.passwordChange}</h3>

										<form
											onSubmit={handleSubmit(onSubmit)}
											noValidate
											className='auth-form__fields auth-form__fields--small-margin'
										>
											<Form.Group controlId='currentPassword'>
												<FormInputPassword
													register={register}
													dict={dict}
													errors={errors}
													name='currentPassword'
													novalidate
												/>
											</Form.Group>

											<Form.Group controlId='newPassword'>
												<FormInputPassword
													register={register}
													dict={dict}
													errors={errors}
													name='newPassword'
												/>
											</Form.Group>

											<div className='auth-form__passwordtip'>
												<span className='text--gray text--small'>{dict.form.passwordTip}</span>
											</div>

											<Form.Group controlId='repeatedNewPassword'>
												<FormInputPasswordRepeated
													register={register}
													dict={dict}
													errors={errors}
													getValues={getValues()}
													name='repeatedNewPassword'
												/>
											</Form.Group>

											<Form.Group
												controlId='registerFormSubmit'
												className='auth-submit'
											>
												<Row className='auth-submit__row'>
													<Col className='auth-submit__col--tobutton'>
														<Button
															variant='link'
															type='button'
															onClick={() => setUserPasswordForm(false)}
														>
															{dict.form.cancel}
														</Button>
													</Col>
													<Col sm='auto'>
														<FormSubmit
															label={dict.form.change}
															className='auth-submit__button'
															variant='submit'
															disabled={
																UserPasswordFormSubmit ||
																!formState.isDirty ||
																!formState.isValid
															}
														/>
													</Col>
												</Row>
											</Form.Group>
										</form>
									</div>
								</div>
							</>
						)}
						<div className={styles["account__table--row"]}>
							<div className={styles["account__table--label"]}>
								<strong>{dict.auth.account.diskSpace}:</strong>
							</div>
							<div className={styles["account__table--value"]}>
								<span>{UserUsedSpaceQuota !== null ? UserUsedSpaceQuota : 0} {dict.auth.account.mb} / </span>
								<span className={styles["account__table--additional-text"]}>{UserQuota} {dict.auth.account.mb}</span>
							</div>
						</div>
						<div className={styles["account__groups"]}>
							<div className={styles["account__groups--title"]}>
								<h2 className='text--header'>{dict.auth.account.myGroup}</h2>
								<Button
									variant='link'
									type='button'
									className={styles["account__button"]}
									href={url.auth.group.new}
								>
									{dict.auth.account.createNewGroup}
								</Button>
							</div>
							{UserMyGroups.length > 0 && (
								<div className={styles["account__groups--admin"]}>
									<h3 className='text--heading'>{dict.auth.account.groupAdministrator}:</h3>
									<div className={styles["account__adminlist"]}>
										{UserMyGroups.map((group, index) => MyGroupItem(group, index, dict))}
									</div>
								</div>
							)}
							{UserOtherGroups.length > 0 && (
								<div className={styles["account__groups--rest"]}>
									<h3 className='text--heading'>{dict.auth.account.groupRest}:</h3>
									<ul className={styles["account__list"]}>
										{UserOtherGroups.map((group, index) => OtherGroupItem(group, index, dict))}
									</ul>
								</div>
							)}
							{!(UserMyGroups.length > 0 || UserOtherGroups.length > 0) && (
								<div className={styles["account__groups--blank"]}>{dict.auth.account.groupBlank}</div>
							)}
						</div>
					</div>
				</div>
			</Container>
		</main>
	);
};

export default Profile;
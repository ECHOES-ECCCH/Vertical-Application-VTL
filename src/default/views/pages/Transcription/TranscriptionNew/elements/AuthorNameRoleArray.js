import React from 'react';
import { useTranslation } from 'react-i18next';
import { useFieldArray, useFormContext, useWatch } from "react-hook-form";
import PropTypes from "prop-types";

import Button from "@Elements/Button/Button";
import FormInputCustom from "@Elements/Form/FormInputCustom";
import RoleDropdown from '@Pages/Transcription/TranscriptionNew/elements/RoleDropdown';

const AuthorNameRoleArray = ({ linkedObject, roleOptions, isDisabled }) => {
	const { t } = useTranslation();

	const {
		register,
		control,

		formState: {
			errors,
		},
	} = useFormContext();
	const { fields, append, remove } = useFieldArray({
		control,
		name: `${linkedObject}`,
	});

	const actors = useWatch({control, name: linkedObject});
	const anyActorEmptyName = actors.some((item) => (item.name.length < 1));

	const onDeleteClick = (index) => {
		remove(index);
	};
	const addActor = () => {
		append({ name: "", role: '' });
	};

	return (
		<div className='transcription-new__form--inline-list'>
			{fields.map((field, index) => (
				<div
					className='transcription-new__form--inline-row'
					key={field.id}
				>
					<RoleDropdown
						roleOptions={roleOptions}
						fieldName={`${linkedObject}[${index}].role`}
						isDisabled={isDisabled}
					/>
					<div className='transcription-new__form--inline-full'>
						<FormInputCustom
							register={register}
							label={t('transcription.form.firstAndLastName')}
							name={`${linkedObject}[${index}].name`}
							errors={errors}
							placeholder={t('transcription.form.firstAndLastNamePlaceholder')}
							isDisabled={isDisabled}
							onDeleteFunction={(actors.length > 1) ? () => onDeleteClick(index) : null}
						/>
					</div>
				</div>
			))}
			{(!anyActorEmptyName && !isDisabled) &&
				(
					<Button
						variant='link'
						className='transcription-new__btn--add'
						onClick={() => {
							addActor();
						}}
					>
						<i className='icon-plus2' />{t('transcription.form.appendAnotherPerson')}
					</Button>
				)}
		</div>
	);
};

AuthorNameRoleArray.propTypes = {
	linkedObject: PropTypes.string,
	isDisabled: PropTypes.bool,
	roleOptions: PropTypes.array,
};

export default AuthorNameRoleArray;
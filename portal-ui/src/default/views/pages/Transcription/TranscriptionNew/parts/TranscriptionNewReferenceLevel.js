import React, { useState, useEffect } from "react";
import { useTranslation } from 'react-i18next';
import { useFieldArray, useFormContext } from "react-hook-form";
import PropTypes from "prop-types";

import FormInputCustom from "@Elements/Form/FormInputCustom";
import Button from "@Elements/Button/Button";

const TranscriptionNewReferenceLevel = ({ isDisabled }) => {
	const {
		register,
		control,
		watch,
		trigger,
		getValues,
		formState: {
			errors,
		},
	} = useFormContext();
	const { t } = useTranslation();

	const { fields, append, remove } = useFieldArray(
		{
			control,
			name: "profileDesc.links",
		},
	);
	const [emptyLinks, setEmptyLinks] = useState(true);
	const [showDelete, setShowDelete] = useState(false);
	const linksNumberWatch = watch("profileDesc.links");

	const checkLinks = (currentLinks) => {
		setShowDelete(currentLinks.length > 1);
		setEmptyLinks(currentLinks.some(it => it.name === ""));
	};

	useEffect(() => {
		checkLinks(linksNumberWatch);
	}, [linksNumberWatch]);

	return (
		<div className='transcription-new__level'>
			<h3 className='text--level-title'>
				{t('transcription.new.reference')}
			</h3>

			<div className='transcription-new__form--row'>
				<div className='transcription-new__form--links'>
					{fields.map((item, index) => {
						return (
							<FormInputCustom
								name={`profileDesc.links[${index}].name`}
								register={register}
								label={t('transcription.form.link')}
								onDeleteFunction={ (showDelete) ? () => remove(index) : null}
								key={item.id}
								isDisabled={isDisabled}
								validateData={{
									pattern: { // eslint-disable-line
										value: /^(?:(?:(?:https?|ftp):)?\/\/)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})))(?::\d{2,5})?(?:[/?#]\S*)?$/, // eslint-disable-line
										message: t('form.messages.invalidUrl'),
									},
								}}
								onBlur={async () => {
									await trigger(`profileDesc.links[${index}]`);
									checkLinks(getValues('profileDesc.links'));
								}}
								errorMessage={`${(errors && errors.profileDesc && errors.profileDesc.links && errors.profileDesc.links[index]) ? errors.profileDesc.links[index].name.message : ''}`}
							/>
						);
					})}
				</div>

				{(!emptyLinks && !isDisabled) &&
					<Button
						variant='link'
						className='transcription-new__btn--add'
						onClick={() => {
							append({ name: "" });
						}}
					>
						<i className='icon-plus2' />{t('transcription.form.appendAnotherLink')}
					</Button>}
			</div>
		</div>
	);
};

TranscriptionNewReferenceLevel.propTypes = {
	isDisabled: PropTypes.bool,
};

export default TranscriptionNewReferenceLevel;
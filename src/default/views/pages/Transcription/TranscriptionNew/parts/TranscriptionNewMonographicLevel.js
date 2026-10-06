import React from "react";
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";
import { useFormContext } from "react-hook-form";

import FormInput from "@Elements/Form/FormInput";
import FormInputCustom from "@Elements/Form/FormInputCustom";

import CommonDropdownForm from '@Elements/Dropdown/CommonDropdownForm';

import AuthorNameRoleArray from '@Pages/Transcription/TranscriptionNew/elements/AuthorNameRoleArray';

const TranscriptionNewMonographicLevel = ({ languageOptions, roleOptions, biblTypes, isDisabled }) => {
	const {
		register,
		watch,
		formState: {
			errors,
		},
	} = useFormContext();
	const { t } = useTranslation();

	const secondFieldValue = watch('analythicalInfo.title');

	return (
		<div className='transcription-new__level'>
			<h3 className='text--level-title'>
				{t('transcription.details.monographicLevel')}
			</h3>
			<span className='text--level-desc'>
				{t('transcription.details.monographicLevelDescription')}
			</span>

			<div className='transcription-new__form--row'>
				<FormInput
					name='monographicInfo.title'
					register={register}
					label={t('transcription.form.title')}
					isDisabled={isDisabled}
					placeholder={t('transcription.form.titlePlaceholder')}
					validateData={
						{
							validate: value => value !== '' || secondFieldValue !== '' || t('form.messages.minumumOnetitle'),
						}
					}
					errorMessage={errors.monographicInfo && errors.monographicInfo.title && errors.monographicInfo.title.message ? errors.monographicInfo.title.message : ''}
				/>
			</div>

			<div className='transcription-new__form--inline'>
				<div className='transcription-new__form--inline-select'>
					<CommonDropdownForm
						label={t('transcription.form.type')}
						placeholder={t('transcription.form.typeSelect')}
						fieldName='monographicInfo.biblType'
						options={biblTypes}
						isDisabled={isDisabled}
					/>
				</div>

				<div className='transcription-new__form--inline-full'>
					<FormInputCustom
						register={register}
						label={t('transcription.form.signature')}
						name='monographicInfo.signature'
						isDisabled={isDisabled}
					/>
				</div>
			</div>

			<div className='transcription-new__form--inline-list'>
				<AuthorNameRoleArray
					linkedObject='monographicInfo.actors'
					isDisabled={isDisabled}
					roleOptions={roleOptions}
				/>
			</div>

			<div className='transcription-new__form--inline-row'>
				<div className='transcription-new__form--inline-select'>				
					<CommonDropdownForm
						label={t('transcription.form.language')}
						placeholder={t('transcription.form.languageSelect')}
						fieldName='monographicInfo.language'
						options={languageOptions}
						isDisabled={isDisabled}
					/>
				</div>
			</div>

			<div className='transcription-new__form--inline'>
				<FormInputCustom
					register={register}
					label={t('transcription.form.chapterNumber')}
					name='monographicInfo.chapterNo'
					inputContainerClassName='transcription-new__form--inline-small'
					type='number'
					isDisabled={isDisabled}
				/>

				<FormInputCustom
					register={register}
					label={t('transcription.form.volumeNumber')}
					name='monographicInfo.volumeNo'
					inputContainerClassName='transcription-new__form--inline-small'
					type='number'
					isDisabled={isDisabled}
				/>

				<FormInputCustom
					register={register}
					label={t('transcription.form.notebookNumber')}
					name='monographicInfo.issueNo'
					inputContainerClassName='transcription-new__form--inline-small'
					type='number'
					isDisabled={isDisabled}
				/>
			</div>

			<div className='transcription-new__form--inline'>
				<FormInputCustom
					register={register}
					label={t('transcription.form.pagesRange')}
					name='monographicInfo.pageRange'
					placeholder={t('transcription.form.pagesRangePlaceholder')}
					inputContainerClassName='transcription-new__form--inline-small'
					isDisabled={isDisabled}
				/>

				<FormInputCustom
					register={register}
					label={t('transcription.form.pagesNumber')}
					name='monographicInfo.pages'
					type='number'
					inputContainerClassName='transcription-new__form--inline-small'
					isDisabled={isDisabled}
				/>
			</div>

			<div className='transcription-new__form--row'>
				<FormInputCustom
					register={register}
					label={t('transcription.form.publisher')}
					name='monographicInfo.publisher'
					isDisabled={isDisabled}
				/>
			</div>

			<div className='transcription-new__form--inline'>
				<FormInputCustom
					register={register}
					label={t('transcription.form.publicationPlace')}
					name='monographicInfo.publicationPlace'
					isDisabled={isDisabled}
				/>
			</div>

			<div className='transcription-new__form--row'>
				<FormInputCustom
					register={register}
					label={t('transcription.form.publicationDate')}
					name='monographicInfo.publicationDate'
					placeholder={t('transcription.form.publicationDatePlaceholder')}
					inputContainerClassName='transcription-new__form--inline-medium'
					isDisabled={isDisabled}
				/>
			</div>

			<div className='transcription-new__form--inline'>
				<FormInputCustom
					register={register}
					label={t('transcription.form.isbn')}
					name='monographicInfo.isbn'
					inputContainerClassName='transcription-new__form--inline-medium'
					isDisabled={isDisabled}
				/>

				<FormInputCustom
					register={register}
					label={t('transcription.form.eisbn')}
					name='monographicInfo.eisbn'
					inputContainerClassName='transcription-new__form--inline-medium'
					isDisabled={isDisabled}
				/>
			</div>

			<div className='transcription-new__form--inline'>
				<FormInputCustom
					register={register}
					label={t('transcription.form.issn')}
					name='monographicInfo.issn'
					inputContainerClassName='transcription-new__form--inline-medium'
					isDisabled={isDisabled}
				/>

				<FormInputCustom
					register={register}
					label={t('transcription.form.eissn')}
					name='monographicInfo.eissn'
					inputContainerClassName='transcription-new__form--inline-medium'
					isDisabled={isDisabled}
				/>
			</div>
		</div>
	);
};

TranscriptionNewMonographicLevel.propTypes = {
	languageOptions: PropTypes.array,
	roleOptions: PropTypes.array,
	biblTypes: PropTypes.array,
	isDisabled: PropTypes.bool,
};

export default TranscriptionNewMonographicLevel;
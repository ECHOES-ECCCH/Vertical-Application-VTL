import React, { useEffect } from 'react';
import PropTypes from "prop-types";
import { useTranslation } from 'react-i18next';
import { useForm, FormProvider } from 'react-hook-form';
import { useHistory } from 'react-router';

import Button from "@Elements/Button/Button";
import Frame from "@Elements/Frame/Frame";
import FormCheckbox from "@Elements/Form/FormCheckbox";
import FormSubmit from "@Elements/Form/FormSubmit";

import { useShowModal } from '@Elements/Modal/ModalHooks';

import TranscriptionNewAnalyticalLevel from '@Pages/Transcription/TranscriptionNew/parts/TranscriptionNewAnalyticalLevel';
import TranscriptionNewMonographicLevel from '@Pages/Transcription/TranscriptionNew/parts/TranscriptionNewMonographicLevel';
import TranscriptionNewTextProfileLevel from '@Pages/Transcription/TranscriptionNew/parts/TranscriptionNewTextProfileLevel';
import TranscriptionNewReferenceLevel from '@Pages/Transcription/TranscriptionNew/parts/TranscriptionNewReferenceLevel';
import TranscriptionNewIdentifierLevel from '@Pages/Transcription/TranscriptionNew/parts/TranscriptionNewIdentifierLevel';

const TranscriptionNewForm = ({ transcriptionId, defaultValues, isDisabled, roleOptions, biblTypes, languageOptions, onExportData, langs, isImported, forcePublish }) => {
	const { t } = useTranslation();

	const history = useHistory();

	const formMethods = useForm(
		{
			mode: 'onSubmit',
			reValidateMode: 'onChange',
			defaultValues,
		},
	);

	const { handleSubmit, register, formState: { errors }, getValues } = formMethods;

	const parseDropdownSelected = (dataSource) => {
		if (dataSource && dataSource !== 'none') {
			return ({code: dataSource});
		} else {
			return null;
		}
	};

	const onSubmit = data => {
		data.profileDesc.keywords = data.profileDesc.keywords.split(/[;]+/).map((item) => item.replace(/^\s+|\s+$/g, ""));

		if (Object.keys(data.profileDesc.links).length) {
			data.profileDesc.links = data.profileDesc.links.map((item) => item.name).filter((link) => link.length > 0);
		}

		data.analyticInfo.language = parseDropdownSelected(data.analyticInfo.language);
		data.analyticInfo.actors = data.analyticInfo.actors.filter(item => item.name.length > 0);

		data.monographicInfo.language = parseDropdownSelected(data.monographicInfo.language);
		data.monographicInfo.biblType = parseDropdownSelected(data.monographicInfo.biblType);

		onExportData(data);
	};

	const modalTerms = useShowModal({
		title: t('modals.regulations.title'),
		name: 'regulations',
		componentPath: './parts/ModalRegulations',
	});

	return (
		<form
			className='transcription-new__form'
			onSubmit={handleSubmit(onSubmit)}
		>
			<FormProvider {...formMethods}>
				<TranscriptionNewAnalyticalLevel
					isDisabled={isDisabled}
					roleOptions={roleOptions}
					languageOptions={languageOptions}
					defaultValues={defaultValues}
					langs={langs}
				/>
				<TranscriptionNewMonographicLevel
					isDisabled={isDisabled}
					roleOptions={roleOptions}
					languageOptions={languageOptions}
					biblTypes={biblTypes}
					defaultValues={defaultValues}
					langs={langs}
				/>
				<TranscriptionNewTextProfileLevel
					isDisabled={isDisabled}
				/>
				<TranscriptionNewReferenceLevel
					isDisabled={isDisabled}
				/>

				{isImported &&
					<TranscriptionNewIdentifierLevel
						isDisabled={isDisabled}
						defaultValues={defaultValues}
					/>}
			</FormProvider>
			{(!transcriptionId || forcePublish) &&
				<Frame
					title={t('transcription.new.frameTitle')}
					text={forcePublish ? t('transcription.new.frameTextPublish') : t('transcription.new.frameText')}
				/>}

			{!transcriptionId && (
				<div className='transcription-new__form--terms'>
					<div style={{ "marginBottom": "20px" }}>{t('transcription.new.terms')}
						<Button
							variant='link'
							onClick={() => modalTerms()}
						>{t('transcription.new.termsRegLink')}
						</Button>
						{t('transcription.new.termsPart2')}.
					</div>
					<FormCheckbox
						name='acceptedRegulations'
						register={register}
						validateData={{
							required: {
								value: true,
								message: t('form.messages.notAcceptedRegulations'),
							},
						}}
						errorMessage={errors && errors.acceptedRegulations && errors.acceptedRegulations.message}
					>
						{t('transcription.new.termsNew1')}
						<Button
							variant='link'
							onClick={() => modalTerms()}
						>{t('transcription.new.termsLink')}
						</Button>
						{t('transcription.new.termsNew2')}
						{" " + t('transcription.new.termsNew3')}
						{t('transcription.metadata.fieldTitleWithRequiredInfo.required')}
					</FormCheckbox>

				</div>
			)}

			<p className='transcription-new__required-info'><span className='transcription-new__form-required'> *</span> {t('form.requiredInfo')}</p>

			<div className='transcription-new__form--btns'>
				<Button
					variant='secondary'
					className='transcription-new__form--cancel'
					onClick={() => history.goBack()}
				>{t('form.cancel')}
				</Button>
				<FormSubmit
					label={transcriptionId ? (forcePublish ? t('form.publishTranscription') : t('form.saveTranscription'))
						: (isImported ? t('form.createImportedTranscription') : t('form.createNewTranscription'))}
					className='transcription-new__form--submit'
					variant='submit'
				/>
			</div>
		</form>
	);
};
TranscriptionNewForm.propTypes = {
	transcriptionId: PropTypes.number,
	defaultValues: PropTypes.object,
	isDisabled: PropTypes.bool,
	roleOptions: PropTypes.array,
	biblTypes: PropTypes.array,
	languageOptions: PropTypes.array,
	onExportData: PropTypes.func,
	setErrorCode: PropTypes.string,
	langs: PropTypes.array,
	isImported: PropTypes.bool,
	forcePublish: PropTypes.bool,
};

export default TranscriptionNewForm;

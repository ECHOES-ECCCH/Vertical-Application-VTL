import React from "react";
import { useTranslation } from 'react-i18next';
import { useFormContext, useForm } from "react-hook-form";

import PropTypes from "prop-types";

import FormInput from "@Elements/Form/FormInput";

import RichTextEditor from '@Pages/Transcription/TranscriptionRichTextEditor/RichTextEditor';

const TranscriptionNewTranscriptionInfoLevel = ({ isDisabled, description, setDescription, documentSource, setDocumentSource, documentOrigin, setDocumentOrigin, projectColaboratorInstruction, setProjectColaboratorInstruction, isPublic }) => {
	const {
		register,

		formState: {
			errors,
		},
    } = useFormContext();  // eslint-disable-line
	const { control } = useForm();  // eslint-disable-line
	const { t } = useTranslation();

	const isValidUrl = urlString=> {
		try {
			return Boolean(new URL(urlString)).valueOf();
		}
		catch(e){
			return false;
		}
	};

	return (
		<div className='transcription-new__level'>
			<h2 className='transcription-new__sectionTitle'>
				{t('transcription.new.section1')}
			</h2>
			<section className='transcription-new__form--richText transcription-new__form--row'>
				<p className='transcription-new__form--richText-label'>
					{t('transcription.metadata.fieldTitleWithRequiredInfo.projectDescription') } { isPublic && <span className='asterisk'>*</span> }
				</p>
				<RichTextEditor
					elementValue={description}
					setElementValue={setDescription}
					disabled={ isDisabled }
				/>
				<FormInput 
					name='transcriptionInfo.descriptionInputHidden'
					register={ register }
					isDisabled={ isDisabled }
					errorMessage={ errors.transcriptionInfo && errors.transcriptionInfo.descriptionInputHidden && errors.transcriptionInfo.descriptionInputHidden.message ? errors.transcriptionInfo.descriptionInputHidden.message : '' }
					type='RichTextHidden'
					validateData={
						isPublic ?
							({
								validate: value => (value !== '' && value !== "<p></p>" ) ||t('form.messages.requiredDescription'),
							}):
							({
								validate: null,
							})
					}
				/>
			</section>
			<div className='transcription-new__form--row'>
				<FormInput
					name='profileDesc.keywords'
					register={ register }
					label={ `${t('transcription.metadata.fieldTitleWithRequiredInfo.tags')}` }
					sublabel={ `${t('transcription.metadata.fieldTitleWithRequiredInfo.tagsSublabel')}` }
					placeholder={t('transcription.metadata.fieldPlaceholder.tags') }
					isDisabled={ isDisabled }
					validateData={
						isPublic ?
							({
								validate: value => value !== '' ||t('form.messages.requiredKeywords'),
							}):
							({
								validate: null,
							})
					}
					errorMessage={ errors.profileDesc && errors.profileDesc.keywords && errors.profileDesc.keywords.message ? errors.profileDesc.keywords.message : '' }
					isRequired={ isPublic }
				/>
			</div>
			<h2 className='transcription-new__sectionTitle'>
				{t('transcription.new.section2_1')}
			</h2>
			<div className='transcription-new__form--row'>				
				<FormInput
					name='transcriptionInfo.releaseDate'
					register={ register }
					label={ `${t('transcription.metadata.fieldTitleWithRequiredInfo.releaseDate')}` }
					placeholder={t('transcription.metadata.fieldPlaceholder.releaseDate') }
					isDisabled={ isDisabled }
					type='text'
					validateData={
						isPublic ?
							({
								validate: value => value !== '' ||t('form.messages.requiredReleaseDate'),
							}):
							({
								validate: null,
							})
					}
					errorMessage={ errors.transcriptionInfo && errors.transcriptionInfo.releaseDate && errors.transcriptionInfo.releaseDate.message ? errors.transcriptionInfo.releaseDate.message : '' }
					isRequired={ isPublic }
				/>
			</div>

			<div className='transcription-new__form--row'>
				<FormInput
					name='transcriptionInfo.documentTypes'
					register={ register }
					label={ `${t('transcription.metadata.fieldTitleWithRequiredInfo.sourceFileTypes')}` }
					placeholder={t('transcription.metadata.fieldPlaceholder.sourceFileTypes') }
					isDisabled={ isDisabled }
					validateData={
						isPublic ?
							({
								validate: value => value !== '' ||t('form.messages.requiredSourceFileTypes'),
							}):
							({
								validate: null,
							})
					}
					errorMessage={ errors.transcriptionInfo && errors.transcriptionInfo.documentTypes && errors.transcriptionInfo.documentTypes.message ? errors.transcriptionInfo.documentTypes.message : '' }
					isRequired={ isPublic }
				/>
			</div>

			<div className='transcription-new__form--row'>
				<label>{t('transcription.metadata.fieldTitleWithRequiredInfo.sourceFileInfo')} {isPublic && <span className='asterisk'>*</span>}</label>
				<div className='transcription-new__form--radio-block'>
					<input
						type='radio' 
						id='source-www-website' 
						value='www-website' 
						name='documentSource'
						disabled={ isDisabled }
						checked={documentSource == 'www-website'} 
						onChange={(e) => {setDocumentSource(e.target.value);}}
					/>
					<label className='transcription-new__form--radio-label' htmlFor='source-www-website'>
						{t('transcription.metadata.fieldTitle.sourceFileTypeWebsite')}
					</label>
					<div className='transcription-new__form--radio-input'>
						<FormInput 
							name='transcriptionInfo.documentSrc'
							className='transcription-new__form--radio-input'
							register={ register }
							label={ `${t('transcription.metadata.fieldTitleWithRequiredInfo.pasteURLAddress')}` }
							isDisabled={ isDisabled || documentSource !== 'www-website' }
							errorMessage={ errors.transcriptionInfo?.documentsrcInputHidden?.message || errors.transcriptionInfo?.documentSrc?.message || '' }
							validateData={
								isPublic ?
									(documentSource !== 'www-website'? {
										validate: value => value !== '' || documentSource == 'internal-collection' ||t('form.messages.requiredDocumentSrc'),
									} : {
										validate: value => isValidUrl(value) || t('default:form.messages.malformedURL'),
									}):
									(documentSource !== 'www-website'?{
										validate: null,
									}: {
										validate: value => isValidUrl(value) || t('default:form.messages.malformedURL'),
									})
							}
						/>
					</div>
				</div>
				<div className='transcription-new__form--radio-block'>
					<input
						type='radio' 
						id='source-internal-collection' 
						value='internal-collection' 
						name='documentSource'
						disabled={ isDisabled }
						checked={documentSource == 'internal-collection'} 
						onChange={(e) => { setDocumentSource(e.target.value); }}
					/>
					<label className='transcription-new__form--radio-label' htmlFor='source-internal-collection'>
						{t('transcription.metadata.fieldTitle.sourceFileTypeInternal')}
					</label> 
				</div>
				
			</div>

			<section className='transcription-new__form--richText transcription-new__form--row'>
				<p className='transcription-new__form--richText-label'>{t('transcription.metadata.fieldTitleWithRequiredInfo.additionalInformation') }</p>
				<RichTextEditor
					elementValue={documentOrigin}
					setElementValue={setDocumentOrigin}
					disabled={ isDisabled }
				/>
				<FormInput 
					name='transcriptionInfo.documentOriginInputHidden'
					register={ register }
					isDisabled={ isDisabled }
					errorMessage={ errors.transcriptionInfo && errors.transcriptionInfo.documentOriginInputHidden && errors.transcriptionInfo.documentOriginInputHidden.message ? errors.transcriptionInfo.documentOriginInputHidden.message : '' }
					type='RichTextHidden'
				/>
			</section>
		
			<h2 className='transcription-new__sectionTitle2'>
				{t('transcription.new.section3')}
			</h2>
			<section className='transcription-new__form--richText transcription-new__form--row'>
				<p className='transcription-new__form--richText-label'>{t('transcription.metadata.fieldTitleWithRequiredInfo.projectColaboratorInstruction') }</p>
				<RichTextEditor
					elementValue={projectColaboratorInstruction}
					setElementValue={setProjectColaboratorInstruction}
					disabled={ isDisabled }
				/>

			</section>			

			<h2 className='transcription-new__sectionTitle'>
				{t('transcription.new.section4')}
			</h2>
			<div className='transcription-new__form--row'>
				<FormInput
					name='transcriptionInfo.collectionName'
					register={ register }
					label={t('transcription.metadata.fieldTitleWithRequiredInfo.projectCollectionName') }
					isDisabled={ isDisabled }
					errorMessage={ errors.transcriptionInfo && errors.transcriptionInfo.collectionName && errors.transcriptionInfo.collectionName.message ? errors.transcriptionInfo.collectionName.message : '' }
				/>
			</div>
			<div className='transcription-new__form--row'>
				<FormInput
					name='transcriptionInfo.contactName'
					register={ register }
					label={t('transcription.metadata.fieldTitleWithRequiredInfo.projectCoordinatorContact') }
					placeholder={t('transcription.metadata.fieldPlaceholder.projectCoordinatorContact') }
					isDisabled={ isDisabled }
					errorMessage={ errors.transcriptionInfo && errors.transcriptionInfo.contactName && errors.transcriptionInfo.contactName.message ? errors.transcriptionInfo.contactName.message : '' }
				/>
			</div>

		</div>
	);
};

TranscriptionNewTranscriptionInfoLevel.propTypes = {
	isDisabled: PropTypes.bool,
	description: PropTypes.string,
	setDescription: PropTypes.func, 
	documentSource: PropTypes.string, 
	setDocumentSource: PropTypes.func, 
	documentOrigin: PropTypes.string, 
	setDocumentOrigin: PropTypes.func, 
	projectColaboratorInstruction: PropTypes.string, 
	setProjectColaboratorInstruction: PropTypes.func, 
	isPublic: PropTypes.bool,
};

export default TranscriptionNewTranscriptionInfoLevel;
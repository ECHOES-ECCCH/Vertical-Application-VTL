import React, { useState } from "react";
import { useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { useHistory } from "react-router-dom";
import { Modal } from "react-bootstrap";
import PropTypes from "prop-types";
import { useForm } from "react-hook-form";

import api from "@/services/api";
import { appActions } from "@/store/actions/app.actions";

import FormSubmit from "@Elements/Form/FormSubmit";
import FormInputFile from "@Elements/Form/FormInputFile";
import FormInput from "@Elements/Form/FormInput";
import Button from "@Elements/Button/Button";
import Alert from "@Elements/Alert/Alert";

import CommonDropdown from '@Elements/Dropdown/CommonDropdown';
import Frame from "@Elements/Frame/Frame";
import { alertActions } from "@/store/actions/alert.actions";

const ModalAddPages = ({ handleOnClose, transcriptionId }) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();
	const history = useHistory();

	const [ selectedOption, setSelectedOption ] = useState(0);

	const options = [
		{ value: 'IIIF', label: t('form.importSelect.importIIIF') },
		{ value: 'LOCAL', label: t('form.importSelect.importLocal') },
	];

	const { register, handleSubmit, formState: { errors, isValid }, setError, watch } = useForm({
		defaultValues: {
			importLink: "",
		},
		mode: "onChange",
	});

	const inputFile = watch('file');
	const formats = [
		'.zip',
		'.tiff',
		'.tif',
		'.pdf',
		'.jpeg',
		'.jpg',
		'.djv',
		'.djvu',
		'.png',
	];

	const sendFiles = (data) => {
		const formData = new FormData();
		formData.append('file', data.file[0]);
		dispatch(appActions.setLoading(true));
		api.post(`transcriptions/${transcriptionId}/add-pages`, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
			.then(() => {
				history.go(0);
				handleOnClose();
				dispatch(appActions.setLoading(false));
			}).catch((response) => {
				dispatch(appActions.setLoading(false));
				dispatch(
					alertActions.setAlert({
						type: "danger",
						icon: "icon-circle-warning-empty",
						text: "form.messages.fileCouldNotBeAdded",
						close: true,
						target: 'addPageBox',
					}),
				);
				if (response && response.data && response.data.code) {
					switch (response.data.code) {
						case 'pages.error.wrongFileType': {
							const files = response.data.params;
							const message = files.length > 1 ? `${t('form.messages.wrongFilesType')}: ${files.join(', ')}` : `${t('form.messages.wrongFileType')}: ${files[0]}`;
							setError('file', {type: 'wrongFilesType', message});
							break;
						}
						case 'pages.error.fileAlreadyExists': {
							const files = response.data.params;
							const message = files.length > 1 ? `${t('form.messages.filesAlreadyExist')}: ${files.join(', ')}` : `${t('form.messages.fileAlreadyExists')}: ${files[0]}`;
							setError('file', {type: 'fileAlreadyExists', message});
							break;
						}
						case 'pages.error.fileTooLarge': {
							dispatch(
								alertActions.setAlert({
									type: "danger",
									icon: "icon-circle-warning-empty",
									text: "form.messages.fileTooLarge",
									close: true,
									target: 'addPageBox',
								}),
							);
							break;
						}
						case 'quota.error.user.exceeded': {
							dispatch(
								alertActions.setAlert({
									type: "danger",
									icon: "icon-circle-warning-empty",
									text: "form.messages.quotaExceeded",
									close: true,
									target: 'addPageBox',
								}),
							);
							break;
						}
						case 'quota.error.translation.exceeded': {
							dispatch(
								alertActions.setAlert({
									type: "danger",
									icon: "icon-circle-warning-empty",
									text: "form.messages.quotaTranscriptionExceeded",
									close: true,
									target: 'addPageBox',
								}),
							);
							break;
						}
						case 'pages.error.iiif-tools': {
							dispatch(
								alertActions.setAlert({
									type: "danger",
									icon: "icon-circle-warning-empty",
									text: "form.messages.fileCannotBeProcessed",
									close: true,
									target: 'addPageBox',
								}),
							);
							break;
						}
						default: {
							dispatch(
								alertActions.setAlert({
									type: "danger",
									icon: "icon-circle-warning-empty",
									text: response.data.code,
									close: true,
									target: 'addPageBox',
								}),
							);
						}
					}
				}
			});
	};

	const importIIIF = (url) => {
		dispatch(appActions.setLoading(true));
		api.post(`transcription/${transcriptionId}/import-iiif?resourceURL=${url}`)
			.then(() => {
				history.go(0);
				handleOnClose();
				dispatch(appActions.setLoading(false));
			})
			.catch((response) => {
				dispatch(appActions.setLoading(false));
				dispatch(
					alertActions.setAlert({
						type: "danger",
						icon: "icon-circle-warning-empty",
						text: "form.messages.fileCouldNotBeAdded",
						close: true,
						target: 'addPageBox',
					}),
				);
				if (response && response.data && response.data.resourceUrl) {
					switch (response.data.resourceUrl) {
						case 'imports.error.url.not-found': {
							setError('importLink', {type: 'error', message: t('transcription.import.errors.' + response.data.resourceUrl.replace(/[.-]/g, ""))});
							break;
						}
						case 'imports.text.format.link-less': {
							setError('importLink', {type: 'error', message: t('transcription.import.errors.' + response.data.resourceUrl.replace(/[.-]/g, ""))});
							break;
						}
						default: {
							dispatch(
								alertActions.setAlert({
									type: "danger",
									icon: "icon-circle-warning-empty",
									text: response.data.code,
									close: true,
									target: 'addPageBox',
								}),
							);
						}
					}
				}
			});

	};

	const onSubmit = data => {
		if (data.importLink) {
			importIIIF(data.importLink);
		}
		else if (data.file) {
			sendFiles(data);
		}

	};

	const errorMessage = () => {
		return errors.file.type === 'wrongFileType' ? t('form.messages.wrongFileType') : errors.file.message;
	};

	return (
		<>
			<Modal.Body>
				<Alert alertBoxName='addPageBox' />
				<p style={{ fontSize: "0.9375rem" }}>
					{t('transcription.addPages.description')}
				</p>
				<CommonDropdown
					options={
						options.map(it => it.label)
					}
					selectedOption={selectedOption}
					setSelectedOption={setSelectedOption}
					className='dropdown-new'
				/>

				<form
					style={{ "marginTop": "28px" }}
					className='block__form'
					onSubmit={handleSubmit(onSubmit)}
				>
					<div className='block__form--row'>
						{selectedOption === 0 ?
							<FormInput
								name='importLink'
								label={t('form.inputIIIF.label')}
								placeholder={t('form.inputIIIF.placeholder')}
								dict={null}
								register={register}
								validateData={{
									required: {
										value: true,
										message: t('form.messages.emptyUrl'),
									},
									pattern: { // eslint-disable-line
										value: /^(?:(?:(?:https?|ftp):)?\/\/)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})))(?::\d{2,5})?(?:[/?#]\S*)?$/, // eslint-disable-line
										message: t('form.messages.invalidUrl'),
									},
								}}
								type='text'
								errorMessage={errors.importLink ? errors.importLink.message : ''}
							/>
							:
							<FormInputFile
								dict={null}
								register={register}
								errorMessage={errors.file && errorMessage()}
								formats={formats}
								chooseFile={inputFile && Object.values(inputFile).map((value) => { return value.name; }).toString()}
							/>}
					</div>
					<Frame
						text={`${t('modals.addPages.quotaInfo')}`}
					/>
					<div className='block__form--row block__form--buttons'>
						<Button
							variant='secondary'
							onClick={handleOnClose}
							className='block__button--cancel'
						>
							{t('form.cancel')}
						</Button>
						<FormSubmit
							label={t('form.inputFile.startSendingFile')}
							className='block__button--submit'
							variant='submit'
							disabled={selectedOption === 0 ?
								!isValid
								:
								!isValid || (inputFile && Object.keys(inputFile).length === 0)}
						/>
					</div>
				</form>
			</Modal.Body>
			<Modal.Footer />
		</>
	);
};

ModalAddPages.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
};

export default ModalAddPages;

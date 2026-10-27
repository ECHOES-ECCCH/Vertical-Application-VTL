import React from "react";
import { useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { Modal } from "react-bootstrap";
import PropTypes from "prop-types";
import { useForm } from "react-hook-form";

import api from "@/services/api";

import { alertActions } from "@/store/actions/alert.actions";
import { appActions } from "@/store/actions/app.actions";
import FormSubmit from "@Elements/Form/FormSubmit";
import FormInputFile from "@Elements/Form/FormInputFile";
import Button from "@Elements/Button/Button";
import Alert from "@Elements/Alert/Alert";
import DropdownForm from "@Elements/Dropdown/DropdownForm";

import modalStyles from '@Elements/Modal/parts/ModalDropdown.module.sass';

const ModalImportTextToLayer = ({
	handleOnClose,
	modalProps,
	transcriptionId,
	layers,
	pageId = null,
}) => {
	const { t } = useTranslation();
	const dispatch = useDispatch();

	const {
		control,
		register,
		handleSubmit,
		formState: { errors, isValid },
		setError,
		watch,
		reset,
	} = useForm({
		defaultValues: {
			selectedLayer: {
				label: layers[0].name,
				value: layers[0].id,
			},
			file: '',
		},
		mode: "onChange",
	});

	const inputFileWatch = watch('file');

	const formats = [
		'.hocr',
		'.tei',
		'.txt',
	];
	if (!pageId) {
		formats.push('.zip');
	}

	const onClose = () => modalProps?.setModalVisible === undefined ? handleOnClose() : modalProps.setModalVisible(false);

	const onError = (response) => {
		dispatch(
			alertActions.setAlert({
				type: "danger",
				icon: "icon-circle-warning-empty",
				text: "form.messages.uploadFailed",
				close: true,
			}),
		);

		if (response.data && response.data.code) {
			switch (response.data.code) {
				case 'pages.error.wrongFileType': {
					const files = response.data.params;
					const message = files.length > 1 ? `${t('form.messages.wrongFilesType')}: ${files.join(', ')}` : `${t('form.messages.wrongFileType')}: ${files[0]}`;
					setError('file', { type: 'wrongFilesType', message });
					break;
				}
				case 'import.error.unrecognizablePage': {
					const files = response.data.params;
					const message = files.length > 1 ? `${t('form.messages.unrecognizablePages')}: ${files.join(', ')}` : `${t('form.messages.unrecognizablePage')}: ${files[0]}`;
					setError('file', { type: 'unrecognizablePage', message });
					break;
				}
				case 'import.error.wrongFileProvided': {
					setError('file', { type: 'wrongFileProvided', message: t('form.messages.wrongFileProvided') });
					break;
				}
				case 'pages.not.equal.to.transcription.pages.exception': {
					setError('file', { type: 'pagesNotEqual', message: t('form.messages.pagesNotEqual') });
					break;
				}
				default:
					return false;
			}
		}
	};

	const onSuccess = (response) => {
		dispatch(
			alertActions.setAlert({
				type: "success",
				icon: "icon-accept",
				text: "form.messages.uploadSuccess",
				close: true,
			}),
		);
		window.location.reload(false);
		reset();
		dispatch(appActions.setLoading(false));
	};

	const onSubmit = async (data) => {
		const formData = new FormData();
		dispatch(appActions.setLoading(true));
		if (pageId) {
			formData.append('file', data.file[0]);
			const apiUrl = `/pages/${pageId}/content?layerId=${data.selectedLayer.value}`;
			try {
				const response = await api.post(apiUrl, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
				onSuccess(response);
			} catch (error) {
				onerror(error);
			} finally {
				dispatch(appActions.setLoading(false));
			}
		} else {
			formData.append('file', data.file[0]);
			const apiUrl = `/transcriptions/${transcriptionId}/layer/${data.selectedLayer.value}/import`;
			try {
				const response = await api.put(apiUrl, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
				onSuccess(response);
			} catch (error) {
				onError(error);
			} finally {
				dispatch(appActions.setLoading(false));
			}
		}
	};

	return (
		<form
			onSubmit={handleSubmit(onSubmit)}
		>
			<Modal.Body>
				<Alert />
				<DropdownForm
					label={t('modals.importTextToLayer.chooseLayer')}
					options={[
						...layers.map(layer => ({
							value: layer.id,
							label: `${t('modals.downloadResults.textLayerLabel')}: ${layer.name}`,
						})),
					]}
					control={control}
					name='selectedLayer'
					className={pageId ? "dropdown-new dropdown-viewer" : modalStyles['modal-dropdown']}
					style={{ marginBottom: '20px' }}
				/>

				<label>
					{t('modals.importTextToLayer.addFileInFormat')}
				</label>
				<div className='block__form--row'>
					<FormInputFile
						dict={null}
						register={register}
						errorMessage={errors.file && errors.file.message}
						formats={formats}
						chooseFile={inputFileWatch && Object.values(inputFileWatch).map((value) => { return value.name; }).toString()}
					/>
				</div>
			</Modal.Body>
			<Modal.Footer>
				<div className={modalStyles["form-buttons"]}>
					<Button
						variant='secondary'
						onClick={onClose}
						className='block__button--cancel'
					>
						{t('form.cancel')}
					</Button>
					<FormSubmit
						label={t('modals.importTextToLayer.importText')}
						className='block__button--submit'
						variant='submit'
						disabled={!isValid || (!inputFileWatch || Object.keys(inputFileWatch).length === 0)}
					/>
				</div>
			</Modal.Footer>
		</form>);
};

ModalImportTextToLayer.propTypes = {
	handleOnClose: PropTypes.func,
	modalProps: PropTypes.object,
	transcriptionId: PropTypes.number,
	pageId: PropTypes.number,
	layers: PropTypes.array,
};

export default ModalImportTextToLayer;

import React, { useState } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";
import { useForm } from 'react-hook-form';

import { appActions } from '@/store/actions/app.actions';
import { alertActions } from "@/store/actions/alert.actions";

import Button from '@Elements/Button/Button';
import CommonDropdownValue from '@Elements/Dropdown/CommonDropdownValue';
import FormSubmit from "@Elements/Form/FormSubmit";
import Alert from "@Elements/Alert/Alert";

import styles from "@Elements/Modal/parts/Styles/ModalLoadTextTranscriptionPage.module.sass";

import api from "@/services/api";

const ModalLoadTextTranscriptionPage = ({ handleOnClose, transcriptionId, layers }) => {
	const { t } = useTranslation();

	const pageId = useSelector((state) => state.app.modalLoadTextTranscriptionPage);
	const [selectedLayer, setSelectedLayer] = useState(layers[0].id);

	const dispatch = useDispatch();

	const { register, handleSubmit, formState: {isValid}, setError, reset } = useForm({
		mode: "onChange",
	});

	const onSubmit = (data) => {
		const formData = new FormData();
		formData.append('file', data.file[0]);
		dispatch(appActions.setLoading(true));

		const apiUrl = pageId ? `pages/${pageId}/content?layerId=${selectedLayer}` : `/transcription/${transcriptionId}/content`;

		api.post(apiUrl, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
			.then(() => {
				dispatch(appActions.setLoading(false));
				dispatch(
					alertActions.setAlert({
						type: "success",
						icon: "icon-accept",
						text: "form.messages.uploadSuccess",
						close: true,
					}),
				);
				reset();
				window.location.reload(false);
			}).catch((response) => {
				dispatch(appActions.setLoading(false));
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
							setError('file', {type: 'wrongFilesType', message});
							break;
						}
						case 'import.error.unrecognizablePage': {
							const files = response.data.params;
							const message = files.length > 1 ? `${t('form.messages.unrecognizablePages')}: ${files.join(', ')}` : `${t('form.messages.unrecognizablePage')}: ${files[0]}`;
							setError('file', {type: 'unrecognizablePage', message});
							break;
						}
						case 'import.error.wrongFileProvided': {
							setError('file', {type: 'wrongFileProvided', message: t('form.messages.wrongFileProvided')});
							break;
						}
						case 'pages.not.equal.to.transcription.pages.exception': {
							setError('file', {type: 'pagesNotEqual', message: t('form.messages.pagesNotEqual')});
							break;
						}
						default:
							return false;
					}
				}
			});
	};

	const formats = pageId ? [
		'.hocr',
		'.tei',
		'.txt',
	] : [
		'.hocr',
		'.tei',
		'.html',
		'.xml',
		'.zip',
	];

	return (
		<form onSubmit={handleSubmit(onSubmit)}>
			<Modal.Body>
				<Alert />
				<div className={styles["modal-loadtext--dropbox-container"]}>
					<CommonDropdownValue
						{...register('layer')}
						className={styles["modal-loadtext--dropbox"]}
						label={t('modals.loadTextTranscriptionPage.body')}
						options={
							layers.map(option => { return { label: option.name, value: option.id }; })
						}
						selectedValue={selectedLayer}
						setSelectedValue={(value) => { setSelectedLayer(value); }}
					/>
				</div>
				<p className={styles["modal-loadtext--body"]} dangerouslySetInnerHTML={{ __html: t('modals.loadTextTranscriptionPage.bodyInfo') }} />

				<label htmlFor='upload' tabIndex='0' className={styles["modal-loadtext--inputfile"]}>
					{t('modals.loadTextTranscriptionPage.buttonAdd')}
					<input
						accept={formats.join(',')}
						type='file'
						id='upload'
						{...register('file', {
							required: true,
						})}
						style={{ display: 'none' }}
					/>
				</label>
			</Modal.Body>
			<Modal.Footer>
				<div className={styles["modal-loadtext--footer"]}>
					<Button
						variant='secondary'
						onClick={handleOnClose}
						className='modal__button--cancel'
					>
						{t('form.cancel')}
					</Button>

					<FormSubmit
						label={t('modals.loadTextTranscriptionPage.button')}
						variant='submit'
						//disabled={!formState.isValid || (inputFile && Object.keys(inputFile).length === 0)}
						disabled={!isValid}
					/>
				</div>
			</Modal.Footer>
		</form>
	);
};

ModalLoadTextTranscriptionPage.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
	layers: PropTypes.array,
};

export default ModalLoadTextTranscriptionPage;
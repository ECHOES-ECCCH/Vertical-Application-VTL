import React, { useState, useEffect } from "react";
import { useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { Container, Dropdown } from "react-bootstrap";
import { Link } from "react-router-dom";
import { useParams } from "react-router";
import { reverse } from 'named-urls';

import url from "@/router/urls";
import api from "@/services/api";

import { alertActions } from "@/store/actions/alert.actions";
import { appActions } from '@/store/actions/app.actions';

import { CheckViewModePermission } from "@/permissions/ViewModes";

import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import Alert from "@Elements/Alert/Alert";
import Error403 from "@Pages/Error/Error403";

import FormSubmit from "@Elements/Form/FormSubmit";
import FormInputFile from "@Elements/Form/FormInputFile";
import styles from "@Elements/Modal/parts/Styles/ModalDownloadResults.module.sass";

const TranscriptionAddText = () => {
	const { t } = useTranslation();

	const dispatch = useDispatch();
	const { transcriptionId, pageId } = useParams();

	const [title, setTitle] = useState('');
	const [viewMode, setViewMode] = useState('read-only');

	const [layers, setLayers] = useState([]);
	const [selectedLayer, setSelectedLayer] = useState(0);

	useEffect(() => {
		getTranscription();
	}, []);

	useEffect(() => {
		api.get(`/transcriptions/${transcriptionId}/layer`)
			.then(response => {
				if (response && response.data) {
					setLayers(response.data);
					if (response.data.length > 0) {
						setSelectedLayer(response.data[0].id);
					}
				}
			});
	}, []);

	const getTranscription = () => {
		dispatch(appActions.setLoading(true));

		api.get(`/transcriptions/${transcriptionId}`)
			.then(({ data }) => {
				setTitle(data.analyticInfo.title);
				setViewMode(data.viewMode);
				dispatch(appActions.setLoading(false));
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
			});
	};

	const breadcrumbsList = [
		{ id: 1, label: "home", link: url.home },
		{ id: 2, label: t('breadcrumbs.transcriptions'), link: url.transcriptions },
		{ id: 3, label: title, link: reverse(url.transcription.details, { transcriptionId: transcriptionId }) },

	];

	if (pageId) {
		breadcrumbsList.push(
			{
				id: 4,
				label: `[#${pageId}]`,
				link: reverse(url.transcription.viewer, { transcriptionId: transcriptionId, pageId: pageId }),
			},
			{ id: 5, label: t('breadcrumbs.addTextPage') },
		);
	} else {
		breadcrumbsList.push(
			{ id: 4, label: t('breadcrumbs.addTextPages') },
		);
	}

	const {
		register,
		handleSubmit,
		formState,
		setError,
		watch,
		reset,
	} = useForm({
		mode: "onChange",
	});

	const {
		errors,
	} = formState;

	const inputFile = watch('file');

	const formats = pageId ? [
		'.hocr',
		'.tei',
		'.html',
		'.xml',
	] : [
		'.hocr',
		'.tei',
		'.html',
		'.xml',
		'.zip',
	];

	const onSubmit = data => {
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

	return CheckViewModePermission("canAddNewPages", viewMode) ? (
		<main>
			<Container>
				<Breadcrumbs breadcrumbsList={breadcrumbsList} />
				<div className='block block--750'>
					<Alert />
					<div className='block__header'>
						<h1 className='text--title'>
							{pageId ? t('transcription.addText.titlePage') : t('transcription.addText.titlePages')}
						</h1>
					</div>

					<div className='block__content'>
						<p className='paragraph__mb--35'>
							{pageId ? t('transcription.addText.descriptionPage') : t('transcription.addText.descriptionPages')}
						</p>
						{t('modals.downloadResults.chooseLayers')}
						<Dropdown className={styles["modal-grid--dropdown"]}>
							<Dropdown.Toggle
								className={styles["modal-grid--dropdown-btn"]}
								variant='dropdown'
								id='download'
							>
								{
									layers.length > 0 && (
										(selectedLayer > 0 && layers.filter(x => x.id === selectedLayer)[0].name) ||
										(layers[0].name)
									)
								}
							</Dropdown.Toggle>

							<Dropdown.Menu className={styles["modal-grid--dropdown-list"]}>
								{layers.map(option => {
									return (
										<Dropdown.Item
											key={option.id}
											onClick={() => {
												setSelectedLayer(option.id);
											}}
											className={styles["modal-grid--dropdown-item"]}
										>
											{option.name}
										</Dropdown.Item>
									);
								})}
							</Dropdown.Menu>
						</Dropdown>

						<form
							className='block__form block__form--inputfile'
							onSubmit={handleSubmit(onSubmit)}
						>
							<div className='block__form--row'>
								<FormInputFile
									dict={null}
									register={register}
									errorMessage={errors.file && errors.file.message}
									formats={formats}
									chooseFile={inputFile && Object.values(inputFile).map((value) => {
										return value.name;
									}).toString()}
								/>
							</div>

							<div className='block__form--row block__form--buttons'>
								<Link
									className='block__button--cancel'
									to={reverse(url.transcription.details, { transcriptionId: transcriptionId })}
								>
									{t('form.cancel')}
								</Link>
								<FormSubmit
									label={t('form.inputFile.sendFile')}
									className='block__button--submit'
									variant='submit'
									disabled={!formState.isValid || (inputFile && Object.keys(inputFile).length === 0)}
								/>
							</div>
						</form>
					</div>
				</div>
			</Container>
		</main>
	) : (
		<Error403 />
	);
};

export default TranscriptionAddText;
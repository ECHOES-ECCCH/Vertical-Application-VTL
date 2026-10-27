import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { Container } from "react-bootstrap";
import { Link } from "react-router-dom";
import { useHistory } from "react-router";

import url from "@/router/urls";
import api from "@/services/api";

import { appActions } from '@/store/actions/app.actions';

import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import Alert from "@Elements/Alert/Alert";
import FormInput from "@Elements/Form/FormInput";
import Button from "@Elements/Button/Button";
import { useShowModal } from '@Elements/Modal/ModalHooks';

import SearchTranscriptions from "@Elements/Search/SearchTranscriptions";
import SearchTypeEnum from "@/utils/SearchTypeEnum";

import FormSubmit from "@Elements/Form/FormSubmit";
import styles from '@Pages/Transcription/TranscriptionImport/TranscriptionImport.module.sass';

import CommonDropdown from '@Elements/Dropdown/CommonDropdown';

const importFormats = [
	{ value: 'iiif', label: "IIIF" },
	{ value: 'mets', label: 'METS' },
	{ value: 'tei', label: 'TEI' },
];

const TranscriptionImport = () => {
	const history = useHistory();
	const { t } = useTranslation();

	const dispatch = useDispatch();
	
	const daceUrl = useSelector(state => state.app.config.daceUri);

	const [dropdownOption, setDropdownOption] = useState(0);

	const [searchString, setSearchString] = useState(null);
	const [useMetadata, setUseMetadata] = useState(SearchTypeEnum.ALL.toString());

	const breadcrumbsList = [
		{ id: 1, label: "home", link: url.home },
		{ id: 2, label: t('breadcrumbs.transcriptionNew'), link: url.transcription.new },
		{ id: 3, label: t('breadcrumbs.transcriptionImport') },
	];

	const {
		register,
		handleSubmit,
		setError,
		formState: {
			errors, isValid,
		},
		clearErrors,
	} = useForm({
		defaultValues: {
			resourceUrl: "",
		},
		mode: "onChange",
	});
	
	useEffect(() => {
		let currentUrlParams = new URLSearchParams(window.location.search);
		const daceId = currentUrlParams.get("daceId");
		let resourceUrl, format;
		if (daceId) {
			resourceUrl = daceUrl + "?daceId=" + daceId + "&schemaId=dace&page=0";
			format = "dace";
		} else {
			importFormats.forEach(f => {
				if (currentUrlParams.get(f.value)) {
					resourceUrl = currentUrlParams.get(f.value);
					format = f.value;
				}
			});
		}
		if (format) {
			api.put('imports/', {
				resourceUrl: decodeURIComponent(resourceUrl),
				fileType: format,
				externalId: currentUrlParams.get("externalId") || decodeURIComponent(resourceUrl),
				metadataUrl: currentUrlParams.get("metadataUrl"),
				metadataType: currentUrlParams.get("metadataType"),
			}).then((res) => {
				dispatch(appActions.setImportedTranscription(res.data));
				history.replace(url.transcription.new);
			}).catch((error) => {
				dispatch(appActions.setLoading(false));
			});
		}
	}, [true]);

	useEffect(() => {
		clearErrors("resourceUrl");
		dispatch(appActions.setImportsDomains(importFormats[dropdownOption].value));
	}, [dropdownOption]);

	const onSubmit = data => {
		dispatch(appActions.setLoading(true));
		api.put('imports/', { resourceUrl: data.resourceUrl, fileType: importFormats[dropdownOption].value })
			.then((res) => {
				dispatch(appActions.setImportedTranscription(res.data));
				history.push(url.transcription.new);
			})
			.catch((error) => {
				dispatch(appActions.setLoading(false));
				if (error.data && error.data.resourceUrl && error.data.resourceUrl.length > 0) {
					setError('resourceUrl', {type: 'error', message: t('transcription.import.errors.' + error.data.resourceUrl.replace(/[\.-]/g, ""))});  // eslint-disable-line
				}
				if (error.data && error.data.code) {
					switch (error.data.code) {
						case 'domain.error.not.available.tei':
						case 'domain.error.not.available.iiif':
						case 'domain.error.not.available.mets': {
							setError('resourceUrl', {type: 'error', message: t('transcription.import.errors.' + error.data.code.replace(/[\.-]/g, ""))});  // eslint-disable-line
							break;
						}

						default:
							return false;
					}

				}

			});
	};

	const modalAvailableImportSources = useShowModal({
		title: t('modals.importAvailableSources.title.' + importFormats[dropdownOption].value),
		name: 'availableImportSources',
		componentPath: './parts/ModalAvailableImportSources',
	});

	return (
		<main>
			<SearchTranscriptions
				setSearchString={setSearchString}
				searchString={searchString}
				setUseMetadata={setUseMetadata}
			/>
			<Container>
				<Breadcrumbs breadcrumbsList={breadcrumbsList} />
				<div className='block block--750'>
					<Alert />
					<div className='block__header'>
						<h1 style={{ fontSize: "1.75rem" }} className='text--title'>
							{t('transcription.import.title')}
						</h1>
					</div>

					<div className='block__content'>
						<form
							className='block__form block__form--inputfile'
							onSubmit={handleSubmit(onSubmit)}
						>
							<div className={`${styles["transcription-import__dropdown"]}`}>
								<CommonDropdown
									label={t('transcription.import.selectLabel')}
									options={
										importFormats.map(it => it.label)
									}
									selectedOption={dropdownOption}
									setSelectedOption={setDropdownOption}
									className='dropdown-new'
									isRequired
								/>
							</div>

							<div className={`block__form--row ${styles["transcription-import__input"]}`}>
								<FormInput
									name='resourceUrl'
									register={register}
									label={t('transcription.import.urlLabel')}
									sublabel={t('transcription.import.urlSublabel')}
									validateData={{
										required: {
											value: true,
											message: t('form.messages.emptyUrl'),
										},
										pattern: { // eslint-disable-line
											value: /^(?:(?:(?:https?|ftp):)?\/\/)(?:\S+(?::\S*)?@)?(?:(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})))(?::\d{2,5})?(?:[/?#]\S*)?$/, // eslint-disable-line
											message: t('form.messages.invalidUrl'),
										},
									}}
									type='text'
									errorMessage={errors.resourceUrl ? errors.resourceUrl.message : ''}
									isRequired
								/>
								<Button
									variant='link'
									onClick={() => modalAvailableImportSources()}
									className={`${styles["transcription-import__sources"]}`}
								>
									{t('transcription.import.checkList')}
								</Button>
							</div>
							<p className='transcription-new__required-info'><span className='asterisk'>*</span>{t('form.requiredInfo')}</p>
							<div className='block__form--row block__form--buttons'>
								<Link
									className='block__button--cancel btn-secondary'
									to={url.transcription.new}
								>
									{t('form.cancel')}
								</Link>
								<FormSubmit
									label={t('transcription.import.startImport')}
									className='block__button--submit'
									variant='submit'
									disabled={!isValid}
								/>
							</div>
						</form>
					</div>
				</div>
			</Container>
		</main>
	);
};

export default TranscriptionImport;

import React, { useState, useEffect, useMemo } from "react";
import { useDispatch, useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { Container } from "react-bootstrap";
import { Link } from "react-router-dom";
import { useHistory } from "react-router";

import url from "@/router/urls";
import api from "@/services/api";
import EchoesPlatformClient from "@/services/echoes/echoesPlatformClient";
import { getEchoesAccessToken } from "@/services/echoes/echoesTokenProvider";

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

const SOURCE_URL = 0;
const SOURCE_ECHOES = 1;

const assetLabel = (asset) => {
	const name = asset.fileName || asset.assetId;
	return asset.managementMode ? `${name} — ${asset.managementMode}` : name;
};

const TranscriptionImport = () => {
	const history = useHistory();
	const { t } = useTranslation();

	const dispatch = useDispatch();
	
	const config = useSelector(state => state.app.config);
	const daceUrl = config.daceUri;

	const [dropdownOption, setDropdownOption] = useState(0);
	const [sourceOption, setSourceOption] = useState(SOURCE_URL);
	const [echoesAssets, setEchoesAssets] = useState([]);
	const [selectedAssetOption, setSelectedAssetOption] = useState(-1);
	const [echoesLoading, setEchoesLoading] = useState(false);
	const [echoesError, setEchoesError] = useState('');

	const [searchString, setSearchString] = useState(null);
	const [useMetadata, setUseMetadata] = useState(SearchTypeEnum.ALL.toString());

	const platformClient = useMemo(() => new EchoesPlatformClient({
		baseUrl: config.commonPlatformApi,
		getAccessToken: getEchoesAccessToken,
	}), [config.commonPlatformApi]);

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
		shouldUnregister: true,
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
	}, [dropdownOption, sourceOption]);

	useEffect(() => {
		if (sourceOption !== SOURCE_ECHOES) {
			setEchoesError('');
			return undefined;
		}

		if (!config.commonPlatformApi) {
			setEchoesAssets([]);
			setSelectedAssetOption(-1);
			setEchoesError(t('echoesImport.missingApi'));
			return undefined;
		}

		let active = true;
		setEchoesLoading(true);
		setEchoesError('');
		setEchoesAssets([]);
		setSelectedAssetOption(-1);

		platformClient.listMyAssets()
			.then((collection) => {
				if (!active) return;
				const assets = collection?.items || [];
				setEchoesAssets(assets);
				setSelectedAssetOption(assets.length > 0 ? 0 : -1);
			})
			.catch((error) => {
				if (!active) return;
				setEchoesError(error?.message || t('echoesImport.loadFailed'));
			})
			.finally(() => {
				if (active) setEchoesLoading(false);
			});

		return () => {
			active = false;
		};
	}, [sourceOption, platformClient, config.commonPlatformApi, t]);

	const selectedAsset = selectedAssetOption >= 0 ? echoesAssets[selectedAssetOption] : null;
	const isEchoesSource = sourceOption === SOURCE_ECHOES;

	const onSubmit = data => {
		if (isEchoesSource && !selectedAsset) return;

		dispatch(appActions.setLoading(true));
		setEchoesError('');

		const payload = isEchoesSource
			? { echoesAssetId: selectedAsset.assetId, fileType: importFormats[dropdownOption].value }
			: { resourceUrl: data.resourceUrl, fileType: importFormats[dropdownOption].value };

		api.put('imports/', payload)
			.then((res) => {
				dispatch(appActions.setImportedTranscription(res.data));
				history.push(url.transcription.new);
			})
			.catch((error) => {
				dispatch(appActions.setLoading(false));

				if (isEchoesSource) {
					setEchoesError(error?.data?.message || error?.message || t('echoesImport.importFailed'));
					return;
				}

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

	const sourceOptions = [t('echoesImport.urlSource'), t('echoesImport.personalSpaceSource')];
	const canSubmit = isEchoesSource ? Boolean(selectedAsset) && !echoesLoading : isValid;

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
									label={t('echoesImport.sourceLabel')}
									options={sourceOptions}
									selectedOption={sourceOption}
									setSelectedOption={setSourceOption}
									className='dropdown-new'
									isRequired
								/>
							</div>

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

							{!isEchoesSource && (
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
							)}

							{isEchoesSource && (
								<div className={`${styles["transcription-import__input"]}`}>
									{echoesLoading && <p>{t('echoesImport.loading')}</p>}
									{!echoesLoading && echoesAssets.length > 0 && (
										<div className={`${styles["transcription-import__dropdown"]}`}>
											<CommonDropdown
												label={t('echoesImport.assetLabel')}
												options={echoesAssets.map(assetLabel)}
												selectedOption={selectedAssetOption}
												setSelectedOption={setSelectedAssetOption}
												className='dropdown-new'
												isRequired
											/>
										</div>
									)}
									{!echoesLoading && !echoesError && echoesAssets.length === 0 && <p>{t('echoesImport.noAssets')}</p>}
									{selectedAsset && <p className='text-muted'>{selectedAsset.mediaType || t('echoesImport.unknownMediaType')} · {selectedAsset.assetId}</p>}
									{echoesError && <p className='text-danger'>{echoesError}</p>}
								</div>
							)}

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
									disabled={!canSubmit}
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

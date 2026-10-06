import React, { useState } from "react";
import { useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { Container } from "react-bootstrap";
import { Link } from "react-router-dom";
import { useHistory, useParams } from "react-router";
import { reverse } from "named-urls";

import FormRadioGroup from '@Elements/Form/FormRadioGroup';

import url from "@/router/urls";
import api from "@/services/api";

import { appActions } from '@/store/actions/app.actions';
import { alertActions } from "@/store/actions/alert.actions";

import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import Alert from "@Elements/Alert/Alert";
import FormInput from "@Elements/Form/FormInput";

import SearchTranscriptions from "@Elements/Search/SearchTranscriptions";
import SearchTypeEnum from "@/utils/SearchTypeEnum";

import FormSubmit from "@Elements/Form/FormSubmit";

const TranscriptionAddLayer = () => {
	const history = useHistory();
	const { t } = useTranslation();
	const { transcriptionId } = useParams();

	const dispatch = useDispatch();

	const [searchString, setSearchString] = useState(null);
	const [useMetadata, setUseMetadata] = useState(SearchTypeEnum.ALL.toString());

	const breadcrumbsList = [
		{ id: 1, label: "home", link: url.home },
		{ id: 2, label: t('breadcrumbs.transcriptions'), link: url.transcriptions },
		{ id: 3, label: t('breadcrumbs.transcriptionImport') },
	];

	const {
		register,
		handleSubmit,
		watch,
		formState: {
			errors, isValid,
		},
	} = useForm({
		defaultValues: {
			layerName: '',
			layerDescription: '',
			layerType: 'null',
			copyVerses: '1',
			copyBlocks: '1',
		},
		mode: "onChange",
	});
	const watchCopyVerses = watch('copyVerses');
	const watchLayerType = watch('layerType');

	const parseCopyType = (versesOption, blocksOption) => {
		if (versesOption === 2) {
			if (blocksOption === 1) return 'VERSES_EMPTY';
			if (blocksOption === 2) return 'BLOCKS_EMPTY';
		}
		if (versesOption === 3) {
			if (blocksOption === 1) return 'VERSES';
			if (blocksOption === 2) return 'BLOCKS';
		}
		return 'NONE';
	};

	const onSubmit = async (data) => {
		dispatch(appActions.setLoading(true));
		const dataToSend = {
			name: data.layerName,
			description: data.layerDescription,
			type: data.layerType === 'null' ? null : data.layerType,
			...(data.layerType === 'null' && { copy: parseCopyType(parseInt(data.copyVerses), parseInt(data.copyBlocks)) }),
		};
		try {
			await api.post(`/transcriptions/${transcriptionId}/layer`, dataToSend);
			setSuccessToast();
		} catch (err) {
			console.log('transcriptionAddLayer ERROR: ', err); // eslint-disable-line
		} finally {
			dispatch(appActions.setLoading(false));
		}
		history.push(reverse(url.transcription.layers, { transcriptionId: transcriptionId }));
	};

	const setSuccessToast = () => {
		dispatch(
			alertActions.setAlert({
				type: "success",
				icon: "icon-circle-accept",
				text: t('transcriptions.addLayerPage.layerAddedToast'),
				close: true,
			}),
		);
	};

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
							{t('transcriptions.addLayerPage.title')}
						</h1>
					</div>

					<div className='block__content'>
						<form
							className='block__form block__form--inputfile'
							onSubmit={handleSubmit(onSubmit)}
						>
							<p>{t('transcriptions.addLayerPage.addLayerStep1')}</p>
							<FormInput
								name='layerName'
								register={register}
								label={t('transcriptions.addLayerPage.layerName')}
								validateData={{
									required: {
										value: true,
										message: t('form.messages.fillField'),
									},
								}}
								type='text'
								errorMessage={errors.layerName ? errors.layerName.message : ''}
								isRequired
							/>
							<FormInput
								name='layerDescription'
								register={register}
								label={t('transcriptions.addLayerPage.layerDescription')}
								validateData={{
									required: {
										value: true,
										message: t('form.messages.fillField'),
									},
								}}
								type='text'
								errorMessage={errors.layerDescription ? errors.layerDescription.message : ''}
							/>

							<FormRadioGroup
								label={t('transcriptions.addLayerPage.addLayerStep2')}
								name='layerType'
								register={register}
								options={[
									{ label: t('transcriptions.addLayerPage.step2option1'), value: 'null' },
									{ label: t('transcriptions.addLayerPage.step2option2'), value: 'IIIF_ANNOTATION' },
								]}
								className='block__content'
								errorMessage={errors.layerType ? errors.layerType.message : ''}
							/>

							<FormRadioGroup
								label={t('transcriptions.addLayerPage.addLayerStep3')}
								name='copyVerses'
								register={register}
								options={[
									{ label: t('transcriptions.addLayerPage.step3option1'), value: '1' },
									{ label: t('transcriptions.addLayerPage.step3option2'), value: '2' },
									{ label: t('transcriptions.addLayerPage.step3option3'), value: '3' },
								]}
								className='block__content'
								errorMessage={errors.copyVerses ? errors.copyVerses.message : ''}
								disabled={watchLayerType !== 'null'}
							/>

							<FormRadioGroup
								label={t('transcriptions.addLayerPage.addLayerStep4')}
								name='copyBlocks'
								register={register}
								options={[
									{ label: t('transcriptions.addLayerPage.step4option1'), value: '1' },
									{ label: t('transcriptions.addLayerPage.step4option2'), value: '2' },
								]}
								className='block__content'
								errorMessage={errors.copyBlocks ? errors.copyBlocks.message : ''}
								disabled={watchCopyVerses === '1' || watchLayerType !== 'null'}
							/>

							<div className='block__form--row block__form--buttons'>
								<Link
									className='block__button--cancel btn-secondary'
									to={reverse(url.transcription.layers, { transcriptionId: transcriptionId })}
								>
									{t('form.cancel')}
								</Link>
								<FormSubmit
									label={t('transcriptions.addLayerPage.addlayerbt')}
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

export default TranscriptionAddLayer;

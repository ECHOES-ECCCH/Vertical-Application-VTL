import React, { useEffect, useState } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal, Form } from "react-bootstrap";
import PropTypes from "prop-types";
import { useForm } from "react-hook-form";
import { useHistory } from "react-router-dom";

import { alertActions } from "@/store/actions/alert.actions";

import api from "@/services/api";
import { appActions } from "@/store/actions/app.actions";

import Button from "@Elements/Button/Button";
import FormSubmit from "@Elements/Form/FormSubmit";
import FormInput from "@Elements/Form/FormInput";

import styles from "@Elements/Modal/parts/Styles/ModalTrainOCR.module.sass";

import CommonDropdownValue from '@Elements/Dropdown/CommonDropdownValue';
import Alert from "@Elements/Alert/Alert";

const ModalTrainOCR = ({ handleOnClose, layers, pageCount }) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();
	const history = useHistory();
	
	const currentLang = useSelector(state => state.app.currentLang);
	
	const languagePlaceholder = { label: "---", value: "" };

	const [selectedLayer, setSelectedLayer] = useState(layers[0].id);
	const [rangeType, setRangeType] = useState(0);
	const [languageOptions, setLanguageOptions] = useState([languagePlaceholder]);
	const [selectedLanguage, setSelectedLanguage] = useState(languagePlaceholder.value);

	const { register, handleSubmit, formState: { errors } } = useForm({
		defaultValues: {
			verses: "",
		},
		mode: "onChange",
	});
	
	useEffect(() => {
		getTrainableLanguages();
	}, [ currentLang ]);
	
	const getTrainableLanguages = () => {
		api.get("/ocr/train/languages")
			.then(res => {
				setLanguageOptions(res.data.map((roleItem) => (
					{
						label: roleItem[currentLang],
						value: roleItem.code,
					}
				)));
				setSelectedLanguage(res.data[0].code);
			});
	};

	const onSubmit = data => {
		let ranges = [];
		if (rangeType == 1 && data.pageRange) {
			ranges = data.pageRange.split(',').map(x => {
				const r = x.split('-');
				let fromPage = 0, toPage = 0;
				if (r.length == 1) {
					fromPage = toPage = parseInt(r[0]);
				}
				else {
					fromPage = parseInt(r[0]);
					toPage = parseInt(r[1]);
				}
				return { from: fromPage, to: toPage };
			});
		}
		else
			ranges.push({ from: 1, to: pageCount });
		handleTrainOCR(ranges);
	};

	const handleTrainOCR = (data) => {
		dispatch(appActions.setLoading(true));
		api.put(
			`/ocr/train/layer/${selectedLayer}?language=${selectedLanguage}`, data,
		)
			.then(response => {
				handleOnClose();
				window.scrollTo(0, 0);
				dispatch(appActions.setLoading(false));
				dispatch(alertActions.setAlert({
					text: 'modals.trainOCR.successAlert',
					textParams: { 0: response.data },
					type: 'success',
					icon: 'icon-accept',
					close: true,
					important: true,
				}));
			})
			.catch(response => {
				dispatch(appActions.setLoading(false));

				dispatch(
					alertActions.setAlert({
						type: "danger",
						icon: "icon-circle-warning-empty",
						text: response.data.code,
						close: true,
					}),
				);
			});
	};

	return (
		<form onSubmit={handleSubmit(onSubmit)} >
			<Modal.Body>
				<Alert />
				<CommonDropdownValue
					label={t('modals.trainOCR.select.selectLayer')}
					options={
						layers.map(option => { return { label: option.name, value: option.id }; })
					}
					selectedValue={selectedLayer}
					setSelectedValue={(value) => { setSelectedLayer(value); }}
					className='dropdown-new'
				/><br />
				<label>{t('modals.trainOCR.selectRange')}</label>
				<br />
				<div className={styles['radio__input']}>
					<input
						id='trainOCR_radio1'
						type='radio'
						value={0}
						name='type'
						checked={rangeType == 0}
						onClick={() => setRangeType(0)}
					/>
					<label htmlFor='trainOCR_radio1'>{t('modals.trainOCR.radio.pagesAll')}</label>
				</div>
				<div className={styles['radio__input']}>
					<input
						id='trainOCR_radio2'
						type='radio'
						value={1}
						name='type'
						checked={rangeType == 1}
						onClick={() => setRangeType(1)}
					/>
					<label htmlFor='trainOCR_radio2'>{t('modals.trainOCR.radio.pagesRange')}</label>
				</div>

				<Form.Group className={styles['range_group']}>
					<FormInput
						name='pageRange'
						placeholder={t('modals.trainOCR.rangeExample')}
						dict={null}
						register={register}
						className={styles['input_range']}
						isDisabled={rangeType == 0}
						validateData={{
							required: {
								value: rangeType == 1,
								message: t('form.messages.requiredPageRange'),
							},
							pattern: { // eslint-disable-line
								value: /^\d+(?:-\d+)?(?:,\d+(?:-\d+)?)*$/, // eslint-disable-line
								message: t('form.messages.invalidPageRange'),
							},
						}}
						type='text'
						errorMessage={errors.pageRange ? errors.pageRange.message : ''}
					/>
					<label>{t('modals.trainOCR.rangeLabel')}</label>
				</Form.Group>
				<div>{t('modals.trainOCR.selectLanguage')}</div>
				<CommonDropdownValue
					options={languageOptions}
					selectedValue={selectedLanguage}
					setSelectedValue={(value) => { setSelectedLanguage(value); }}
					className='dropdown-new'
				/>
			</Modal.Body>
			<Modal.Footer>
				<Button variant='secondary' onClick={handleOnClose} className='modal__button--cancel'>
					{t('form.cancel')}
				</Button>
				<FormSubmit
					label={t('modals.trainOCR.button')}
					className='modal__button--submit'
					variant='submit'
				/>
				{/* <Button variant="primary" onClick={handleTrainOCR} type="submit" className="modal__button--submit">
					{t('modals.trainOCR.button')}
				</Button> */}
			</Modal.Footer>
		</form>
	);
};

ModalTrainOCR.propTypes = {
	handleOnClose: PropTypes.func,
	layers: PropTypes.array,
	pageCount: PropTypes.number,
};

export default ModalTrainOCR;

import React, { useEffect, useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { Modal } from "react-bootstrap";
import PropTypes from "prop-types";
import { useHistory } from "react-router-dom";

import { alertActions } from "@/store/actions/alert.actions";

import api from "@/services/api";
import { appActions } from "@/store/actions/app.actions";

import Button from "@Elements/Button/Button";

import CommonDropdownValue from '@Elements/Dropdown/CommonDropdownValue';
import Alert from "@Elements/Alert/Alert";

import styles from '@Elements/Modal/parts/Styles/ModalAutoTranscribePage.module.sass';

const ModalAutoTranscribePages = ({ handleOnClose, transcriptionId, layers }) => {
	const { t } = useTranslation();

	const OCRProfiles = useSelector(state => state.app.OCRProfiles);
	const currentLang = useSelector(state => state.app.currentLang);
	const dispatch = useDispatch();
	const history = useHistory();

	const [selectedLayer, setSelectedLayer] = useState(layers[0].id);
	const [rangeType, setRangeType] = useState(0);
	const [pageRange, setPageRange] = useState(null);
	const [selectedOCRMode, setSelectedOCRMode] = useState("OVERRIDE");
	const [handleColumns, setHandleColumns] = useState(false);
	const [selectedLang, setSelectedLang] = useState(OCRProfiles[0].code);

	const handleAutoTranscribePages = () => {
		dispatch(appActions.setLoading(true));

		let url = `/transcriptions/${transcriptionId}/ocr`
			+ `?lang=${selectedLang}`
			+ `&layerId=${selectedLayer}`
			+ `&ocrMode=${selectedOCRMode}`
			+ `&handleColumns=${handleColumns && selectedOCRMode !== "UPDATE"}`;

		if (rangeType === 1) {
			url += `&pages=${pageRange}`;
		}

		api.put(url)
			.then(() => {
				history.go(0);
				dispatch(appActions.setLoading(false));
				handleOnClose();
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

	useEffect(() => {
		api.get(`transcriptions/${transcriptionId}/default-language`).then(({ data }) => {
			setSelectedLang(data);
		});
	}, []);

	return (
		<>
			<Modal.Body>
				<Alert />
				<CommonDropdownValue
					label={t('modals.autoTranscribePages.select.selectLayer')}
					options={
						layers.filter(layer => layer.type !== "IIIF_ANNOTATION").map(option => { return {label: option.name, value: option.id }; })
					} 
					selectedValue={selectedLayer}
					setSelectedValue={ (value)=>{ setSelectedLayer(value); } }
					className='dropdown-new'
				/><br />
				<div>
					<input
						id='autoTranscribe_radio1'
						type='radio'
						value={0}
						name='type'
						checked={rangeType === 0}
						onClick={() => setRangeType(0)}
						className={styles['transcribepages--radio']}
					/>
					<label htmlFor='autoTranscribe_radio1' className={styles['transcribepages--label']}>
						{t('modals.autoTranscribePages.radio.pagesAll')}
					</label>
				</div>
				<div>
					<input
						id='autoTranscribe_radio2'
						type='radio'
						value={1}
						name='type'
						checked={rangeType === 1}
						onClick={() => setRangeType(1)}
						className={styles['transcribepages--radio']}
					/>
					<label htmlFor='autoTranscribe_radio2'>{t('modals.autoTranscribePages.radio.pagesRange')}</label>
				</div>
				<div>
					<input
						name='pageRange'
						placeholder={t('modals.autoTranscribePages.input.placeholder')}
						className='form-control'
						disabled={rangeType == 0}
						type='text'
						onChange={e => {
							if (e.target.value === '') {
								setPageRange(null);
							} else {
								setPageRange(e.target.value);
							}
						}}
					/>
					<label>{t('modals.autoTranscribePages.input.label')}</label>
				</div><br />
				<CommonDropdownValue
					label={t('modals.autoTranscribePages.select.titleRange')}
					options={[
						{label:t('modals.autoTranscribePages.select.ocrMode.emptyonly'), value: "EMPTYONLY"},
						{label:t('modals.autoTranscribePages.select.ocrMode.update'), value: "UPDATE" },
						{label:t('modals.autoTranscribePages.select.ocrMode.override'), value: "OVERRIDE"},
					]} 
					selectedValue={selectedOCRMode}
					setSelectedValue={ (value)=>{ setSelectedOCRMode(value); } }
					className='dropdown-new'
				/><br />
				<CommonDropdownValue
					label={t('modals.autoTranscribePages.select.titleProfile')}
					options={
						OCRProfiles.map(item => { return { label: item[currentLang] || item['en'], value: item.code }; })
					} 
					selectedValue={selectedLang}
					setSelectedValue={ (value)=>{ setSelectedLang(value); } }
					className='dropdown-new'
				/><br />
				<div>
					<input
						id='handleColumns'
						type='checkbox'
						checked={handleColumns}
						onChange={e => setHandleColumns(e.target.checked)}
						disabled={selectedOCRMode === "UPDATE"}
						className={styles['transcribepages--checkbox']}
					/>
					<label htmlFor='handleColumns' className={styles['transcribepages--label']}>
						{t('modals.autoTranscribePages.checkbox.handleColumns')}
					</label>
				</div>
				<div className='modal__alert'>{t('modals.autoTranscribePages.notice')}</div><br />
				<div>{t('modals.autoTranscribePages.select.asterisk')}</div>
			</Modal.Body>
			<Modal.Footer>
				<Button variant='secondary' onClick={handleOnClose} className='modal__button--cancel'>
					{t('form.cancel')}
				</Button>
				<Button variant='primary' onClick={handleAutoTranscribePages} type='submit' className='modal__button--submit'>
					{t('modals.autoTranscribePages.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalAutoTranscribePages.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
	layers: PropTypes.array,
};

export default ModalAutoTranscribePages;

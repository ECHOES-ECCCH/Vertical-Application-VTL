import React, { useState } from "react";
import { useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { Modal } from "react-bootstrap";
import PropTypes from "prop-types";
import { useHistory } from "react-router-dom";

import api from "@/services/api";
import { alertActions } from "@/store/actions/alert.actions";
import { appActions } from "@/store/actions/app.actions";
import useOCRModelSelection from "@/utils/useOCRModelSelection";

import Button from "@Elements/Button/Button";
import Alert from "@Elements/Alert/Alert";
import CommonDropdownValue from '@Elements/Dropdown/CommonDropdownValue';
import OCRModelSelector from "@Elements/OCR/OCRModelSelector";
import HandleColumnsCheckbox from "@Elements/OCR/HandleColumnsCheckbox";

import styles from '@Elements/Modal/parts/Styles/ModalAutoTranscribePage.module.sass';

const ModalAutoTranscribePages = ({ handleOnClose, transcriptionId, layers }) => {
	const { t } = useTranslation();
	const dispatch = useDispatch();
	const history = useHistory();

	const selection = useOCRModelSelection(transcriptionId);
	const availableLayers = layers.filter(layer => layer.type !== "IIIF_ANNOTATION");

	const [selectedLayer, setSelectedLayer] = useState(availableLayers[0]?.id);
	const [rangeType, setRangeType] = useState(0);
	const [pageRange, setPageRange] = useState(null);
	const [selectedOCRMode, setSelectedOCRMode] = useState("OVERRIDE");
	const [handleColumns, setHandleColumns] = useState(false);

	const handleAutoTranscribePages = () => {
		dispatch(appActions.setLoading(true));

		let url = `/transcriptions/${transcriptionId}/ocr`
			+ `?model=${selection.encodedModels}`
			+ `&layerId=${selectedLayer}`
			+ `&ocrMode=${selectedOCRMode}`
			+ `&handleColumns=${handleColumns && selectedOCRMode !== "UPDATE"}`;

		if (rangeType === 1 && pageRange) {
			url += `&pages=${encodeURIComponent(pageRange)}`;
		}

		api.put(url)
			.then(() => {
				history.go(0);
				dispatch(appActions.setLoading(false));
				handleOnClose();
			})
			.catch(response => {
				dispatch(appActions.setLoading(false));
				dispatch(alertActions.setAlert({
					type: "danger",
					icon: "icon-circle-warning-empty",
					text: response?.data?.code,
					close: true,
				}));
			});
	};

	return (
		<>
			<Modal.Body>
				<Alert />
				<CommonDropdownValue
					label={t('modals.autoTranscribePages.select.selectLayer')}
					options={availableLayers.map(option => ({ label: option.name, value: option.id }))}
					selectedValue={selectedLayer}
					setSelectedValue={setSelectedLayer}
					className='dropdown-new'
				/><br />
				<div>
					<input
						id='autoTranscribe_radio1'
						type='radio'
						value={0}
						name='type'
						checked={rangeType === 0}
						onChange={() => setRangeType(0)}
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
						onChange={() => setRangeType(1)}
						className={styles['transcribepages--radio']}
					/>
					<label htmlFor='autoTranscribe_radio2'>{t('modals.autoTranscribePages.radio.pagesRange')}</label>
				</div>
				<div>
					<input
						name='pageRange'
						placeholder={t('modals.autoTranscribePages.input.placeholder')}
						className='form-control'
						disabled={rangeType === 0}
						type='text'
						onChange={e => setPageRange(e.target.value === '' ? null : e.target.value)}
					/>
					<label>{t('modals.autoTranscribePages.input.label')}</label>
				</div><br />
				<CommonDropdownValue
					label={t('modals.autoTranscribePages.select.titleRange')}
					options={[
						{ label: t('modals.autoTranscribePages.select.ocrMode.emptyonly'), value: "EMPTYONLY" },
						{ label: t('modals.autoTranscribePages.select.ocrMode.update'), value: "UPDATE" },
						{ label: t('modals.autoTranscribePages.select.ocrMode.override'), value: "OVERRIDE" },
					]}
					selectedValue={selectedOCRMode}
					setSelectedValue={setSelectedOCRMode}
					className='dropdown-new'
				/><br />
				<OCRModelSelector selection={selection} dropdownClassName='dropdown-new' /><br />
				{selection.isMultiSelect && (
					<HandleColumnsCheckbox
						checked={handleColumns}
						onChange={setHandleColumns}
						disabled={selectedOCRMode === "UPDATE"}
					/>
				)}
				<div className='modal__alert'>{t('modals.autoTranscribePages.notice')}</div><br />
			</Modal.Body>
			<Modal.Footer>
				<Button variant='secondary' onClick={handleOnClose} className='modal__button--cancel'>
					{t('form.cancel')}
				</Button>
				<Button
					variant='primary'
					onClick={handleAutoTranscribePages}
					type='submit'
					className='modal__button--submit'
					disabled={!selection.selectedModels.length}
				>
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
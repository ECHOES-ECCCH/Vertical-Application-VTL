import React, { useEffect, useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { Modal } from "react-bootstrap";
import PropTypes from "prop-types";

import api from "@/services/api";
import { viewerActions } from "@/store/actions/viewer.actions";

import Button from "@Elements/Button/Button";
import CommonDropdownValue from '@Elements/Dropdown/CommonDropdownValue';

import styles from '@Elements/Modal/parts/Styles/ModalAutoTranscribePage.module.sass';

const ModalAutoTranscribePage = ({ handleOnClose, transcriptionId, pageId, anyVerseVerified, viewMode, layers }) => {
	const { t } = useTranslation();

	const OCRProfiles = useSelector(state => state.app.OCRProfiles);
	const currentLang = useSelector(state => state.app.currentLang);

	const [selectedLayer, setSelectedLayer] = useState(layers[0].id);
	const [selectedOCRMode, setSelectedOCRMode] = useState(anyVerseVerified && viewMode === "transcribe" ? "UPDATE" : "OVERRIDE");
	const [handleColumns, setHandleColumns] = useState(false);
	const [selectedLangs, setSelectedLangs] = useState([OCRProfiles[0].code]);

	const dispatch = useDispatch();

	const handleAutoTranscribePage = () => {
		dispatch(
			viewerActions.submitAutoTranscribePage(transcriptionId, pageId, selectedLangs, selectedOCRMode, handleColumns, selectedLayer),
		);
	};

	const setSelectedLang = (index, lang) => {
		let langs = selectedLangs.slice();
		langs[index] = lang;
		setSelectedLangs(langs);
	};

	const addLang = () => {
		let langs = selectedLangs.slice();
		langs.push(OCRProfiles.find(p => !selectedLangs.includes(p.code)).code);
		setSelectedLangs(langs);
	};
	const removeLang = () => {
		let langs = selectedLangs.slice();
		langs.pop();
		setSelectedLangs(langs);
	};

	useEffect(() => {
		api.get(`transcriptions/${transcriptionId}/default-language`).then(({ data }) => {
			setSelectedLangs(data.split('+'));
		});
	}, []);

	return (
		<>
			<Modal.Body>
				<CommonDropdownValue
					label={t('modals.autoTranscribePage.body')}
					options={
						layers.filter(layer => layer.type !== "IIIF_ANNOTATION").map(option => { return {label: option.name, value: option.id }; })
					} 
					selectedValue={selectedLayer}
					setSelectedValue={ (value)=>{ setSelectedLayer(value); } }
					className='dropdown-new dropdown-viewer'
				/><br />
				<CommonDropdownValue
					label={t('modals.autoTranscribePage.select.titleRange')}
					options={[
						{label:t('modals.autoTranscribePage.select.ocrMode.update'), value: "UPDATE" },
						!(anyVerseVerified && viewMode === "transcribe") ? {label:t('modals.autoTranscribePage.select.ocrMode.override'), value: "OVERRIDE"} : [],
					]} 
					selectedValue={selectedOCRMode}
					setSelectedValue={ (value)=>{ setSelectedOCRMode(value); } }
					className='dropdown-new dropdown-viewer'
				/><br />
				<label>{ t('modals.autoTranscribePage.select.titleProfile') }</label>
				<div >
					{ selectedLangs.map((lang, index) => {
						return (
							<CommonDropdownValue
								key={ index }
								options={
									OCRProfiles
										.filter(item => lang === item.code || !selectedLangs.includes(item.code))
										.map(item => { return { label: item[currentLang] || item['en'], value: item.code }; })
								} 
								selectedValue={ lang }
								setSelectedValue={ (value)=>{ setSelectedLang(index, value); } }
								className='dropdown-new dropdown-viewer dropdown-language'
							/>
						);
					}) }
					<span style={{ display: 'inline-block' }}>
						<Button className='btn-sm' onClick={ addLang } disabled={ selectedLangs.length === OCRProfiles.length || selectedLangs.length >= 7 }>
							+
						</Button>
						&nbsp;&nbsp;
						<Button className='btn-sm' onClick={ removeLang } disabled={ selectedLangs.length === 1}>
							&minus;
						</Button>
					</span>
				</div><br />
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
						{t('modals.autoTranscribePage.checkbox.handleColumns')}
					</label>
				</div><br />
				<p>{t('modals.autoTranscribePage.select.asterisk')}</p>
			</Modal.Body>
			<Modal.Footer>
				<Button variant='secondary' onClick={handleOnClose} className='modal__button--cancel'>
					{t('form.cancel')}
				</Button>
				<Button variant='primary' onClick={handleAutoTranscribePage} className='modal__button--submit'>
					{t('modals.autoTranscribePage.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalAutoTranscribePage.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
	pageId: PropTypes.number,
	viewMode: PropTypes.string,
	anyVerseVerified: PropTypes.bool,
	layers: PropTypes.array,
};

export default ModalAutoTranscribePage;

import React, { useEffect, useState } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import api from "@/services/api";
import { viewerActions } from '@/store/actions/viewer.actions';

import Button from '@Elements/Button/Button';
import CommonDropdownValue from '@Elements/Dropdown/CommonDropdownValue';

const ModalAutoTranscribeVerse = ({ handleOnClose, transcriptionId }) => {
	const { t } = useTranslation();

	const verseActive = useSelector((state) => state.viewer.editor.verseActive);
	const verseGroupActive = useSelector((state) => state.viewer.editor.verseGroupActive);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const OCRProfiles = useSelector((state) => state.app.OCRProfiles);
	const currentLang = useSelector((state) => state.app.currentLang);
	const [selectedLangs, setSelectedLangs] = useState([OCRProfiles[0].code]);
	const dispatch = useDispatch();

	const handleAutoTranscribeVerse = () => {
		let versesToAutotranscribe = [];

		if (verseActive) {
			versesToAutotranscribe = [verseActive];
		} else if (verseGroupActive.firstVerseNo !== null && verseGroupActive.lastVerseNo !== null) {
			versesToAutotranscribe = verseGroupActive.activeVersesIds;
		}

		dispatch(viewerActions.submitAutoTranscribeVerses(transcriptionId, selectedLayer, versesToAutotranscribe, selectedLangs));
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
				<p dangerouslySetInnerHTML={{ __html: t('modals.autoTranscribeVerse.bodyHTML') }} />

				<label>{ t('modals.autoTranscribePage.select.titleProfile') }</label>
				<div>
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
				</div>
			</Modal.Body>
			<Modal.Footer>
				<Button
					variant='secondary'
					onClick={handleOnClose}
					className='modal__button--cancel'
				>
					{t('form.cancel')}
				</Button>
				<Button
					variant='primary'
					onClick={handleAutoTranscribeVerse}
					className='modal__button--submit'
				>
					{t('modals.autoTranscribeVerse.button')}
				</Button>
			</Modal.Footer>
		</>
	);
};

ModalAutoTranscribeVerse.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
};

export default ModalAutoTranscribeVerse;
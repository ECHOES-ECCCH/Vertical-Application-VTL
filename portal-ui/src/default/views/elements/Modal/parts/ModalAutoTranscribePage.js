import React, { useState } from "react";
import { useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { Modal } from "react-bootstrap";
import PropTypes from "prop-types";

import { viewerActions } from "@/store/actions/viewer.actions";
import useOCRModelSelection from "@/utils/useOCRModelSelection";

import Button from "@Elements/Button/Button";
import CommonDropdownValue from '@Elements/Dropdown/CommonDropdownValue';
import OCRModelSelector from "@Elements/OCR/OCRModelSelector";
import HandleColumnsCheckbox from "@Elements/OCR/HandleColumnsCheckbox";

const ModalAutoTranscribePage = ({ handleOnClose, transcriptionId, pageId, anyVerseVerified, viewMode, layers }) => {
	const { t } = useTranslation();
	const dispatch = useDispatch();

	const selection = useOCRModelSelection(transcriptionId);
	const availableLayers = layers.filter(layer => layer.type !== "IIIF_ANNOTATION");
	const overrideLocked = anyVerseVerified && viewMode === "transcribe";

	const [selectedLayer, setSelectedLayer] = useState(availableLayers[0]?.id);
	const [selectedOCRMode, setSelectedOCRMode] = useState(overrideLocked ? "UPDATE" : "OVERRIDE");
	const [handleColumns, setHandleColumns] = useState(false);

	const handleAutoTranscribePage = () => {
		dispatch(viewerActions.submitAutoTranscribePage(
			transcriptionId, pageId, selection.selectedModels, selectedOCRMode, handleColumns, selectedLayer,
		));
	};

	return (
		<>
			<Modal.Body>
				<CommonDropdownValue
					label={t('modals.autoTranscribePage.body')}
					options={availableLayers.map(option => ({ label: option.name, value: option.id }))}
					selectedValue={selectedLayer}
					setSelectedValue={setSelectedLayer}
					className='dropdown-new dropdown-viewer'
				/><br />
				<CommonDropdownValue
					label={t('modals.autoTranscribePage.select.titleRange')}
					options={[
						{ label: t('modals.autoTranscribePage.select.ocrMode.update'), value: "UPDATE" },
						...(!overrideLocked
							? [{ label: t('modals.autoTranscribePage.select.ocrMode.override'), value: "OVERRIDE" }]
							: []),
					]}
					selectedValue={selectedOCRMode}
					setSelectedValue={setSelectedOCRMode}
					className='dropdown-new dropdown-viewer'
				/><br />
				<OCRModelSelector selection={selection} dropdownClassName='dropdown-new dropdown-viewer' /><br />
				{selection.isMultiSelect && (
					<>
						<HandleColumnsCheckbox
							checked={handleColumns}
							onChange={setHandleColumns}
							disabled={selectedOCRMode === "UPDATE"}
						/><br />
					</>
				)}
			</Modal.Body>
			<Modal.Footer>
				<Button variant='secondary' onClick={handleOnClose} className='modal__button--cancel'>
					{t('form.cancel')}
				</Button>
				<Button
					variant='primary'
					onClick={handleAutoTranscribePage}
					className='modal__button--submit'
					disabled={!selection.selectedModels.length}
				>
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
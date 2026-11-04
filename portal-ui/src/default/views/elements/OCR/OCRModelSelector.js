import React from "react";
import PropTypes from "prop-types";
import { useTranslation } from "react-i18next";

import Button from "@Elements/Button/Button";
import CommonDropdownValue from "@Elements/Dropdown/CommonDropdownValue";

const capitalize = (s) => s.charAt(0).toUpperCase() + s.slice(1);

const OCRModelSelector = ({ selection, dropdownClassName = 'dropdown-new' }) => {
	const { t } = useTranslation();
	const {
		availableEngines, selectedEngine, changeEngine,
		engineModels, selectedModels, setSelectedModel,
		addModel, removeModel, isMultiSelect, canAddModel, canRemoveModel,
	} = selection;

	return (
		<>
			<CommonDropdownValue
				label={t('modals.autoTranscribe.select.engine', 'Engine')}
				options={availableEngines.map(engine => ({ label: capitalize(engine), value: engine }))}
				selectedValue={selectedEngine}
				setSelectedValue={changeEngine}
				className={dropdownClassName}
			/><br />
			<label>{t('modals.autoTranscribe.select.model')}</label>
			<div>
				{selectedModels.map((modelId, index) => (
					<CommonDropdownValue
						key={index}
						options={engineModels
							.filter(item => item.id === modelId || !selectedModels.includes(item.id))
							.map(item => ({ label: item.name, value: item.id }))}
						selectedValue={modelId}
						setSelectedValue={(value) => setSelectedModel(index, value)}
						className={`${dropdownClassName} dropdown-language`}
					/>
				))}
				{isMultiSelect && (
					<span style={{ display: 'inline-block' }}>
						<Button className='btn-sm' onClick={addModel} disabled={!canAddModel}>
							+
						</Button>
						&nbsp;&nbsp;
						<Button className='btn-sm' onClick={removeModel} disabled={!canRemoveModel}>
							&minus;
						</Button>
					</span>
				)}
			</div>
		</>
	);
};

OCRModelSelector.propTypes = {
	selection: PropTypes.object.isRequired,
	dropdownClassName: PropTypes.string,
};

export default OCRModelSelector;
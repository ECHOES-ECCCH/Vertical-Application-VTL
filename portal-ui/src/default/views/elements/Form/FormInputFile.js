import React from 'react';
import PropTypes from "prop-types";
import Form from "react-bootstrap/Form";
import { useTranslation } from 'react-i18next';

import styles from '@Elements/Form/Form.module.sass';

const FormInputFile = ({ register, name='file', errorMessage, label, chooseFile, formats, disableAfterValidFile = false }) => {
	const { t } = useTranslation();
	const buttonLabel = label ? label : t('form.inputFile.selectFile');
	const buttonChangeLabel = label ? label : t('form.inputFile.changeFile');
	const isEmptyObject = (obj) => {
		return Object.keys(obj).length === 0;
	};

	const blockClick = (e) => e.preventDefault();

	return (
		<Form.Row className={styles["form__input-row"]}>
			<div className='input-group'>
				<div className='custom-file'>
					<input
						name={name}
						type='file'
						className='custom-file-input'
						/* ref={ register({
							validate: {
								wrongFileType: inputFile => {
									if (isEmptyObject(inputFile)){
										return true;
									}
									const result = inputFile.length > 0 && Object.values(inputFile).every(
										file => formats.includes(file.name.slice(file.name.lastIndexOf('.')).toLowerCase()),
									);
									if (result && disableAfterValidFile) {
										const inputFileElement = document.getElementById('inputFile');
										const customFileLabelElement = document.getElementById('customFileLabel');

										customFileLabelElement.classList.add('custom-file-label-inactive');
										inputFileElement.classList.add('custom-file-input-inactive');

										customFileLabelElement.dataset.buttontext =t('form.inputFile.selectedFile');
										inputFileElement.onclick = blockClick;
									}
									return result;
								},
							},
						}) }*/
						accept={formats.join(',')}
						id='inputFile'
						{...register(name, {
							validate: {
								wrongFileType: inputFile => {
									if (isEmptyObject(inputFile)) {
										return true;
									}
									const result = inputFile.length > 0 && Object.values(inputFile).every(
										file => formats.includes(file.name.slice(file.name.lastIndexOf('.')).toLowerCase()),
									);
									if (result && disableAfterValidFile) {
										const inputFileElement = document.getElementById('inputFile');
										const customFileLabelElement = document.getElementById('customFileLabel');

										customFileLabelElement.classList.add('custom-file-label-inactive');
										inputFileElement.classList.add('custom-file-input-inactive');

										customFileLabelElement.dataset.buttontext = t('form.inputFile.selectedFile');
										inputFileElement.onclick = blockClick;
									}
									return result;
								},
							},
						})}
					/>
					<label
						className='custom-file-label'
						id='customFileLabel'
						htmlFor='inputFile'
						data-buttontext={(document.getElementById("inputFile") && document.getElementById("inputFile").files.length != 0) ? buttonChangeLabel : buttonLabel}
					>
						{chooseFile}
					</label>
				</div>
			</div>
			{errorMessage ? <div className={styles["form__message--invalid"]}><i className='icon-close' />{errorMessage}</div> : null}
		</Form.Row>
	);
};

FormInputFile.propTypes = {
	register: PropTypes.func,
	name: PropTypes.string,
	errorMessage: PropTypes.string,
	label: PropTypes.string,
	chooseFile: PropTypes.string,
	formats: PropTypes.array,
	disableAfterValidFile: PropTypes.bool,
};

export default FormInputFile;
import React from "react";
import PropTypes from "prop-types";
import { useTranslation } from "react-i18next";

import styles from '@Elements/Modal/parts/Styles/ModalAutoTranscribePage.module.sass';

const HandleColumnsCheckbox = ({ checked, onChange, disabled }) => {
	const { t } = useTranslation();

	return (
		<div>
			<input
				id='handleColumns'
				type='checkbox'
				checked={checked}
				onChange={e => onChange(e.target.checked)}
				disabled={disabled}
				className={styles['transcribepages--checkbox']}
			/>
			<label htmlFor='handleColumns' className={styles['transcribepages--label']}>
				{t('modals.autoTranscribe.checkbox.handleColumns')}
			</label>
		</div>
	);
};

HandleColumnsCheckbox.propTypes = {
	checked: PropTypes.bool,
	onChange: PropTypes.func,
	disabled: PropTypes.bool,
};

export default HandleColumnsCheckbox;
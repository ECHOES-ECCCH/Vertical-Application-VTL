import React, { useState, useEffect } from "react";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router';
import PropTypes from "prop-types";

import api from "@/services/api";

import styles from "@Pages/Transcription/TranscriptionPermissions/TranscriptionPermissions.module.sass";

import CommonDropdown from '@Elements/Dropdown/CommonDropdown';

const TranscriptionPermissionsStatus = ({ publicAvailable, dataLoaded }) => {
	const { t } = useTranslation();

	const { transcriptionId } = useParams();

	const [ dropdownOption, setDropdownOption ] = useState(0);
	const [ dropdownOptionStart, setDropdownOptionStart ] = useState(0);

	useEffect(() => {
		if (dataLoaded) {
			setDropdownOption(publicAvailable ? 0 : 1);
			setDropdownOptionStart(publicAvailable ? 0 : 1);
		}
        // eslint-disable-next-line react-hooks/exhaustive-deps
	}, [ dataLoaded, setDropdownOption, setDropdownOptionStart ]);

	useEffect(() => {
		if (dataLoaded && (dropdownOption !== dropdownOptionStart)) {
			api.put(`/transcriptions/${transcriptionId}/hidden`, {"hidden": (dropdownOption !== 0).toString()});
			setDropdownOptionStart(null);
		}
        // eslint-disable-next-line react-hooks/exhaustive-deps
	}, [ dropdownOption, dataLoaded ]);

	return (
		<div className={ styles["permissions__status"] }>
			<h2 className={ styles["permissions__title"] }>{t('transcription.permissions.objectAuthority')}</h2>
			<p className='text--small'>{t('transcription.permissions.status')}</p>

			<div className={ styles["permissions__dropdown--wrapper"] }>
				<CommonDropdown
					options={ [t('transcription.permissions.publicAvailability'),t('transcription.permissions.privateAvailability'),
					] }
					selectedOption={ dropdownOption }
					setSelectedOption={ setDropdownOption }
				/>
			</div>
		</div>
	);
};

TranscriptionPermissionsStatus.propTypes = {
	publicAvailable: PropTypes.bool,
	dataLoaded: PropTypes.bool,
};

export default TranscriptionPermissionsStatus;
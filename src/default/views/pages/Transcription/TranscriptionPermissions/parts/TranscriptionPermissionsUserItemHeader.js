import React from 'react';
import { useTranslation } from 'react-i18next';

import styles from "@Pages/Transcription/TranscriptionPermissions/TranscriptionPermissions.module.sass";

const TranscriptionPermissionsUserItemHeader = () => {
	const { t } = useTranslation();

	return (
		<div
			className={ styles["permissions__userlist--item-header"] }
		>
			<div className={ `${styles["permissions__userlist--item-order"]} ${styles["permissions__userlist--item-order--header"]}` }>{t('admin.dashboard.no')}</div>
			<div className={ `${styles["permissions__userlist--item-email"]} ${styles["permissions__userlist--item-email--header"]}` }>{t('admin.dashboard.email')}</div>
			<div className={ `${styles["permissions__userlist--item-role"]} ${styles["permissions__userlist--item-role--header"]}` }>{t('transcription.permissions.role') }</div>
			<div className={ `${styles["permissions__userlist--item-activity"]} ${styles["permissions__userlist--item-activity--header"]}` }>{t('transcription.permissions.activity') }</div>
		</div>
	);
};

export default TranscriptionPermissionsUserItemHeader;
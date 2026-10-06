import React from 'react';
import { useTranslation } from 'react-i18next';
import PropTypes from 'prop-types';

import styles from '@Pages/Transcription/TranscriptionPermissions/TranscriptionPermissions.module.sass';

const TranscriptionPermissionsProjectInfo = ({title, creator}) => {
	const { t } = useTranslation();

	return (
		<section className={styles["permissions__project-info"]}>
			<div className={styles["permissions__project-info--container"]}>
				<span className={styles["permissions__project-info--label"]}>{t('transcription.permissions.projectName')}</span>
				<span className={styles["permissions__project-info--title"]}>{ title }</span>
			</div>
			<div className={styles["permissions__project-info--container"]}>
				<span className={styles["permissions__project-info--label"]}>{t('transcription.permissions.author')}</span>
				<span className={styles["permissions__project-info--author"]}>{ creator }</span>
			</div>
			<br />
			<div>
				<span className={styles["permissions__project-info--label"]}>{t('transcription.permissions.pageInfo.firstLine')}</span>
				<ul className={styles["permissions__project-info--list"]}>
					<li>
						<span className={styles["permissions__project-info--title"]}>{t('transcription.permissions.pageInfo.userRole')}</span>
						<span className={styles["permissions__project-info--label"]}>{t('transcription.permissions.pageInfo.userRoleInfo')}</span>
					</li>
					<li>
						<span className={styles["permissions__project-info--title"]}>{t('transcription.permissions.pageInfo.modRole')}</span>
						<span className={styles["permissions__project-info--label"]}>{t('transcription.permissions.pageInfo.modRoleInfo')}</span>
					</li>
					<li>
						<span className={styles["permissions__project-info--title"]}>{t('transcription.permissions.pageInfo.ownerRole')}</span>
						<span className={styles["permissions__project-info--label"]}>{t('transcription.permissions.pageInfo.ownerRoleInfo')}</span>
					</li>
				</ul>
			</div>
		</section>
	);
};

TranscriptionPermissionsProjectInfo.propTypes = {
	title: PropTypes.string,
	creator: PropTypes.string,
};

export default TranscriptionPermissionsProjectInfo;
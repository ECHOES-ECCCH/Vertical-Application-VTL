import React, { useEffect } from "react";
import PropTypes from 'prop-types';
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';

import styles from "@Elements/Alert/Alert.module.sass";

import { alertActions } from '@/store/actions/alert.actions';

const Alert = ({ alertBoxName = "default", leaveAlert = false, className }) => {
	let alertState = useSelector((state) => state.alert);
	const dispatch = useDispatch();
	const { t } = useTranslation();

	useEffect(() => {
		if(alertState.target !== alertBoxName) { return; }
		if(!alertState.alreadyShown) { dispatch(alertActions.setAlertShown(alertState)); return; }
		if(!(leaveAlert || alertState.important)){
			dispatch(alertActions.clearAlert());
		}
	}, [alertState]);

	const onCloseAlert = () => {
		if (alertState.onClose)
			alertState.onClose();
		alertState = {};
		dispatch(alertActions.clearAlert());
	};

	const icon = alertState.icon?.length ? (
		<i className={ `${alertState.icon} ${styles.icon}` } />
	) : null;
	const closeIcon = (alertState.close) ? (
		<i
			className={ `icon-close ${styles.close}` }
			onClick={ onCloseAlert }
		/>
	) : null;

	return alertState.target === alertBoxName ? (
		<>
			{(alertState.text?.length || alertState.textHTML?.length) ? (
				<div className={ `${styles["alert--" + alertState.type]} ${className ? className : ''}` }>
					{icon}
					{ alertState.text?.length ? (
						<>
							{ t(alertState.text, alertState.textParams) }
							{ alertState.progress >= 0 && (' (' + alertState.progress + ' %)') }
						</>
					) : (
						<span dangerouslySetInnerHTML={ { __html: t(alertState.textHTML, alertState.textParams) } } />
					)}
					{closeIcon}
				</div>) : null }
		</>
	) : null;
};

Alert.propTypes = {
	alertBoxName: PropTypes.string,
	leaveAlert: PropTypes.bool,
	className: PropTypes.string,
};

export default Alert;

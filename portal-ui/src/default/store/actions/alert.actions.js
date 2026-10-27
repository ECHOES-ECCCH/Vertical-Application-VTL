function setAlert(alert) {
	if(alert.target === undefined) { alert.target = 'default'; }
	alert.alreadyShown = false;
	return { type: 'SET_ALERT', alert };
}

function setAlertShown(alert) {
	alert.alreadyShown = true;
	return { type: 'SET_ALERT_SHOWN', alert };
}

function clearAlert() {
	return { type: 'CLEAR_ALERT'};
}

export const alertActions = {
	setAlert,
	setAlertShown,
	clearAlert,
};

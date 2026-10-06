import alertInitialState from '@/store/initialState/alert.initialState';

export function alert(state = alertInitialState, action) {
	switch (action.type) {
		case 'SET_ALERT':
			return action.alert;
		case 'SET_ALERT_SHOWN':
			return action.alert;
		case 'CLEAR_ALERT':
			return alertInitialState;
		default:
			return state;
	}
}

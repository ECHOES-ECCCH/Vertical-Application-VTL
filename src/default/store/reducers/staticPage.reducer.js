import staticPageInitialState from '@/store/initialState/staticPage.initialState';

export function staticPage(state = staticPageInitialState, action) {
	switch (action.type) {
		case 'RECEIVE_CONTACT_PAGE':
			return {
				...state,
				contactPage: action.value,
			};
		default:
			return state;
	}
}
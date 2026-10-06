import modalInitialState from '@/store/initialState/modal.initialState';

export function modal(state = modalInitialState, action) {
	const { type, payload } = action;

	switch (type) {
		case 'SHOW_MODAL':
			return {
				show: true,
				...payload,
			};

		case 'HIDE_MODAL':
			return {};

		default:
			return state;
	}
}

import api from "@/services/api";

const receiveContactPage = (value) => ({
	type: "RECEIVE_CONTACT_PAGE",
	value,
});

const getContactPage = () => {
	return dispatch => {
		api.get('static-files/contact')
			.then(response => {
				dispatch(receiveContactPage(response.data));
			});
	};
};

export const staticPageActions = {
	getContactPage,
};
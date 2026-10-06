import { useCallback } from "react";
import { useDispatch, useSelector, shallowEqual } from "react-redux";

import { hideModal, showModal } from "@/store/actions/modal.actions";

import { alertActions } from "@/store/actions/alert.actions";

export const useModal = () => {
	return {
		modal: useSelector(state => state.modal, shallowEqual),
	};
};

export const useShowModal = props => {
	const dispatch = useDispatch();
	return useCallback(
		(params = {}) => {
			dispatch(showModal({ ...props, ...params }));
		},
		[dispatch, props],
	);
};

export const useHideModal = () => {
	const dispatch = useDispatch();
	const handleOnClose = useCallback(() => {
		dispatch(hideModal());
		dispatch(alertActions.clearAlert());
	}, [dispatch]);
	return {
		handleOnClose,
	};
};

import { showLoading, hideLoading } from "react-redux-loading-bar";

const loadingBarMiddleware = store => next => action => {
	if (action.isLoading === true) {
		store.dispatch(showLoading());
	} else if (action.isLoading === false) {
		store.dispatch(hideLoading());
	}

	return next(action);
};

export default loadingBarMiddleware;

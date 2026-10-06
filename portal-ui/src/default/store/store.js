/* eslint-disable no-underscore-dangle */
import { createStore, applyMiddleware, compose } from "redux";
import thunkMiddleware from "redux-thunk";

import rootReducer from "@/store/reducers";
import loadingBarMiddleware from "@/store/middleware/loadingBarMiddleware";
import cookieMiddleware from "@/store/middleware/cookieMiddleware";

let composeEnhancers = compose;

if (typeof window !== "undefined" && window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__) {
	composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__;
}

const store = createStore(
	rootReducer,
	compose(
		composeEnhancers(
			applyMiddleware(
				thunkMiddleware,
				cookieMiddleware,
				loadingBarMiddleware),
		),
	),
);
export default store;
import { combineReducers } from "redux";
import { loadingBarReducer } from "react-redux-loading-bar";

import { app } from '@/store/reducers/app.reducer';
import { alert } from '@/store/reducers/alert.reducer';
import { auth } from '@/store/reducers/auth.reducer';
import { modal } from '@/store/reducers/modal.reducer';
import { viewer } from '@/store/reducers/viewer.reducer';
import { staticPage } from "@/store/reducers/staticPage.reducer";

export default combineReducers({
	app,
	auth,
	alert,
	staticPage,
	viewer,
	modal,
	loadingBar: loadingBarReducer,
});

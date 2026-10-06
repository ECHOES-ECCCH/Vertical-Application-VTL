import Cookies from 'universal-cookie';
const cookies = new Cookies();
let stateFromCookie = true;

const cookieDataFromStore = (store) => {
	return {
		currentLang: store.getState().app.currentLang,
		cookiePermission: store.getState().app.cookiePermission,
		viewer: store.getState().app.viewer,
	};

};

const cookieMiddleware = store => next => action => {
	if (action.type === 'SAVE_COOKIE') {
		if (!cookies.get('appData')) { 
			cookies.set('appData', JSON.stringify(cookieDataFromStore(store)), { path: '/' });
		} else {
			if (JSON.stringify(store.getState().app) !== JSON.stringify(cookies.get('appData'))) {
				if (stateFromCookie) {
					action.payload = cookies.get('appData');
					next(action);
				}else{
					cookies.set('appData', JSON.stringify(cookieDataFromStore(store)), { path: '/' });
				}
			}
		}
		stateFromCookie = false;
	}
	else{
		next(action); 
	}
    
};

export default cookieMiddleware;
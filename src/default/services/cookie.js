import Cookies from 'universal-cookie';
const cookies = new Cookies();

const setToken = token => {
	return cookies.set('token', token, { path: '/', expires: new Date(Date.now() + (7 * 24 * 60 * 60 * 1000)) });
};

const getToken = () => {
	return cookies.get('token');
};

const removeToken = () => {	
	cookies.set('token', '', { path: '/', expires: new Date(1990,1,1) });
	cookies.remove('token');
};

export const cookieService = {
	setToken,
	getToken,
	removeToken,
};

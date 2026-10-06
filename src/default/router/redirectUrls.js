import url from "@/router/urls";

const hostname = window.location.origin;

export default {
	afterRegister: `${hostname}${url.auth.login}/`,
	passwordChanged: `${hostname}${url.auth.changePassword.send2mail}`,
};

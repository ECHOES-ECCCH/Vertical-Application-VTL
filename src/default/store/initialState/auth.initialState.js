const authInitialState = {
	credentials: {
		isLoggedIn: false,
		isLoggingEnd: false,
		id: null,
		securityGroup: "USER",
		token: null,
		csrf: null,
		name: '',
		email: '',
		hasPassword: null,
		quota: null,
		usedSpaceQuota: null,
		myGroups: [],
		otherGroups: [],
	},

	login: {
		isLoading: false,
		returnUrl: null,
	},

	path: {
		isProtected: false,
	},
};

export default authInitialState;

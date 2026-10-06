import authInitialState from '@/store/initialState/auth.initialState';
import jwtDecode from "jwt-decode";

export function auth(state = authInitialState, action) {
	switch (action.type) {
		case 'SET_AUTH_TOKEN':
			return {
				...state,
				credentials: {
					...state.credentials,
					token: action.token,
					email: jwtDecode(action.token).sub,
				},
			};

		case 'SET_AUTH_CSRFTOKEN':
			return {
				...state,
				credentials: {
					...state.credentials,
					csrf: action.token,
				},
			};
		
		case 'SET_AUTH_ISLOGGEDIN':
			return {
				...state,
				credentials: {
					...state.credentials,
					isLoggedIn: action.isLoggedIn,
				},
			};

		case 'SET_AUTH_ID':
			return {
				...state,
				credentials: {
					...state.credentials,
					id: action.id,
				},
			};

		case 'SET_AUTH_HAS_PASSWORD':
			return {
				...state,
				credentials: {
					...state.credentials,
					hasPassword: action.hasPassword,
				},
			};
		
		case 'SET_AUTH_QUOTA':
			return {
				...state,
				credentials: {
					...state.credentials,
					quota: action.quota,
				},
			};

		case 'SET_AUTH_USED_SPACE_QUOTA':
			return {
				...state,
				credentials: {
					...state.credentials,
					usedSpaceQuota: action.usedSpaceQuota,
				},
			};

		case 'SET_AUTH_MY_GROUPS':
			return {
				...state,
				credentials: {
					...state.credentials,
					myGroups: action.myGroups,
				},
			};
		
		case 'SET_AUTH_OTHER_GROUPS':
			return {
				...state,
				credentials: {
					...state.credentials,
					otherGroups: action.otherGroups,
				},
			};

		case 'SET_AUTH_NAME':
			return {
				...state,
				credentials: {
					...state.credentials,
					name: action.name,
				},
			};

		case 'SET_AUTH_EMAIL':
			return {
				...state,
				credentials: {
					...state.credentials,
					email: action.email,
				},
			};

		case 'SET_AUTH_SECURITYGROUP':
			return {
				...state,
				credentials: {
					...state.credentials,
					securityGroup: action.securityGroup,
				},
			};
	
		case 'SET_AUTH_SIGNOUT':
			return {
				...state,
				credentials: {
					...state.credentials,
					isLoggedIn: false,
					token: null,
					email: '',
				},
			};

		case 'SET_AUTH_ISLOGGINGEND':
			return {
				...state,
				credentials: {
					...state.credentials,
					isLoggingEnd: action.isLoggingEnd,
				},
			};

		case 'SET_AUTH_PATH_ISPROTECTED':
			return {
				...state,
				path: {
					...state.path,
					isProtected: action.isProtected,
				},
			};

		case 'SET_LOGIN_ISLOADING':
			return {
				...state,
				login: {
					...state.login,
					isLoading: action.value,
				},
			};

		case 'SET_LOGIN_RETURNURL':
			return {
				...state,
				login: {
					...state.login,
					returnUrl: action.value,
				},
			};

		case 'SET_LOGIN_ISNOTVALID':
			return {
				...state,
				login: {
					...state.login,
					isNotValid: action.value,
				},
			};

		default:
			return state;
	}
}
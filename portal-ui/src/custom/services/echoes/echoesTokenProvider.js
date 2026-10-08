import api from '@/services/api';

const normaliseAccessToken = (data) => {
	if (typeof data === 'string' && data.trim()) return data.trim();
	if (data?.accessToken) return data.accessToken;
	if (data?.access_token) return data.access_token;
	if (data?.token) return data.token;
	throw new Error('VTL did not return an access token for ECHOES');
};

export const getEchoesAccessToken = async () => {
	const response = await api.get('/oauth2/accessToken');
	return normaliseAccessToken(response.data);
};
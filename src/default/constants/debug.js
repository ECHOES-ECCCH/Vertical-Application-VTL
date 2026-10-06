const debug = (data) => {
	if (isDevelopment()) {
		console.log(JSON.stringify(data, null, 2));
	}
};

export default debug;

export const isDevelopment = () => process.env.NODE_ENV === 'development';
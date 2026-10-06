import React from "react";
import PropTypes from "prop-types";
import { useSelector } from 'react-redux';

const Spinner = ({stateFromStore, viewer}) => {
	const isLoading = useSelector((state) => state.app.isLoading);

	return (stateFromStore) ? 
		isLoading && (
			<div className={ `spinner ${ viewer ? 'spinner--black' : '' }` }>
				<div className='spinner__loading' />
			</div>
		) : (
			<div className={ `spinner ${ viewer ? 'spinner--black' : '' }` }>
				<div className='spinner__loading' />
			</div>
		);
};

Spinner.propTypes = {
	stateFromStore: PropTypes.bool,
	viewer: PropTypes.bool,
};

export default Spinner;
import React from 'react';
import PropTypes from "prop-types";

import styles from '@Elements/ProgressBar/ProgressBar.module.sass';

const ProgressBar = ({ progress }) => {
	return (
		<div className={ styles["progress__bar--wrapper"] }>
			<span
				className={
					progress === 100 ?
						styles["progress__bar--active"]
						:
						styles["progress__bar--meter"]
				}
				style={ { width: `${progress}%` } }
			/>
		</div>
	);
};

ProgressBar.propTypes = {
	progress: PropTypes.number,
};

export default ProgressBar;

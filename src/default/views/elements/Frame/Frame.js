import React from 'react';
import PropTypes from 'prop-types';

import styles from '@Elements/Frame/Frame.module.sass';

const Frame = ({title, text}) => {
	return (
		<section className={styles["frame__wrapper"]}>
			{title && 
			<h3>{title}</h3>}
			<div>
				{text}
			</div>
		</section>
	);
};

Frame.propTypes = {
	title: PropTypes.string,
	text: PropTypes.string,
};

export default Frame;

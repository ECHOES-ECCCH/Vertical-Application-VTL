import React from "react";
import PropTypes from 'prop-types';

import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";

const Error = ({ children }) => {
	return (
		<section>
			<Container>
				<Row>{children}</Row>
			</Container>
		</section>
	);
};
Error.propTypes = {
	children: PropTypes.oneOfType([ PropTypes.string, PropTypes.element ]),
};

export default Error;

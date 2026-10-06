import PropTypes from 'prop-types';

const Enum = function (value, name) {
	this.value = value;
	this.name = name;
};

Enum.prototype.toString = function () {
	return this.name;
};

Enum.prototype.valueOf = function () {
	return this.value;
};

Enum.propTypes = {
	value: PropTypes.number.isRequired,
	name: PropTypes.string.isRequired,
};

export default Enum;
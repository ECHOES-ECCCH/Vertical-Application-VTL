import React, { useEffect, useState } from "react";
import { useSelector } from 'react-redux';
import PropTypes from 'prop-types';
import { Prompt } from "react-router-dom";

import LoginConfirmBox from "@Elements/Modal/LoginConfirmBox";
import url from "@/router/urls";

export const RouteLeavingGuard = ({
	navigate,
	shouldBlockNavigation,
}) => {
	const [modalVisible, updateModalVisible] = useState(false);
	const [lastLocation, updateLastLocation] = useState();
	const [confirmedNavigation, updateConfirmedNavigation] = useState(false);
	const formsAutorization = useSelector((state) => state.app.config.authorization == "FORMS");
	const authorizationRegisterUrl = useSelector((state) => state.app.config.reactAppApi + state.app.config.authorizationRegisterUrl);

	const showModal = location => {
		updateModalVisible(true);
		updateLastLocation(location);
	};

	const closeModal = cb => {
		updateModalVisible(false);
		if (cb) {
			cb();
		}
	};

	const handleBlockedNavigation = nextLocation => {
		if (!confirmedNavigation && shouldBlockNavigation(nextLocation)) {
			showModal(nextLocation);
			return false;
		}
		return true;
	};
	const handleConfirmNavigationClick = () => {
		closeModal(() => {
			if (lastLocation) {
				updateConfirmedNavigation(true);
			}
		});
	};

	const handleConfirmRegisterClick = () => {
		closeModal(() => {
			if (lastLocation) {
				lastLocation.pathname = formsAutorization ? url.auth.register : authorizationRegisterUrl + "?returnUrl=" + encodeURIComponent(window.location);
				updateConfirmedNavigation(true);
			}
		});
	};
	useEffect(() => {
		if (confirmedNavigation) {
			navigate(lastLocation.pathname);
			updateConfirmedNavigation(false);
		}
	}, [confirmedNavigation]);

	return (
		<>
			<Prompt when message={handleBlockedNavigation} />
			<LoginConfirmBox
				visible={modalVisible}
				onCancel={closeModal}
				onConfirm={handleConfirmNavigationClick}
				onRegister={handleConfirmRegisterClick}
			/>
		</>
	);
};

RouteLeavingGuard.propTypes = {
	navigate: PropTypes.func,
	shouldBlockNavigation: PropTypes.func,
};

export default RouteLeavingGuard;

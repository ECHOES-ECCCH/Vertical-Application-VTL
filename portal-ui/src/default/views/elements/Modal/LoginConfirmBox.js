import React from "react";
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";

import '@Elements/Modal/LoginConfirmBox.sass';

import Button from '@Elements/Button/Button';

const LoginConfirmBox = ({
	visible,
	onCancel,
	onConfirm, 
	onRegister,
}) => {
	const { t } = useTranslation();

	const confirm = () => {
		onCancel();
		onConfirm();
	};

	const register = () => {
		onCancel();
		onRegister();
	};

	return (
		<>
			{visible && (
				<div className='modal_container'>
					<div className='inner_modal'>
						<div className='modal_body'>
							<h4>{t('auth.confirmBox.title')}</h4>
							<p>{t('auth.confirmBox.message')}</p>
						</div>
						<div className='modal_foot'>
							<Button
								variant='secondary'
								onClick={() => onCancel()}
								className='modal__button--cancel'
							>
								{t('form.cancel')}
							</Button>
							<Button
								variant='secondary'
								onClick={confirm}
								className='modal__button--cancel'
							>
								{t('auth.login.title')}
							</Button>
							<Button
								variant='primary'
								onClick={register}
							>
								{t('auth.confirmBox.register')}
							</Button>
						</div>
            
					</div>
				</div>
			)}
		</>
	);
};

LoginConfirmBox.propTypes = {
	visible: PropTypes.bool,
	onCancel: PropTypes.func,
	onConfirm: PropTypes.func, 
	onRegister: PropTypes.func,
};

export default LoginConfirmBox;

import React from "react";
import { Modal } from 'react-bootstrap';
import PropTypes from 'prop-types';

import Button from '@Elements/Button/Button';

const ModalSpinner = () => (
	<div className='modal-spinner'>
		<div className='modal-spinner__loading' />
	</div>
);

const ModalWrapperChildren = ({
	modalVisible = false,
	withClose = false,
	withFooter = false,
	setModalVisible,
	type,
	title,
	body,
	bodyHTML,
	closeButtonName,
	onClose,
	confirmButtonName,
	onConfirm,
	confirmButtonDisabled,
	children,
}) => {

	const onCloseModal = () => {
		setModalVisible(false);
	};

	const modalProps = {
		modalSpinner: ModalSpinner,
		setModalVisible,
		onCloseModal,
	};

	return (
		<Modal
			show={modalVisible}
			dialogClassName='modal-dialog-centered'
			className={`modal-${type ? type : 'default'}`}
			animation
			onHide={() => setModalVisible(false)}
		>
			{title &&
				<Modal.Header closeButton={withClose}>
					<Modal.Title>{title}</Modal.Title>
				</Modal.Header>}

			{bodyHTML && (
				<Modal.Body dangerouslySetInnerHTML={{ __html: bodyHTML }} />
			)}
			{body && (
				<Modal.Body>{body}</Modal.Body>
			)}

			{children && (
				typeof children === 'function' ? children(modalProps) : children
			)}

			{withFooter &&
				<Modal.Footer>
					{closeButtonName && (
						<Button
							variant='secondary'
							onClick={ onClose ? onClose : onCloseModal }
						>
							{closeButtonName}
						</Button>
					)}
					{confirmButtonName && (
						<Button
							variant='primary'
							onClick={onConfirm}
							className='modal__button--submit'
							disabled={confirmButtonDisabled}
						>
							{confirmButtonName}
						</Button>
					)}
				</Modal.Footer>}

		</Modal >
	);
};

ModalWrapperChildren.propTypes = {
	modalVisible: PropTypes.bool,
	setModalVisible: PropTypes.func,
	type: PropTypes.string,
	title: PropTypes.string,
	body: PropTypes.string,
	bodyHTML: PropTypes.string,
	withFooter: PropTypes.bool,
	withClose: PropTypes.bool,
	closeButtonName: PropTypes.string,
	onClose: PropTypes.func,

	confirmButtonName: PropTypes.string,
	onConfirm: PropTypes.func,
	confirmButtonDisabled: PropTypes.bool,

	children: PropTypes.oneOfType([
		PropTypes.element,
		PropTypes.func,
	]),
};

export default ModalWrapperChildren;
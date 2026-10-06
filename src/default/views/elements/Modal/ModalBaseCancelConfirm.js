import React from "react";
import { Modal } from 'react-bootstrap';
import PropTypes from 'prop-types';

import Button from '@Elements/Button/Button';

const ModalBaseCancelConfirm = ({
	modalVisible = false,
	setModalVisible,
	title,
	body,
	bodyHTML,
	cancelButtonName,
	confirmButtonName,
	onConfirm = () => { },
	withClose = false,
	type,
}) => {

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

			<Modal.Footer>
				{cancelButtonName && (
					<Button
						variant='secondary'
						onClick={() => setModalVisible(false)}
						className='modal__button--cancel'
					>
						{cancelButtonName}
					</Button>
				)}
				{confirmButtonName && (
					<Button
						variant='primary'
						onClick={onConfirm}
						className='modal__button--submit'
					>
						{confirmButtonName}
					</Button>
				)}
			</Modal.Footer>
		</Modal>
	);
};

ModalBaseCancelConfirm.propTypes = {
	modalVisible: PropTypes.bool,
	setModalVisible: PropTypes.func,
	title: PropTypes.string,
	body: PropTypes.string,
	bodyHTML: PropTypes.string,
	cancelButtonName: PropTypes.string,
	confirmButtonName: PropTypes.string,
	onConfirm: PropTypes.func,
	type: PropTypes.string,
	withClose: PropTypes.bool,
};

export default ModalBaseCancelConfirm;

{/* EXAMPLE USAGE:
<ModalWrapperChildren
	modalVisible={showModalRemoveTranscription}
	setModalVisible={setShowModalRemoveTranscription}
	title={t('modals.deleteTranscription.title')}
	bodyHTML={t('modals.deleteTranscription.bodyHTML')}
>
	{modalProps => (
		<ModalDeleteTranscription
			modalProps={modalProps}
			commitUnpublish={modalCommitRemoveTranscription}
		/>
	)}
</ModalWrapperChildren> 
*/}

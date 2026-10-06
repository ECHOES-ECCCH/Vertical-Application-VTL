import React, { Suspense, lazy, useEffect } from "react";
import { Modal } from 'react-bootstrap';
import { useLocation } from "react-router-dom";

import { useModal, useHideModal } from '@Elements/Modal/ModalHooks';

import Button from '@Elements/Button/Button';

const ModalSpinner = () => {
	return (
		<div className='modal-spinner'>
			<div className='modal-spinner__loading' />
		</div>
	);
};

const ModalWrapper = () => {
	const { modal } = useModal();
	const { handleOnClose } = useHideModal();
	
	const CustomComponent = lazy(() => import(`${modal.componentPath}`));

	return (
		<Modal 
			show={ modal.show } 
			dialogClassName='modal-dialog-centered' 
			className={ `modal-${ modal.type ? modal.type : 'default'}` } 
			animation 
			onHide={ handleOnClose }
		>
			{modal.title && 
			<Modal.Header closeButton={ modal.withClose }>
				<Modal.Title>{modal.title}</Modal.Title>
			</Modal.Header>}

			{modal.component}

			{modal.componentPath ? (
				<Suspense fallback={ <ModalSpinner /> }>
					<CustomComponent
						{ ...modal.componentProps }
						handleOnClose={ handleOnClose }
						ModalSpinner={ ModalSpinner }
					/>
				</Suspense>
			) : (
				<>
					{modal.bodyHTML ? (
						<Modal.Body dangerouslySetInnerHTML={ { __html: modal.bodyHTML } } />
					) : (
						<Modal.Body>{modal.body}</Modal.Body>
					)}
                    
					{modal.withFooter && 
					<Modal.Footer>
						<Button
							variant='primary'
							onClick={ handleOnClose }
						>
							{modal.actionButton}
						</Button>
					</Modal.Footer>}
				</>
			) }
		</Modal>
	);
};

export default ModalWrapper;
import React from "react";
import { useTranslation } from 'react-i18next';
import { Modal } from "react-bootstrap";
import PropTypes from "prop-types";

import Button from "@Elements/Button/Button";

const ModalExportTextLayers = ({handleOnClose, downloadFileFunction}) => {
	const { t } = useTranslation();

	return (
		<>
			<Modal.Body>
				<span>{t('viewer.header.chooseFormat')}</span><br /><br />
				<Button onClick={() => downloadFileFunction('hocr')} className='modal__button--option'>
					{t('viewer.header.downloadhOCR')}
				</Button>
				<Button onClick={() => downloadFileFunction('alto')} className='modal__button--option'>
					{t('viewer.header.downloadALTO')}
				</Button>
				<Button onClick={() => downloadFileFunction('plain-text')} className='modal__button--option'>
					{t('viewer.header.downloadText')}
				</Button>
				<Button onClick={() => downloadFileFunction('tei')} className='modal__button--option'>
					{t('viewer.header.downloadTEI')}
				</Button>
			</Modal.Body>
		</>
	);
};

ModalExportTextLayers.propTypes = {
	handleOnClose: PropTypes.func,
	downloadFileFunction: PropTypes.func,
};

export default ModalExportTextLayers;
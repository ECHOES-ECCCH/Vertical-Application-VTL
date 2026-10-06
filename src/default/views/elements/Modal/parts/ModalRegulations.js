import React, { useEffect, useState } from "react";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";
import Markdown from 'react-markdown';

import api from "@/services/api";

import Button from '@Elements/Button/Button';

import wlt_logo_pl from '@Assets/images/wlt_logo_pl.svg';
import wlt_logo_en from '@Assets/images/wlt_logo_en.svg';
import wlt_logo_de from '@Assets/images/wlt_logo_de.svg';

const ModalRegulations = ({ handleOnClose, ModalSpinner }) => {
	const { t } = useTranslation();

	const currentLang = useSelector((state) => state.app.currentLang);

	const [ regulationsText, setRegulationsText ] = useState(null);
	const [ regulationsLoading, setRegulationsLoading ] = useState(true);

	const wlt_logo = {
		'pl': wlt_logo_pl,
		'en': wlt_logo_en,
		'de': wlt_logo_de,
	};

	useEffect(() => {
		setRegulationsLoading(true);

		api.get('/static-files/regulations')
			.then(response => {
				setRegulationsLoading(false);
				setRegulationsText(response.data);
			});
	}, []);
	
	return (
		<>
			{ regulationsLoading ? ( 
				<ModalSpinner /> 
			) : (
				<>
					<Modal.Body>
						<img
							src={ wlt_logo[currentLang] }
							alt={t('headers.logoAlt') }
							className='modal-logo'
						/>
						{ regulationsText &&
						<Markdown>{ regulationsText[currentLang] }</Markdown> }
					</Modal.Body>
					<Modal.Footer>
						<Button
							variant='primary'
							onClick={ () =>  handleOnClose() }
						>
							{t('modals.regulations.button')}
						</Button>
					</Modal.Footer>
				</>
			)}
			
		</>
	);
};

ModalRegulations.propTypes = {
	handleOnClose: PropTypes.func,
	ModalSpinner: PropTypes.func,
};

export default ModalRegulations;
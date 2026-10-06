import React from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal, Form } from 'react-bootstrap';
import PropTypes from "prop-types";
import { useForm } from 'react-hook-form';

import api from "@/services/api";

import { appActions } from "@/store/actions/app.actions";
import { viewerActions } from "@/store/actions/viewer.actions";

import Button from '@Elements/Button/Button';
import FormInput from '@Elements/Form/FormInput';

const ModalHistoricalPageNumber = ({ handleOnClose, pageId }) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	const currentHistoricalNumber = useSelector((state) => state.viewer.data.teiPbValue);

	const {
		register,
		handleSubmit,
		formState,
	} = useForm({
		defaultValues: {
			historicalNumber: currentHistoricalNumber,
		},
		mode: "onChange",
	});

	const setHistoricalPageNumber = (data) => {
		dispatch(appActions.setLoading(true));

		api.put(`/pages/${pageId}/tei_pb_value`, data.historicalNumber, { headers: { 'Content-Type': 'text/plain' } })
			.then(() => {
				dispatch(appActions.setLoading(false));
				dispatch(viewerActions.setTeiPbValue(data.historicalNumber));
				handleOnClose();
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
			});
	};

	return (
		<form onSubmit={handleSubmit(setHistoricalPageNumber)}>
			<Modal.Body>
				<Form.Group controlId='historicalNumber'>
					<Form.Label>{t('modals.historicalPageNumber.label')}</Form.Label>
					<FormInput
						name='historicalNumber'
						register={ register }
						dict={ null }
						placeholder=''
					/>
				</Form.Group>
			</Modal.Body>
			
			<Modal.Footer>
				<Button
					variant='secondary'
					onClick={ () => {
						handleOnClose();
					} }
					className='modal__button--cancel'
				>
					{t('form.cancel')}
				</Button>
				<Button
					variant='primary'
					type='submit'
					disabled={ !formState.isValid }
					className='modal__button--submit'
				>
					{t('modals.historicalPageNumber.button')}
				</Button>
			</Modal.Footer>
		</form>
	);
};

ModalHistoricalPageNumber.propTypes = {
	handleOnClose: PropTypes.func,
	pageId: PropTypes.number,
};

export default ModalHistoricalPageNumber;
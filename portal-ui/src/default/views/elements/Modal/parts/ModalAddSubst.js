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

const ModalAddSubst = ({ handleOnClose, wordIds, transcriptionId, layerId }) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	const {
		register,
		handleSubmit,
		formState,
	} = useForm({
		defaultValues: {
			substId: "",
		},
		mode: "onChange",
	});

	const addSubstMark = (data) => {
		dispatch(appActions.setLoading(true));

		api.put(`/words/tei_elements`, {
			wordsIdentifiers: wordIds,
			teiElement: "SUBST",
			teiElementValue: data.substId,
		})
			.then((res) => {
				dispatch(appActions.setLoading(false));
				dispatch(viewerActions.reloadVerses(res.data.verses, transcriptionId));
				dispatch(viewerActions.reloadLayerBlocks(res.data.blocks, layerId));
				handleOnClose();
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
			});
	};

	return (
		<form onSubmit={handleSubmit(addSubstMark)}>
			<Modal.Body>
				<p dangerouslySetInnerHTML={ { __html:t('modals.addSubst.body1') } } />
				<p dangerouslySetInnerHTML={ { __html:t('modals.addSubst.body2') } } />
				<Form.Group controlId='substId'>
					<Form.Label>{t('modals.addSubst.label')}</Form.Label>
					<FormInput
						name='substId'
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
					{t('modals.addSubst.button')}
				</Button>
			</Modal.Footer>
		</form>
	);
};

ModalAddSubst.propTypes = {
	handleOnClose: PropTypes.func,
	wordIds: PropTypes.array,
	transcriptionId: PropTypes.number, 
	layerId: PropTypes.number,
};

export default ModalAddSubst;
import React from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal, Form } from 'react-bootstrap';
import PropTypes from "prop-types";
import { useForm } from 'react-hook-form';

import { viewerActions } from "@/store/actions/viewer.actions";

import Button from '@Elements/Button/Button';
import FormInputRange from '@Elements/Form/FormInputRange';

const ModalDeleteVerses = ({ handleOnClose, transcriptionId }) => {
	const { t } = useTranslation();

	const verses = useSelector((state) => state.viewer.data.verses);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);

	const dispatch = useDispatch();

	const {
		register,
		handleSubmit,
		setError,
		formState,
	} = useForm({
		defaultValues: {
			verses: "",
		},
		mode: "onChange",
	});

	const {
		errors,
	} = formState;

	const deleteVerses = (data) => {
		let tempVersesNo = [];
		let versesNo = [];
		let versesIds = [];
		let uniqueVersesIds = [];

		tempVersesNo = data.verses.split(",");
		tempVersesNo.forEach(
			(x) => {
				if (x.includes('-')) {
					const versesTab = x.split('-');
					for (let i = parseInt(versesTab[0]); i <= parseInt(versesTab[1]); i++) {
						versesNo.push(i);
					}
				} else {
					versesNo.push(parseInt(x));
				}
			},
		);

		versesNo.forEach(
			(x) => {
				const found = verses.find(y => y.verseNo === x && y.pageLayer.layerId === selectedLayer);
				if (found) {
					versesIds.push(parseInt(found.id));
				}
			},
		);

		uniqueVersesIds = versesIds.filter((item, pos) => {
			return versesIds.indexOf(item) == pos;
		});

		if (!versesIds.length || (versesIds.length !== versesNo.length)) {
			setError('verses', {type: 'wrongVerses', message: t('modals.deleteVerses.input.error')});
		} else {
			dispatch(viewerActions.deleteVerses(uniqueVersesIds, null, selectedLayer));
		}

	};

	return (
		<form onSubmit={handleSubmit(deleteVerses)}>
			<Modal.Body>
				<p dangerouslySetInnerHTML={ { __html:t('modals.deleteVerses.bodyHTML') } } />
				<Form.Group controlId='verses'>
					<Form.Label>{t('modals.deleteVerses.input.label')}</Form.Label>
					<FormInputRange
						name='verses'
						register={ register }
						dict={ null }
						placeholder={t('modals.deleteVerses.input.placeholder') }
						errorMessage={ errors.verses && errors.verses.message ? errors.verses.message : '' }
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
					disabled={ !formState.isDirty || !formState.isValid }
					className='modal__button--submit'
				>
					{t('modals.deleteVerses.button')}
				</Button>
			</Modal.Footer>
		</form>
	);
};

ModalDeleteVerses.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
};

export default ModalDeleteVerses;
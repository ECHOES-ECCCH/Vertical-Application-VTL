import React from "react";
import { useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";
import { useForm } from 'react-hook-form';

import api from "@/services/api";
import { appActions } from "@/store/actions/app.actions";
import FormInput from "@Elements/Form/FormInput";
import FormSubmit from "@Elements/Form/FormSubmit";
import Button from '@Elements/Button/Button';

import styles from "@Elements/Modal/parts/LayersActions/ModalEditLayer.module.sass";

const ModalEditLayer = ({ handleOnClose, layer, onEditComplete }) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();
	const {
		register,
		handleSubmit,

		formState: {
			errors,
		},
	} = useForm({
		mode: 'onSubmit',
		reValidateMode: 'onChange',
		defaultValues: {
			name: layer ? layer.name : "",
			description: layer ? layer.description : "",
		},
	});

	const onSubmit = (data) => {
		onEditComplete({ ...data, id: layer.id });
	};
	// dispatch(appActions.setLoading(true));

	// const dataToSend = {
	// 	transcriptionId: transcriptionId,
	// 	name: data.name,
	// 	description: data.description,
	// };

	// api.put(`/transcriptions/${transcriptionId}/layer/${layer.id}`, dataToSend)
	// 	.then(() => {
	// 		dispatch(appActions.setLoading(false));
	// 		handleOnClose();
	// 		if (onComplete)
	// 			onComplete();

	// 	})
	// 	.catch(() => {
	// 		dispatch(appActions.setLoading(false));
	// 	});
	// };

	return (
		<form
			className={styles["form"]}
			onSubmit={handleSubmit(onSubmit)}
		>
			<Modal.Body>

				<FormInput
					name='name'
					register={register}
					isRequired
					label={t('transcriptions.layers.layerName')}
					validateData={
						{
							validate: value => value !== '' || t('modals.addNewLayer.addNew.form.nameRequired'),
						}
					}
					errorMessage={errors.name && errors.name.message}
				/>
				<FormInput
					name='description'
					register={register}
					isRequired
					label={t('transcriptions.layers.layerDescription')}
					sublabel={t('transcriptions.layers.layerDescriptionSub')}
				/>

				<p className={styles["form--info"]}><span className='asterisk'>*</span>{t('transcriptions.layers.info')}</p>

			</Modal.Body>
			<Modal.Footer>
				<Button
					variant='secondary'
					onClick={handleOnClose}
					className='modal__button--cancel'
				>
					{t('form.cancel')}
				</Button>
				<FormSubmit
					label={t('form.save')}
					className='block__button--submit'
					variant='primary'
				/>
			</Modal.Footer>
		</form>
	);
};

ModalEditLayer.propTypes = {
	handleOnClose: PropTypes.func,
	layer: PropTypes.shape({
		id: PropTypes.number,
		name: PropTypes.string,
		description: PropTypes.string,
	}),
	onEditComplete: PropTypes.func,
};

export default ModalEditLayer;
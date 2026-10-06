import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useTranslation } from 'react-i18next';
import { Form, Modal } from "react-bootstrap";
import PropTypes from "prop-types";
import { useForm } from "react-hook-form";

import api from "@/services/api";
import { appActions } from "@/store/actions/app.actions";
import { viewerActions } from "@/store/actions/viewer.actions";

import { useShowModal } from "@Elements/Modal/ModalHooks";

import { CheckViewModePermission } from "@/permissions/ViewModes";

import Button from "@Elements/Button/Button";
import FormTextarea from "@Elements/Form/FormTextarea";

const ModalCommentsVerse = ({ handleOnClose, ModalSpinner, verseId, addBox = false, verified, transcriptionId, pageId }) => {
	const { t } = useTranslation();

	const viewMode = useSelector((state) => state.viewer.data.viewMode);
	const accountEmail = useSelector((state) => state.auth.credentials.email);
	const textareaLimit = 2000;
	const dispatch = useDispatch();

	const [ comments, setComments ] = useState({});
	const [ commentsAdd, setCommentsAdd ] = useState(addBox);
	const [ commentsLoading, setCommentsLoading ] = useState(true);

	const { register, handleSubmit, setError, formState, watch } = useForm({
		defaultValues: {
			content: "",
		},
		mode: "onChange",
	});

	useEffect(() => {
		setCommentsLoading(true);
		api.get(`/comments/${verseId}`).then(({ data }) => {
			setCommentsLoading(false);
			setComments(data);
		});
	}, [ verseId ]);

	const handleCommentsAdd = (data) => {
		dispatch(appActions.setLoading(true));
		api.post(`/comments/${verseId}`, data).then((response) => {
			dispatch(appActions.setLoading(false));
			dispatch(viewerActions.updateVerseCommentsCount(verseId, response.data));
			handleOnClose();
		}).catch((er) => {
			console.error(er);
			dispatch(appActions.setLoading(false));
			setError("content", {type: "wrong", message: t('modals.commentsVerse.failed')});
		});
	};

	const modalCommentsDelete = useShowModal({
		title:t('modals.commentsDelete.title'),
		type: "viewer",
		name: "commentsDelete",
		componentPath: "./parts/ModalCommentsDelete",
		componentProps: { verseId: parseInt(verseId), transcriptionId: transcriptionId, pageId: pageId },
	});

	const contentLimit = watch("content");

	return (
		<>
			{commentsLoading ? (
				<ModalSpinner />
			) : (
				<Modal.Body>
					<div className='modal__comment--title'>
						<h3 className='modal__text--comment-title'>{t('modals.commentsVerse.commentsInAVerse')} ({comments.length})</h3>
						{(!commentsAdd && (!verified || CheckViewModePermission("canAddComments", viewMode))) &&
						<Button
							variant='link'
							onClick={() => setCommentsAdd(true)}
							className='modal__button--addcomment'
						>
							<i className='icon-plus2' />
							{t('modals.commentsVerse.button')}
						</Button>}
					</div>
					{commentsAdd &&
					<div className='modal__comment--addbox'>
						<form onSubmit={handleSubmit(handleCommentsAdd)}>
							<div className='modal__comment--content'>
								<Form.Group>
									<FormTextarea
										name='content'
										register={register}
										label={t('modals.commentsVerse.label')}
										maxLength={textareaLimit}
									/>
								</Form.Group>
							</div>
							<div className='modal__comment--buttons'>
								<div
									className='modal__comment--limit'
								>{t('modals.commentsVerse.limit')}: {contentLimit.length}/{textareaLimit} 
								</div>
								<Button
									variant='secondary'
									onClick={ () => { handleOnClose(); } }
									className='modal__button--cancel'
								>
									{t('form.cancel')}
								</Button>
								<Button
									variant='primary'
									type='submit'
									disabled={!formState.isDirty}
								>
									{t('modals.commentsVerse.button')}
								</Button>
							</div>
						</form>
					</div>}
					{comments.length > 0 &&
					<div className='modal__comment--list'>
						{commentsAdd &&
						<h3 className='modal__text--comment-title'>{t('modals.commentsVerse.previousComments')}</h3>}

						{comments.map((item) => (
							<div
								key={item.id}
								className='modal__comment--item'
							>
								<div className='modal__comment--upper'>
									<div className='modal__comment--date'>{item.dateCreated}</div>
									<div className='modal__comment--email'>{item.creator.email}</div>
									{(CheckViewModePermission("canRemoveAnyComments", viewMode) || accountEmail === item.creator.email) &&
									<div className='modal__comment--delete'>
										<Button
											variant='link'
											onClick={() => {
												dispatch(appActions.setModalCommentsDeleteCommentId(item.id));
												modalCommentsDelete();
											}}
										>
											{t('modals.commentsVerse.delete')}
										</Button>
									</div>}
								</div>
								<div className='modal__comment--lower'>
									{item.content}
								</div>
							</div>
						))}
					</div>}
				</Modal.Body>
			)}

		</>
	);
};

ModalCommentsVerse.propTypes = {
	handleOnClose: PropTypes.func,
	verseId: PropTypes.number,
	ModalSpinner: PropTypes.func,
	addBox: PropTypes.bool,
	verified: PropTypes.bool,
	transcriptionId: PropTypes.number,
	pageId: PropTypes.number,
};

export default ModalCommentsVerse;
/* eslint-disable react/prop-types */
import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { useTranslation } from 'react-i18next';
import { Modal } from "react-bootstrap";
import PropTypes from "prop-types";

import { useShowModal } from "@Elements/Modal/ModalHooks";

import { CheckViewModePermission } from "@/permissions/ViewModes";

import Button from "@Elements/Button/Button";

const ModalListOfIndex = ({ transcriptionId, verseId, wordId, indexes, type }) => {

	const { t } = useTranslation();

	const viewMode = useSelector(state => state.viewer.data.viewMode);
	const [clickedIndex, setClickedIndex] = useState(null);

	const [actionType, setActionType] = useState(null);
	const modalSetVerseAsIndex = useShowModal({
		title: type === "person" ? t('modals.setAsPerson.titleEdit')
			: type === "place" ? t('modals.setAsPlace.titleEdit')
				:t('modals.setAsTerm.titleEdit'),
		type: "viewer modal-index",
		name: "PersonVerse",
		componentPath: "./parts/ModalSetVerseAsIndex",
		componentProps: {
			transcriptionId: transcriptionId,
			verseId: verseId,
			wordId: wordId,
			indexType: type,
			indexId: parseInt(clickedIndex),
		},
	});

	const modalIndexesDelete = useShowModal({
		title:t('modals.listOfIndex.deleteIndexTitle'),
		type: "viewer",
		name: "indexDelete",
		componentPath: "./parts/ModalIndexesDelete",
		componentProps: {indexId: clickedIndex, verseId: verseId, wordId: wordId},
	});

	useEffect(() => {
		if (actionType === "edit") {
			if (clickedIndex !== null) {
				modalSetVerseAsIndex();
			}
		}
		else if (actionType === "delete") {
			if (clickedIndex !== null) {
				modalIndexesDelete();
			}
		}
	}, [actionType]);

	const IndexActions = ({index}) => {
		return (
			<div className='modal__index__content__actions'>
				<div className='modal__index__content__actions--edit'>
					<Button
						variant='link'
						onClick={() => {
							setClickedIndex(index.id);
							setActionType('edit');
						}}
					>
						{t('modals.listOfIndex.edit')}
					</Button>
				</div>
	
				<div className='modal__index__content__actions--delete'>
					<Button
						variant='link'
						onClick={() => {
							setClickedIndex(index.id);
							setActionType('delete');
						}}
					>
						{t('modals.listOfIndex.delete')}
					</Button>
				</div>
			</div>
		);
	};

	IndexActions.propTypes = {
		index: PropTypes.object, 
	};

	return (
		<Modal.Body>
			<section className='modal__index'>
				<div className='modal__index__content'>
					{type === 'person' && 
					<>
						{indexes
							.filter(item => item.type === type)
							.map(index => (
								<div key={index.id}>
									<hr className='modal__index--divider' />
									<div className='modal__index__content__item'>
										<div>
											<span className='modal__index__content__item--value-name'>{t('modals.listOfIndex.personName')}: </span>
											<span className='modal__index__content__item--value-value'>{index.name}</span>
										</div>
										{CheckViewModePermission("canOperateOnIndexes", viewMode) && <IndexActions index={index} />}
									</div>
								</div>
							))}
					</>}
					{type === 'place' && 
					<>
						{indexes
							.filter(item => item.type === type)
							.map(index => (
								<div key={index.id}>
									<hr className='modal__index--divider' />
									<div className='modal__index__content__item'>
										<div>
											<span className='modal__index__content__item--value-name'>{t('modals.listOfIndex.placeName')}: </span>
											<span className='modal__index__content__item--value-value'>{index.name}</span>
										</div>
										{CheckViewModePermission("canOperateOnIndexes", viewMode) && (
											<IndexActions index={index} />
										)}
									</div>
									<div className='modal__index__content__item'>
										<div>
											<span className='modal__index__content__item--value-name'>{t('modals.listOfIndex.urlAddress')}: </span>
											<span className='modal__index__content__item--value-value'>{index.accessibleOn ? <a href={index.accessibleOn}>{index.accessibleOn}</a> :t('modals.listOfIndex.notAccessible')}</span>
										</div>
									</div>
								</div>
							))}
					</>}
					{type === 'term' && 
					<>
						{indexes
							.filter(item => item.type === type)
							.map(index => (
								<div key={index.id}>
									<hr className='modal__index--divider' />
									<div className='modal__index__content__item'>
										<div>
											<span className='modal__index__content__item--value-name'>{t('modals.listOfIndex.termName')}: </span>
											<span className='modal__index__content__item--value-value'>{index.name}</span>
										</div>
										{CheckViewModePermission("canOperateOnIndexes", viewMode) && (
											<IndexActions index={index} />
										)}
									</div>
									<div className='modal__index__content__item'>
										<div>
											<span className='modal__index__content__item--value-name'>{t('modals.listOfIndex.termMeaning')}: </span>
											<span className='modal__index__content__item--value-value'><br />{index.description}</span>
										</div>
									</div>
								</div>
							))}
					</>}
				</div>
			</section>
		</Modal.Body>
	);
};

ModalListOfIndex.propTypes = {
	transcriptionId: PropTypes.number,
	verseId: PropTypes.number,
	wordId: PropTypes.object,
	indexes: PropTypes.arrayOf(PropTypes.object),
	type: PropTypes.string,
};

export default ModalListOfIndex;

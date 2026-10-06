import React from "react";
import PropTypes from "prop-types";
import { useTranslation } from 'react-i18next';
import { useParams } from "react-router";

import { useShowModal } from "@Elements/Modal/ModalHooks";

import Button from "@Elements/Button/Button";

const VerseIndex = ({ verseId, wordId, indexes }) => {
	const { t } = useTranslation();

	const { transcriptionId } = useParams();
	
	const terms = indexes.filter(index => index.type === 'term');
	const places = indexes.filter(index => index.type === 'place');
	const persons = indexes.filter(index => index.type === 'person');

	const showModalListOfIndexTerms = useShowModal({
		title:t('modals.listOfIndex.title.terms') + " (" + terms.length + ")",
		type: "viewer modal-index",
		name: "ModalListOfIndex",
		componentPath: "./parts/ModalListOfIndex",
		componentProps: { transcriptionId: parseInt(transcriptionId), verseId: verseId, wordId: wordId, indexes: terms, type: 'term' },
		withClose: true,
	});

	const showModalListOfIndexPlaces = useShowModal({
		title:t('modals.listOfIndex.title.places') + " (" + places.length + ")",
		type: "viewer modal-index",
		name: "ModalListOfIndex",
		componentPath: "./parts/ModalListOfIndex",
		componentProps: { transcriptionId: parseInt(transcriptionId), verseId: verseId, wordId: wordId, indexes: places, type: 'place' },
		withClose: true,
	});

	const showModalListOfIndexPersons = useShowModal({
		title:t('modals.listOfIndex.title.persons') + " (" + persons.length + ")",
		type: "viewer modal-index",
		name: "ModalListOfIndex",
		componentPath: "./parts/ModalListOfIndex",
		componentProps: { transcriptionId: parseInt(transcriptionId), verseId: verseId, wordId: wordId, indexes: persons, type: 'person' },
		withClose: true,
	});
	
	return (
		<>
			<div className='verse__comments'>
				{terms.length > 0 && (
					<Button
						className='verse__comments--button'
						variant='verseinfo'
						onClick={() => showModalListOfIndexTerms()}
					>
						<i className='icon-index-term' />
						<span className='verse__comments--count'>{terms.length}</span>
					</Button>
				)}
			</div>
			<div className='verse__comments'>
				{places.length > 0 && (
					<Button
						className='verse__comments--button'
						variant='verseinfo'
						onClick={() => showModalListOfIndexPlaces()}
					>
						<i className='icon-index-place' />
						<span className='verse__comments--count'>{places.length}</span>
					</Button>
				)}
			</div>
			<div className='verse__comments'>
				{persons.length > 0 && (
					<Button
						className='verse__comments--button'
						variant='verseinfo'
						onClick={() => showModalListOfIndexPersons()}
					>
						<i className='icon-index-person' />
						<span className='verse__comments--count'>{persons.length}</span>
					</Button>
				)}
			</div>
		</>
	);
};

VerseIndex.propTypes = {
	verseId: PropTypes.number,
	wordId: PropTypes.number,
	indexes: PropTypes.arrayOf(PropTypes.object),
};

export default VerseIndex;

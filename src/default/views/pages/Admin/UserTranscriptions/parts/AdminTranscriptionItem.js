import React from "react";
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";

import Button from "@Elements/Button/Button";

import styles from '@Pages/Admin/UserTranscriptions/UserTranscriptions.module.sass';

const AdminTranscriptionItem = ({ owner, orderNum, id, title, authors, deleteTranscription, openTranscription }) => {
	const { t } = useTranslation();

	return (
		<div className={ styles["transcription-list__item"] }>
			<div className={ styles["transcription-list__item--order"] }>
				{orderNum + 1}.
			</div>
			<div className={ styles["transcription-list__item--project"] }>
				<Button
					variant='link'
					type='button'
					onClick={ () => openTranscription(id) }
					className={ styles["transcription-list__item--btn-left"] + " " + styles["transcription-list__item--link"] }
				>
					{title}
				</Button>
			</div>
			<div className={ styles["transcription-list__item--authors"] }>
				
				{authors.length > 1 ? (authors.map((item)=>{
					return item.email == owner ? "" : <span className={ styles["transcription-list__item--single-author"] } key={`${item.id}`}>{item.email}; </span>;
				})) : <span>{t('admin.userTranscriptions.none')}</span>}
			</div>

			<div className={ styles["transcription-list__item--buttons"] }>
				<Button
					variant='tetriary'
					type='button'
					className={ styles["transcription-list__item--btn"] }
					onClick={ () => deleteTranscription(id) }
				>
					{t('admin.userTranscriptions.delete')}
				</Button>
			</div>
		</div>
	);
};

AdminTranscriptionItem.propTypes = {
	owner: PropTypes.string,
	id: PropTypes.number,
	orderNum: PropTypes.number,
	title: PropTypes.string,
	authors: PropTypes.array,
	deleteTranscription: PropTypes.func,
	openTranscription: PropTypes.func,
};

export default AdminTranscriptionItem;
import React, { useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { useParams} from "react-router";
import { reverse } from 'named-urls';
import PropTypes from "prop-types";

import url from "@/router/urls";
import styles from "@Pages/Transcription/TranscriptionVerification/TranscriptionVerification.module.sass";

const TranscriptionVerificationItem = ({verification}) => {
	
	const { t } = useTranslation();

	const { transcriptionId } = useParams();
	const imageRef = useRef(null);

	const handleImageOnError = () => {
		imageRef.current.className = styles["verification__table--item-img-error"];
	};	

	return (
		<li className={ styles["verification__table--item"] }>
			<span className={ styles["verification__table--item-lp"] }>
				{verification.pageNo}
			</span>
			<div className={ styles["verification__table--item-img-container"] }>
				<div 
					className={ styles["verification__table--item-img"] }
					style={ { backgroundImage: `url(${verification.urlToThumbnail})` } }
					ref={ imageRef }
				>
					{verification.urlToThumbnail && 
						<img 
							src={ verification.urlToThumbnail }
							alt={ verification.filename }
							onError={ handleImageOnError }
						/>}
					<i className='icon-image-1' />
				</div>
			</div>
			<div className={ styles["verification__table--item-name-container"] }>
				<span className={ styles["verification__table--item-label"] }>
					{t('transcription.verification.table.name')}
				</span>
				<Link 
					to={ reverse(url.transcription.viewer, { transcriptionId: parseInt(transcriptionId), pageId: parseInt(verification.pageId) }) }
					className={ styles["verification__table--item-name"] }
				>
					{verification.filename}
				</Link>
			</div>
			<div className={ styles["verification__table--item-default"] }>
				<span className={ styles["verification__table--item-label"] }>
					{t('transcription.verification.table.versesNumber')}
				</span>
				{verification.versesInFile}
			</div>
			<div className={ styles["verification__table--item-default"] }>
				<span className={ styles["verification__table--item-label"] }>
					{t('transcription.verification.table.verified')}
				</span>
				{verification.verifiedVerses}
			</div>
			<div className={ styles["verification__table--item-default"] }>
				<span className={ styles["verification__table--item-label"] }>
					{t('transcription.verification.table.correct')}
				</span>
				{verification.versesMarkedAsVerified}
			</div>
			<div className={ styles["verification__table--item-default"] }>
				<span className={ styles["verification__table--item-label"] }>
					{t('transcription.verification.table.incorrect')}
				</span>
				{verification.versesMarkedAsIncorrect}
			</div>
			<div className={ styles["verification__table--item-edition"] }>
				<span className={ styles["verification__table--item-label"] }>
					{t('transcription.verification.table.lastEdition')}
				</span>
				<div className={ styles["verification__table--item-edition-container"] }>
					<span className={ styles["verification__table--item-edition-email"] }>
						{verification.lastModifier}
					</span>
					<span className={ styles["verification__table--item-edition-date"] }>
						{verification.lastModificationDate}
					</span>
				</div>
			</div>
			<Link 
				className={ styles["verification__table--item-verification"] }
				to={ reverse(url.transcription.viewer, { transcriptionId: parseInt(transcriptionId), pageId: parseInt(verification.pageId) }) }
			>
				{t('transcription.verification.table.verify')}
			</Link>
		</li>
	);
};

TranscriptionVerificationItem.propTypes = {
	verification: PropTypes.object,
};

export default TranscriptionVerificationItem;

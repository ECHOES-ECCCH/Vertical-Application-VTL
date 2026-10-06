import React from 'react';
import { Link } from "react-router-dom";
import { reverse } from "named-urls";
import PropTypes from "prop-types";

import styles from "@Pages/Transcription/TranscriptionIndexes/TranscriptionIndexes.module.sass";

import url from '@/router/urls';

function TranscriptionIndexesItem({index, transcriptionId}) {
	let i = 0;
	return (
		<>
			<div className={ styles["indexes__list--item-name"] } >
				{index.name}
			</div>
			<div className={ styles["indexes__list--item-pages"] } >   
				{ index.type == "term" && <p style={{width: '100%', fontSize: '1rem'}}>{index.description}</p> }
				{Array.from(index.countedPages).map(([id, page]) => {
					i += 1;
					const link = reverse(url.transcription.viewer, {transcriptionId: transcriptionId, pageId: page.id});
					return (
						<Link key={i} to={link} className={styles["indexes__list--item-link"]}>{`Plik ${page.pageNo}`} <span className={styles["indexes__list--item-count"]}>({page.count});</span></Link>
					);
				})}     
			</div>
		</> 
	);
}

TranscriptionIndexesItem.propTypes = {
	index: PropTypes.object,
	transcriptionId: PropTypes.number,
};

export default TranscriptionIndexesItem;

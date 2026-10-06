import React, { useState, useEffect } from "react";
import PropTypes from "prop-types";

import TranscriptionIndexesItemsLetterIndicator from '@Pages/Transcription/TranscriptionIndexes/parts/TranscriptionIndexesItemsLetterIndicator';
import TranscriptionIndexesItem from '@Pages/Transcription/TranscriptionIndexes/parts/TranscriptionIndexesItem';
import styles from "@Pages/Transcription/TranscriptionIndexes/TranscriptionIndexes.module.sass";

function TranscriptionIndexesItemsByTag({indexes,transcriptionId}) {
    
	const [letters, setLetters] = useState(new Map());
    
	useEffect(() => {
		let lettersMap = new Map();
		indexes.map(index => {
			let pagesMap = new Map();
			index.pages.map(page => {
				if(!pagesMap.has(+page.id)){
					pagesMap.set(+page.id, { ...page, count: 1 });
				}else{
					pagesMap.get(+page.id).count += 1;
				}
			});

			index.countedPages = pagesMap;
			if (!lettersMap.has(index.name[0])) {
				lettersMap.set(index.name[0], [index]);
			}else{
				lettersMap.get(index.name[0]).push(index);
			}
		});
		setLetters(lettersMap);
	}, [indexes]);

	return (
		<div>
			{Array.from(letters).map(([letter, letterIndexes]) => (
				<div key={letter}>
					<TranscriptionIndexesItemsLetterIndicator letter={letter} />
					<ul style={{listStyle: 'none', padding: '2rem 0rem'}}>
						{letterIndexes.map(index => {
							if(index.name[0] === letter) {
								return (
									<li key={index.id} className={ styles["indexes__list--item"] } >
										<TranscriptionIndexesItem index={index} transcriptionId={transcriptionId}/>
									</li>
								);
							}
						})}
					</ul>
				</div>
			))}

		</div>
	);
}

TranscriptionIndexesItemsByTag.propTypes = {
	indexes: PropTypes.object,
	transcriptionId: PropTypes.number,
};

export default TranscriptionIndexesItemsByTag;

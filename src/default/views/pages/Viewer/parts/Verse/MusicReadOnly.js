import React, { useEffect, memo, useRef } from "react";
import PropTypes from "prop-types";
import { useSelector } from 'react-redux';

import Button from "@Elements/Button/Button";

import IsVerseOnScreen from "@Pages/Viewer/parts/Verse/IsVerseOnScreen";

const MusicReadOnly = ({ verse }) => {
	const verseActive = useSelector((state) => state.viewer.editor.verseActive);
	const verseEdit = useSelector((state) => state.viewer.editor.verseEdit);
	const layersData = useSelector((state) => state.viewer.transcriptionLayersData);

	const verseRef = useRef();
	const isVisible = IsVerseOnScreen(verseRef);

	useEffect(() => {
		if (verseActive === verse.id && !isVisible) {
			verseRef.current.scrollIntoView({
				behavior: "smooth",
			});
		}
	}, [ verse.id, verseActive ]);

	let layerNumber = layersData.idToNumber.get(verse.layerId ? `${verse.layerId}` : verse.pageLayer.layerId);

	return (
		<div
			className={ `verse__block-music verse__block-music--readonly${verse.id === verseActive ? ' active' : ''} ${verseEdit ? ' activeEdit' : ''}` }
			ref={ verseRef }
		>
			<Button
				variant='verse'
			>
				<div className='verse__num'>
					{verse.verseNo}
					<sub>[{layerNumber}]</sub>
				</div>
				<div
					className='verse__content'
					dangerouslySetInnerHTML={ { __html: verse.payload ? verse.payload.payloadFileName : "" } }
				/>
			</Button>
		</div>
	);
};

MusicReadOnly.propTypes = {
	verse: PropTypes.object,
};

export default memo(MusicReadOnly);
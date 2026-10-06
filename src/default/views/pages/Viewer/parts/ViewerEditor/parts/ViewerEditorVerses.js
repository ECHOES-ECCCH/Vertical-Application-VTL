import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";

import Verse from "@Pages/Viewer/parts/Verse/Verse";

const ViewerEditorVerses = () => {
	const viewerData = useSelector(state => state.viewer.data);
	const pageLayers = useSelector(state => state.viewer.transcriptionLayersData);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const mobilePanels = useSelector(state => state.app.viewer.mobilePanels);

	let student = mobilePanels === "student";
	let verses = student ? viewerData.studentVerses : viewerData.verses;

	const [ versesToUse, setVersesToUse ] = useState(verses);

	useEffect(() => {
		if (student) {
			setVersesToUse(verses.filter(verse => {
				return verse.layerId == selectedLayer;
			}));
		} else {
			const shownLayers = [];
			const availableLayers = [];

			pageLayers.layers.map(layer => {
				availableLayers.push(parseInt(layer.layer.id));
			});

			pageLayers.layers.map(layerToWork => {
				if(layerToWork.isShown) {
					shownLayers.push(parseInt(layerToWork.layer.id));
				}
			});
			
			setVersesToUse(verses.filter(verse => {
				if (verse.layerId) {
					return shownLayers.includes(parseInt(verse.layerId || '-1'));
				}

				return verse.pageLayer && shownLayers.includes(parseInt(verse.pageLayer.layerId || '-1'));
			}));
		}
	}, [verses, pageLayers]);

	return (
		<ul
			className={`viewer__verses--list ${versesToUse && versesToUse.length > 5 ? "viewer__verses--list-overflow" : ""}`}
		>
			{versesToUse && versesToUse.map(verse => {
				return <Verse key={verse.id} verse={verse} />;
			})}
		</ul>
	);
};

export default ViewerEditorVerses;

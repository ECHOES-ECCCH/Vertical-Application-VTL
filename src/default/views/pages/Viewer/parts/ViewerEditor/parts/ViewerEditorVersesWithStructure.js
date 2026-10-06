import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";

import Verse from "@Pages/Viewer/parts/Verse/Verse";
import VersesBlock from "@Pages/Viewer/parts/Verse/VersesBlock";

const ViewerEditorVersesWithStructure = () => {
	const pageLayers = useSelector(state => state.viewer.transcriptionLayersData);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);

	const [ blocksToUse, setBlocksToUse ] = useState([]);

	useEffect(() => {
		if (pageLayers.layers.length > 0) {
			let layer;

			pageLayers.layers.map(layerToWork => {
				if(layerToWork.layer.id == selectedLayer) {
					layer = layerToWork;
				}
			});

			setBlocksToUse(layer.blocks);
		}
	}, [pageLayers, selectedLayer]);

	return (
		<ul
			className={`viewer__verses--list ${blocksToUse && blocksToUse.length > 5 ? "viewer__verses--list-overflow" : ""}`}
		>
			{blocksToUse && blocksToUse.map(block => {
				if(block.blockId !== undefined) {
					return (
						<VersesBlock 
							firstVerseNo={block.firstVerseNo}
							lastVerseNo={block.lastVerseNo}
							key={block.blockId} 
							blockId={block.blockId} 
							hierarchyDepth={0} 
							blockType={block.type} 
							blocks={block.children} 
							consistencyErrors={block.consistencyErrors}
						/>
					);
				}else{
					return (
						<Verse key={block.id} hierarchyDepth={0} verse={block} />
					);
				}
			})}
		</ul>
	);
};

export default ViewerEditorVersesWithStructure;

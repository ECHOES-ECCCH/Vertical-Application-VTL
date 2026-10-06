import React, { memo } from "react";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import PropTypes from 'prop-types';

import Button from "@Elements/Button/Button";

const ViewerOSDMenu = ( { onToggleDrawMode, removeSelectedArea, osdMethods, drawMode, zoom, homeZoom } ) => {
	const { t } = useTranslation();

	const isEditingOverlay = useSelector((state) => state.viewer.ocd.selection);

	const ViewerOSDMenuItems = [
		{
			id: "toggle-verse-draw-mode",
			tooltip:t('viewer.tooltip.osdMenu.addVerseArea'),
			icon: "add-verse",
			hideOnViewMode: true,
			disableOnEditing: true,
		},
		{
			id: "toggle-word-draw-mode",
			tooltip:t('viewer.tooltip.osdMenu.addWordArea'),
			icon: "add-word",
			hideOnViewMode: true,
			disableOnEditing: true,
			enableWhenVerseSelected: true,
			disableOnActiveMusic: true,
		},
		{
			id: "toggle-music-draw-mode",
			tooltip:t('viewer.tooltip.osdMenu.addMusicArea'),
			icon: "add-note-area",
			hideOnViewMode: true,
			disableOnEditing: true,
		},
		{
			id: "remove-area",
			tooltip:t('viewer.tooltip.osdMenu.removeArea'),
			icon: "remove-area",
			hideOnViewMode: true,
			disableOnEditing: false,
			disableWhenAreaNotSelected: true,
			disableOnVerseTextEditing: true,
		},
		{
			id: "divider",
			divider: true,
			hideOnViewMode: true,
		},
		{
			id: "zoom-in",
			tooltip:t('viewer.tooltip.osdMenu.zoomIn'),
			icon: "zoom-in",
			hideOnViewMode: false,
			disableOnEditing: false,
			clickFunction: osdMethods.zoomInFunction,
		},
		{
			id: "zoom-out",
			tooltip:t('viewer.tooltip.osdMenu.zoomOut'),
			icon: "zoom-out",
			hideOnViewMode: false,
			disableOnEditing: false,
			clickFunction: osdMethods.zoomOutFunction,
		},
		{
			id: "full-page",
			tooltip:t('viewer.tooltip.osdMenu.fullScreen'),
			icon: "fix-to-screen",
			hideOnViewMode: false,
			disableOnEditing: false,
			clickFunction: osdMethods.fullScreen,
		},
		{
			id: "fit-to-page",
			tooltip:t('viewer.tooltip.osdMenu.fitToPage'),
			icon: "fit-to-window",
			hideOnViewMode: false,
			disableOnEditing: false,
			clickFunction: osdMethods.fitToPage,
		},
	];

	const ViewerAnnotationOSDMenuItems = [
		{
			id: "toggle-annotation-draw-mode",
			tooltip:t('viewer.tooltip.osdMenu.addAnnotationArea'),
			icon: "add-annotation-rect",
			hideOnViewMode: true,
			disableOnEditing: true,
		},
		{
			id: "toggle-annotation-point-draw-mode",
			tooltip:t('viewer.tooltip.osdMenu.addAnnotationPoint'),
			icon: "add-annotation-point",
			hideOnViewMode: true,
			disableOnEditing: true,
		},
		{
			id: "remove-area",
			tooltip:t('viewer.tooltip.osdMenu.removeArea'),
			icon: "remove-area",
			hideOnViewMode: true,
			disableOnEditing: false,
			disableWhenAreaNotSelected: true,
			disableOnVerseTextEditing: true,
		},
		{
			id: "divider",
			divider: true,
			hideOnViewMode: true,
		},
		{
			id: "zoom-in",
			tooltip:t('viewer.tooltip.osdMenu.zoomIn'),
			icon: "zoom-in",
			hideOnViewMode: false,
			disableOnEditing: false,
			clickFunction: osdMethods.zoomInFunction,
		},
		{
			id: "zoom-out",
			tooltip:t('viewer.tooltip.osdMenu.zoomOut'),
			icon: "zoom-out",
			hideOnViewMode: false,
			disableOnEditing: false,
			clickFunction: osdMethods.zoomOutFunction,
		},
		{
			id: "full-page",
			tooltip:t('viewer.tooltip.osdMenu.fullScreen'),
			icon: "fix-to-screen",
			hideOnViewMode: false,
			disableOnEditing: false,
			clickFunction: osdMethods.fullScreen,
		},
		{
			id: "fit-to-page",
			tooltip:t('viewer.tooltip.osdMenu.fitToPage'),
			icon: "fit-to-window",
			hideOnViewMode: false,
			disableOnEditing: false,
			clickFunction: osdMethods.fitToPage,
		},
	];

	const viewMode = useSelector((state) => state.viewer.data.viewMode);
	const readOnly = viewMode === 'read-only' || viewMode === 'student';
	const annotationLayer = useSelector(state => state.viewer.annotationLayer);
	const verseActive = useSelector((state) => state.viewer.editor.verseActive);
	const verseGroupActive = useSelector((state) => state.viewer.editor.verseGroupActive);
	const verseEdit = useSelector(state => state.viewer.editor.verseEdit);
	const wordActive = useSelector((state) => state.viewer.editor.wordActive);
	const wordGroupActive = useSelector((state) => state.viewer.editor.wordGroupActive);
	const wordEdit = useSelector(state => state.viewer.editor.wordEdit);
	const verses = useSelector(state => state.viewer.data.verses);

	const [activeMusic, setActiveMusic] = React.useState(false);

	React.useEffect(() => {
		let vers = verses.find(verse => verse.id === verseActive);

		if(vers && vers.type === "MUSIC_NOTATION"){
			setActiveMusic(true);
		}else{
			setActiveMusic(false);
		}
	}, [verseActive, verses]);

	return (
		<ul className='ocd-toolbar viewer__menu--list'>
			{(annotationLayer ? ViewerAnnotationOSDMenuItems : ViewerOSDMenuItems).map(item => {
				if (readOnly && item.hideOnViewMode) {
					return null;
				} else if (item.divider) {
					return <div className='viewer__menu--divider' key={item.id} />;
				} else {
					return (
						<li
							key={ item.id }
							className='viewer__menu--item'
							data-tooltip-id='custom-tooltip' data-tooltip-content={ item.tooltip }
							data-place='right'
						>
							<Button
								id={ item.id }
								className={ (item.id === 'toggle-verse-draw-mode' && drawMode === "verse" || 
											item.id === 'toggle-word-draw-mode' && drawMode === "word" ||
											item.id === 'toggle-music-draw-mode' && drawMode === "music" ||
											item.id === 'toggle-annotation-draw-mode' && drawMode === "annotation" ||
											item.id === 'toggle-annotation-point-draw-mode' && drawMode === "annotation-point") ? 'viewer__button--menu viewer__button--active' : 'viewer__button--menu' }
								disabled={ isEditingOverlay && item.disableOnEditing || 
									!verseActive && !wordActive.wordId && !verseGroupActive.lastVerseNo && !wordGroupActive.lastWordNo && item.disableWhenAreaNotSelected ||
									(verseEdit || wordEdit.wordId) && item.disableOnVerseTextEditing || 
									!verseActive && item.enableWhenVerseSelected ||
									activeMusic && item.disableOnActiveMusic }
								{ ...(item.id === 'toggle-verse-draw-mode' && 
									{ 
										onClick: () => {
											if (drawMode === "verse") {
												onToggleDrawMode("");
											} else {
												onToggleDrawMode("verse");
											}
										},
										variant: (drawMode === "verse") ? 'viewer-button--active' : 'viewer-button',
									}
								)}
								{ ...(item.id === 'toggle-word-draw-mode' &&
									{
										onClick: () => {
											if (drawMode === "word") {
												onToggleDrawMode("");
											} else {
												onToggleDrawMode("word");
											}
										},
										variant: (drawMode === "word") ? 'viewer-button--active' : 'viewer-button',
									} 
								)}
								{ ...(item.id === 'toggle-music-draw-mode' &&
									{
										onClick: () => {
											if (drawMode === "music") {
												onToggleDrawMode("");
											} else {
												onToggleDrawMode("music");
											}
										},
										variant: (drawMode === "music") ? 'viewer-button--active' : 'viewer-button',
									}
								)}
								{ ...(item.id === 'toggle-annotation-draw-mode' &&
									{
										onClick: () => {
											if (drawMode === "annotation") {
												onToggleDrawMode("");
											} else {
												onToggleDrawMode("annotation");
											}
										},
										variant: (drawMode === "annotation") ? 'viewer-button--active' : 'viewer-button',
									}
								)}
								{ ...(item.id === 'toggle-annotation-point-draw-mode' &&
									{
										onClick: () => {
											if (drawMode === "annotation-point") {
												onToggleDrawMode("");
											} else {
												onToggleDrawMode("annotation-point");
											}
										},
										variant: (drawMode === "annotation-point") ? 'viewer-button--active' : 'viewer-button',
									}
								)}
								{ ...(item.id === 'remove-area' &&
									{
										onClick: () => removeSelectedArea(),
										variant: 'viewer-button',
									} 
								)}
								{ ...(item.id !== 'toggle-verse-draw-mode' && 
									item.id !== 'toggle-word-draw-mode' && 
									item.id !== 'toggle-music-draw-mode' &&
									item.id !== 'toggle-annotation-draw-mode' &&
									item.id !== 'toggle-annotation-point-draw-mode' &&
									item.id !== 'remove-area' &&
									{
										onClick: item.clickFunction,
										variant: 'viewer-button',
									}
								)}
								{ ...(item.id === 'fit-to-page') ?
									{ disabled: zoom === homeZoom} : {}
								}
							>
								{item.icon && 
								<>
									<span className='d-none'>${item.tooltip}</span>
									<i className={ `icon-${item.icon} viewer__menu--icon` } />
								</>}
								{item.iconImg && 
								<>
									<span><img src={item.iconImg} /></span> 
								</>}
							</Button>
						</li>
					);
				}
				
			})}
		</ul>
	);
};
ViewerOSDMenu.propTypes = {
	onToggleDrawMode: PropTypes.func,
	removeSelectedArea: PropTypes.func,
	osdMethods: PropTypes.object,
	drawMode: PropTypes.string,
	zoom: PropTypes.number,
	homeZoom: PropTypes.number,
};

export default memo(ViewerOSDMenu);
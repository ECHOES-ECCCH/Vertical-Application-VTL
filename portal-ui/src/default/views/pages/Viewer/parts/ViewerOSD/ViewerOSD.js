import React, { useCallback, useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useParams, useHistory } from "react-router";
import PropTypes from "prop-types";

import OpenSeadragon from "openseadragon";

import api from "@/services/api";
import { viewerActions } from "@/store/actions/viewer.actions";
import { appActions } from "@/store/actions/app.actions";
import { CheckViewModePermission } from "@/permissions/ViewModes";

const ViewerOSD = ({ onToggleDrawMode, osdDrawMode, setOsdMethods, setHomeZoom, setZoom }) => {
	const isMobile = useSelector((state) => state.viewer.isMobile);
	const viewer = useSelector(state => state.viewer);
	const verses = useSelector(state => state.viewer.data.verses);
	const selectionEdit = useSelector(state => state.viewer.ocd.selection);
	const { transcriptionId } = useParams();
	const dispatch = useDispatch();
	const [wltOCD, setWltOCD] = useState(null);
	const [imageBounds, setImageBounds] = useState({ x: 0, y: 0 });
	const [drawing, setDrawing] = useState(false);
	const [versesToUse, setVersesToUse] = useState(viewer.data.verses);
	const [viewerInitialised, setViewerInitialised] = useState(false);
	const [resetCalled, setResetCalled] = useState(0);
	const layersData = useSelector((state) => state.viewer.transcriptionLayersData);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);

	const viewMode = useSelector((state) => state.viewer.data.viewMode);

	// adding new layer
	let newOverlay = null;
	let newOverlayStartPoint = { x: 0, y: 0 };
	let dragged = false;

	// edit purpose:
	const [editOverlayId, setEditOverlayId] = useState({ type: "", id: null});

	const openSeadragonConfig = {
		id: `ocd${viewer.data.id}`,
		showNavigationControl: false,
		tileSources: [],
	};

	useEffect(() => {
		if(resetCalled > 0) {
			setResetCalled(0);
			resetOverlays();
		}
	}, [resetCalled]);

	useEffect(() => {
		setResetCalled(resetCalled + 1);
		overlaysAddOnMouseDown();
	}, [viewer.editor.wordActive, viewer.editor.verseActive, viewer.editor.verseGroupActive, viewer.editor.wordGroupActive, osdDrawMode]);

	useEffect(() => {
		if (wltOCD) {
			drawOverlaysFromApiLight(wltOCD, versesToUse);
			// if before reloading verses editor was in edit overlay mode - restore that feature
			if (editOverlayId.type === "verse") { 
				setVerseEdited(editOverlayId.id); 
			} else if (editOverlayId.type === "word") {
				setWordEdited(editOverlayId.id.verseId, editOverlayId.id.wordId);
			} else if (editOverlayId.type === "music") {
				setMusicEdited(editOverlayId.id);
			} else if (editOverlayId.type === "annotation") {
				setAnnotationEdited(editOverlayId.id);
			} else if (editOverlayId.type === "annotation-point") {
				setAnnotationPointEdited(editOverlayId.id);
			}
			
		}
		if (!selectionEdit) { overlaysAddOnMouseDown(); }

		setResetCalled(resetCalled + 1);
	}, [wltOCD, versesToUse, selectionEdit, editOverlayId]);

	const canvasPressHandler = (e) => {
		if(!e.originalEvent.doNotUnset){
			dispatch(viewerActions.setVerseActive(null));
			dispatch(viewerActions.setWordActive({ verseId: null, wordId: null }));
		}
	};

	useEffect(() => {
		const OCD = OpenSeadragon(openSeadragonConfig);
		if (viewer.isLoaded === false) {
			OCD.destroy();
		} else {
			setWltOCD(OCD);
			OCD.open({ ...viewer.ocd.tileSources });
			OCD.addHandler("open", function () {
				const tiledImage = OCD.world.getItemAt(0);
				if (tiledImage.getFullyLoaded()) {
					initializeViewer(OCD);
				} else {
					tiledImage.addHandler("fully-loaded-change", initializeViewer(OCD));
				}
			});
			OCD.addHandler("zoom", () => zoomingButtons(OCD));
			OCD.addHandler("canvas-press", canvasPressHandler);
		}
	}, [viewer.isLoaded]);

	useEffect(() => {
		if (wltOCD) {
			setDrawOverlayMode(osdDrawMode);
		}
	}, [osdDrawMode]);

	const getVerseById = id => {
		for(let singleVerse of verses){
			if(+singleVerse.id === +id) {
				return singleVerse;
			}
		}
		return false;
	};

	const initializeViewer = ocd => {
		let imBounds = ocd.viewport.imageToViewportCoordinates(
			new OpenSeadragon.Point(viewer.data.boundingBox.lowerRightX, viewer.data.boundingBox.lowerRightY),
		);
		setImageBounds({ x: imBounds.x, y: imBounds.y });
		drawOverlaysFromApi(ocd, versesToUse);
		ocd.gestureSettingsMouse.clickToZoom = false;
		ocd.innerTracker.keyDownHandler = null;
		ocd.innerTracker.keyPressHandler = null;
		ocd.innerTracker.keyHandler = null;
		ocd.innerTracker.keyUpHandler = null;

		setViewerInitialised(true);
	};

	useEffect(() => {
		const shownLayers = [];

		layersData.layers.map(layerToWork => {
			if(layerToWork.layer.id === selectedLayer) {
				shownLayers.push(parseInt(layerToWork.layer.id));
			}
		});

		setVersesToUse(verses.filter(verse => {
			return verse.pageLayer && shownLayers.includes(parseInt(verse.pageLayer.layerId || '-1'));
		}));

	}, [verses, layersData]);

	const drawWordOverlay = (verseId, ocd, word) => {
		let overlay = document.createElement("div");
		let bBox = word.boundingBox;
		if(!bBox) { bBox = {upperLeftX: 0, upperLeftY: 0, lowerRightX: 0, lowerRightY: 0}; } 

		overlay.id = "ovrly" + verseId + "-" + word.id;

		overlay.className = "osd-word-overlay";
		if (viewer.editor.wordActive.wordId && word.id === viewer.editor.wordActive.wordId ||
			viewer.editor.wordGroupActive.activeWordsIds.includes(word.id)
		) {
			overlay.className = "osd-word-overlay--highlight";
		}

		let rc = ocd.viewport.imageToViewportRectangle(
			bBox.upperLeftX,
			bBox.upperLeftY,
			bBox.lowerRightX,
			bBox.lowerRightY,
		);
		let rct = new OpenSeadragon.Rect(rc.x, rc.y, rc.width - rc.x, rc.height - rc.y);

		osdAddWordOverlay(overlay, rct, ocd);
	};

	const drawWordOverlayLight = (verseId, ocd, word) => {
		let overlay = document.getElementById("ovrly" + verseId + "-" + word.id);
		if (!overlay) {
			overlay = document.createElement("div");
			overlay.id = "ovrly" + verseId + "-" + word.id;
		}

		let bBox = word.boundingBox;
		if(!bBox) { bBox = {upperLeftX: 0, upperLeftY: 0, lowerRightX: 0, lowerRightY: 0}; } 
		let rc = ocd.viewport.imageToViewportRectangle(
			bBox.upperLeftX,
			bBox.upperLeftY,
			bBox.lowerRightX,
			bBox.lowerRightY,
		);
		let rct = new OpenSeadragon.Rect(rc.x, rc.y, rc.width - rc.x, rc.height - rc.y);

		overlay.className = "osd-word-overlay";
		if (viewer.editor.wordActive.wordId && word.id === viewer.editor.wordActive.wordId ||
			viewer.editor.wordGroupActive.activeWordsIds.includes(word.id)
		) {
			overlay.className = "osd-word-overlay--highlight";
		}

		osdAddWordOverlay(overlay, rct, ocd);

	};

	const drawOverlaysFromApi = (ocd, versesToDraw) => {
		ocd.clearOverlays();
		for (let i = 0; i < versesToDraw.length; i++) {
			let overlay = document.createElement("div");
			let bBox = versesToDraw[i].boundingBox;
			if(!bBox) { bBox = {upperLeftX: 0, upperLeftY: 0, lowerRightX: 0, lowerRightY: 0}; } 

			overlay.id = "ovrly" + versesToDraw[i].id;

			if (viewer.annotationLayer) {
				if (versesToDraw[i].boundingBox.lowerRightX === versesToDraw[i].boundingBox.upperLeftX && versesToDraw[i].boundingBox.lowerRightY === versesToDraw[i].boundingBox.upperLeftY) {
					overlay.className = "osd-annotation-point-overlay";

					if (viewer.editor.verseActive && viewer.editor.verseActive == versesToDraw[i].id ||
						viewer.editor.verseGroupActive.activeVersesIds.includes(versesToDraw[i].id)) {
						overlay.className = "osd-annotation-point-overlay--highlight";
					}
				} else {
					overlay.className = "osd-annotation-overlay";
						
					if (viewer.editor.verseActive && versesToDraw[i].id === viewer.editor.verseActive ||
						viewer.editor.verseGroupActive.firstVerseNo >= versesToDraw[i].verseNo &&
						viewer.editor.verseGroupActive.lastVerseNo <= versesToDraw[i].verseNo) {
						overlay.className = "osd-annotation-overlay--highlight";
					}
				}
			} else {
				if(versesToDraw[i].type === "MUSIC_NOTATION"){
					overlay.className = "osd-music-overlay";

					if (viewer.editor.verseActive && versesToDraw[i].id === viewer.editor.verseActive ||
						viewer.editor.verseGroupActive.firstVerseNo >= versesToDraw[i].verseNo &&
						viewer.editor.verseGroupActive.lastVerseNo <= versesToDraw[i].verseNo) {
						overlay.className = "osd-music-overlay--highlight";
					}
				}else{
					overlay.className = "osd-overlay";
					
					if (viewer.editor.verseActive && versesToDraw[i].id === viewer.editor.verseActive ||
						viewer.editor.verseGroupActive.firstVerseNo >= versesToDraw[i].verseNo &&
						viewer.editor.verseGroupActive.lastVerseNo <= versesToDraw[i].verseNo) {
						overlay.className = "osd-overlay--highlight";
					} else if (!versesToDraw[i].words || versesToDraw[i].words.length === 0) {
						overlay.className = "osd-overlay--visible";
					}
				}
			}

			let rct;
			if (versesToDraw[i].boundingBox.lowerRightX === versesToDraw[i].boundingBox.upperLeftX && versesToDraw[i].boundingBox.lowerRightY === versesToDraw[i].boundingBox.upperLeftY) {
				let rc = ocd.viewport.imageToViewportRectangle(
					bBox.upperLeftX,
					bBox.upperLeftY,
					bBox.upperLeftX,
					bBox.upperLeftY,
				);
				rct = new OpenSeadragon.Rect(rc.x, rc.y, rc.width - rc.x, rc.height - rc.y);
			} else {
				let rc = ocd.viewport.imageToViewportRectangle(
					bBox.upperLeftX,
					bBox.upperLeftY,
					bBox.lowerRightX,
					bBox.lowerRightY,
				);
				rct = new OpenSeadragon.Rect(rc.x, rc.y, rc.width - rc.x, rc.height - rc.y);
			}

			osdAddOverlay(overlay, rct, ocd, versesToDraw[i].words && versesToDraw[i].words.length > 0);

			if (versesToDraw[i].words) {
				for(let j = 0; j < versesToDraw[i].words.length; ++j){
					drawWordOverlay(versesToDraw[i].id, ocd, versesToDraw[i].words[j]);
				}
			}
		}
	};

	const drawOverlaysFromApiLight = useCallback((ocd, versesToDraw) => {
		let overlaysLeftForRemoval = new Set(document.querySelectorAll("[id^='ovrly']"));
		for (let i = 0; i < versesToDraw.length; i++) {
			// Check if element already exists
			let overlay = document.getElementById("ovrly" + versesToDraw[i].id);
			if (!overlay){ 
				overlay = document.createElement("div");
				overlay.id = "ovrly" + versesToDraw[i].id;
			} else {
				overlaysLeftForRemoval.delete(overlay);
			}
			
			// Calculate location
			let bBox = versesToDraw[i].boundingBox;
			if(!bBox) { bBox = {upperLeftX: 0, upperLeftY: 0, lowerRightX: 0, lowerRightY: 0}; } 
		
			let rct;
			if (versesToDraw[i].boundingBox.lowerRightX === versesToDraw[i].boundingBox.upperLeftX && versesToDraw[i].boundingBox.lowerRightY === versesToDraw[i].boundingBox.upperLeftY) {
				let rc = ocd.viewport.imageToViewportRectangle(
					bBox.upperLeftX,
					bBox.upperLeftY,
					bBox.upperLeftX,
					bBox.upperLeftY,
				);
				rct = new OpenSeadragon.Rect(rc.x, rc.y, rc.width - rc.x, rc.height - rc.y);
			} else {
				let rc = ocd.viewport.imageToViewportRectangle(
					bBox.upperLeftX,
					bBox.upperLeftY,
					bBox.lowerRightX,
					bBox.lowerRightY,
				);
				rct = new OpenSeadragon.Rect(rc.x, rc.y, rc.width - rc.x, rc.height - rc.y);
			}
			if (viewer.annotationLayer) {
				if (versesToDraw[i].boundingBox.lowerRightX === versesToDraw[i].boundingBox.upperLeftX && versesToDraw[i].boundingBox.lowerRightY === versesToDraw[i].boundingBox.upperLeftY) {
					overlay.className = "osd-annotation-point-overlay";

					if (viewer.editor.verseActive && viewer.editor.verseActive == versesToDraw[i].id ||
						viewer.editor.verseGroupActive.activeVersesIds.includes(versesToDraw[i].id)) {
						overlay.className = "osd-annotation-point-overlay--highlight";
					}
				} else {
					overlay.className = "osd-annotation-overlay";
						
					if (viewer.editor.verseActive && versesToDraw[i].id === viewer.editor.verseActive ||
						viewer.editor.verseGroupActive.firstVerseNo >= versesToDraw[i].verseNo &&
						viewer.editor.verseGroupActive.lastVerseNo <= versesToDraw[i].verseNo) {
						overlay.className = "osd-annotation-overlay--highlight";
					}
				}
			} else {
				if(versesToDraw[i].type === "MUSIC_NOTATION"){
					overlay.className = "osd-music-overlay";

					if (viewer.editor.verseActive && versesToDraw[i].id === viewer.editor.verseActive ||
						viewer.editor.verseGroupActive.firstVerseNo >= versesToDraw[i].verseNo &&
						viewer.editor.verseGroupActive.lastVerseNo <= versesToDraw[i].verseNo) {
						overlay.className = "osd-music-overlay--highlight";
					}
				} else {
					overlay.className = "osd-overlay";
					
					if (viewer.editor.verseActive && versesToDraw[i].id === viewer.editor.verseActive ||
						viewer.editor.verseGroupActive.firstVerseNo >= versesToDraw[i].verseNo &&
						viewer.editor.verseGroupActive.lastVerseNo <= versesToDraw[i].verseNo) {
						overlay.className = "osd-overlay--highlight";
					} else if (!versesToDraw[i].words || versesToDraw[i].words.length === 0) {
						overlay.className = "osd-overlay--visible";
					}
				}
			}

			osdAddOverlay(overlay, rct, ocd, versesToDraw[i].words && versesToDraw[i].words.length > 0);
		
			if (versesToDraw[i].words) {
				for(let j = 0; j < versesToDraw[i].words.length; ++j){
					let wordOverlay = document.getElementById("ovrly" + versesToDraw[i].id + "-" + versesToDraw[i].words[j].id);
					if (wordOverlay) {
						overlaysLeftForRemoval.delete(wordOverlay);
					}
					drawWordOverlayLight(versesToDraw[i].id, ocd, versesToDraw[i].words[j]);
				}
			}
		}

		for (let x of overlaysLeftForRemoval) {
			ocd.removeOverlay(x);
		}
		
		setResetCalled(resetCalled + 1);
	}, [viewer.editor.verseActive, viewer.editor.verseGroupActive]);

	const onOverlayClick = id => {
		const verseNum = parseInt(id);
		dispatch(viewerActions.setVerseActive(verseNum));
	};
	const onWordOverlayClick = (vId, wId) => {
		const wordId = parseInt(wId);
		const verseId = parseInt(vId);
		dispatch(viewerActions.setWordActive({verseId: verseId, wordId: wordId}));
	};

	const zoomingButtons = ocd => {
		setZoom(ocd.viewport.getZoom());
		setHomeZoom(ocd.viewport.getHomeZoom());
	};

	const overlayPointerDownHandler = (eventInfo, _element) => {
		if (eventInfo.eventType === "pointerdown") {
			if (viewer.editor.verseActive === +_element.id.replace("ovrly", "")) {
				if (!isMobile && CheckViewModePermission("canEditVerses", viewMode)) {
					dispatch(viewerActions.setSelectionEdit(true));
					eventInfo.stopPropagation = true;
				}
			} else {
				onOverlayClick(_element.id.replace("ovrly", ""));
			}
			eventInfo.originalEvent.doNotUnset = true;
		}
	};

	const wordOverlayPointerDownHandler = (eventInfo, _element) => {
		if (eventInfo.eventType === "pointerdown") {
			let pair = _element.id.replace("ovrly", "");
			let ids = pair.split('-');
			
			const verseId = ids[0];
			const wordId = ids[1];

			if (viewer.editor.wordActive.wordId === +wordId) {
				if (!isMobile && CheckViewModePermission("canEditVerses", viewMode)) {
					dispatch(viewerActions.setSelectionEdit(true));
					eventInfo.stopPropagation = true;
				}
			} else if (viewer.editor.verseActive === +verseId) {
				onWordOverlayClick(verseId, wordId);
			} else {
				onOverlayClick(verseId);
			}
			eventInfo.originalEvent.doNotUnset = true;
		}
	};

	const osdAddOverlay = (_element, _location, ocd, hasWords) => {
		let overlayObject = ocd.getOverlayById(_element);
		if (overlayObject) {
			overlayObject.update({ location: _location });
		} else {
			ocd.addOverlay({
				element: _element,
				location: _location,
			});
		}
		
		if (_element.elementMouseTracker) { _element.elementMouseTracker.destroy(); }
		if (hasWords || osdDrawMode === "word") {
			_element.elementMouseTracker = new OpenSeadragon.MouseTracker({
				element: _element,
				preProcessEventHandler: () => {},
			});
		} else {
			_element.elementMouseTracker = new OpenSeadragon.MouseTracker({
				element: _element,
				preProcessEventHandler: eventInfo => overlayPointerDownHandler(eventInfo, _element),
			});
		}
	};

	const osdAddWordOverlay = (_element, _location, ocd) => {
		let overlayObject = ocd.getOverlayById(_element);
		if (overlayObject) {
			overlayObject.update({ location: _location });
		} else {
			ocd.addOverlay({
				element: _element,
				location: _location,
			});
		}

		if (_element.elementMouseTracker) { _element.elementMouseTracker.destroy(); }

		_element.elementMouseTracker = new OpenSeadragon.MouseTracker({
			element: _element,
			preProcessEventHandler: eventInfo => wordOverlayPointerDownHandler(eventInfo, _element),
		});
	};

	// data necessary for transform overlay:
	const resizingData = {
		startTransformOverlayBounds: {
			x: 0,
			y: 0,
			width: 0,
			height: 0,
		},
		drag: {
			startX: 0,
			startY: 0,
		},
		maxMove: {
			left: 0,
			top: 0,
			right: 0,
			bottom: 0,
		},
	};
	let currOverlay = null;

	useEffect(() => {
		if (wltOCD) {
			setEditOverlayMode(selectionEdit);
		}
	}, [selectionEdit]);

	// get into edit position and size mode:
	const setEditOverlayMode = editMode => {
		wltOCD.panVertical = !editMode;
		wltOCD.panHorizontal = !editMode;
		wltOCD.setControlsEnabled(!editMode);
		//wltOCD.setMouseNavEnabled(!editMode); // mouseScroll disabled

		if (editMode) {
			let foundElement = viewer.data.verses.find((el => { return el.id === viewer.editor.verseActive; }));
			
			if (viewer.editor.verseActive && viewer.annotationLayer) {
				setEditOverlayId({ type: "annotation", id: viewer.editor.verseActive });
			} else if (viewer.editor.verseActive && foundElement.type === "MUSIC_NOTATION") {
				setEditOverlayId({ type: "music", id: viewer.editor.verseActive });
			} else if (viewer.editor.verseActive && foundElement.type === null) {
				setEditOverlayId({ type: "verse", id: viewer.editor.verseActive });
			} else if (viewer.editor.wordActive.wordId) {
				setEditOverlayId({ type: "word", id: viewer.editor.wordActive });
			} else {
				setEditOverlayId({ type: "", id: null });
			}
		} else {
			setEditOverlayId({ type: "", id: null });
		}

		setResetCalled(resetCalled + 1);
	};

	//VERSES STATUSES
	const unsetVerse = (verseId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId);
		if(ovr){
			let ovrHtml = document.getElementById("ovrly" + verseId);
			ovrHtml.innerHTML = '';
			ovrHtml.style.zIndex = "0";
		}
	};
	const setVerseHighlighted = (verseId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId);
		let currentVerse = getVerseById(verseId);
		if(ovr && currentVerse){
			let ovrHtml = document.getElementById("ovrly" + verseId);
			ovrHtml.innerHTML = '<div class="versenumber"></div>';
			ovrHtml.style.zIndex = "0";
			ovrHtml.querySelector(".versenumber").innerHTML = '<span>' + currentVerse.verseNo + '<sub>[' + layersData.idToNumber.get(currentVerse.pageLayer.layerId) + ']</sub></span>';

			wltOCD.getOverlayById("ovrly" + verseId).element.className = osdDrawMode !== ""
				? "osd-overlay--highlight h-disabled"
				: "osd-overlay--highlight";
		}
	};
	const setVerseEdited = (verseId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId);
		let currentVerse = getVerseById(verseId);
		if(ovr && currentVerse){
			let ovrHtml = document.getElementById("ovrly" + verseId);
			currOverlay = ovrHtml;
			if (!ovrHtml) return;
			overlaysDisableMouseDown();
			ovrHtml.innerHTML =
				'<div class="versenumber"></div><div class="leftbottom"></div><div class="righttop"></div><div class="inner-handler"></div>';
			
			document.querySelector(".versenumber").innerHTML = '<span>' + currentVerse.verseNo + '<sub>[' + layersData.idToNumber.get(currentVerse.pageLayer.layerId) + ']</sub></span>';
			document.querySelector(".leftbottom").onmousedown = leftCornerStartDrag;
			document.querySelector(".righttop").onmousedown = rightCornerStartDrag;
			document.querySelector(".inner-handler").onmousedown = dragOverlayStart;
			ovrHtml.style.zIndex = "100";
			ovrHtml.className = "osd-overlay--highlight";
		}
	};
	//WORD STATUSES
	const unsetWord = (verseId, wordId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId + '-' + wordId);
		if(ovr){
			let ovrHtml = document.getElementById("ovrly" + verseId + '-' + wordId);
			ovrHtml.innerHTML = '';
			ovrHtml.style.zIndex = "0";
		}
	};
	const setWordHighlighted = (verseId, wordId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId + '-' + wordId);
		if(ovr){
			let ovrHtml = document.getElementById("ovrly" + verseId + '-' + wordId);
			ovrHtml.innerHTML = '';
			ovrHtml.style.zIndex = "0";

			wltOCD.getOverlayById("ovrly" + verseId + '-' + wordId).element.className = osdDrawMode !== ""
				? "osd-word-overlay--highlight h-disabled"
				: "osd-word-overlay--highlight";
		}
	};
	const setWordEdited = (verseId, wordId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId + '-' + wordId);
		if(ovr){
			let ovrHtml = document.getElementById("ovrly" + verseId + '-' + wordId);
			currOverlay = ovrHtml;
			if (!ovrHtml) return;
			overlaysDisableMouseDown();
			ovrHtml.innerHTML =
				'<div class="leftbottom"></div><div class="righttop"></div><div class="inner-handler"></div>';
			
			document.querySelector(".leftbottom").onmousedown = leftCornerStartDrag;
			document.querySelector(".righttop").onmousedown = rightCornerStartDrag;
			document.querySelector(".inner-handler").onmousedown = dragOverlayStart;
			ovrHtml.style.zIndex = "100";
			ovrHtml.className = "osd-word-overlay--highlight";
		}
	};
	//MUSIC NOTATION
	const unsetMusic = (verseId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId);
		if(ovr){
			let ovrHtml = document.getElementById("ovrly" + verseId);
			ovrHtml.innerHTML = '';
			ovrHtml.style.zIndex = "0";
		}
	};
	const setMusicHighlighted = (verseId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId);
		let currentVerse = getVerseById(verseId);
		if(ovr && currentVerse){
			let ovrHtml = document.getElementById("ovrly" + verseId);
			ovrHtml.innerHTML = '<div class="versenumber"></div>';
			ovrHtml.style.zIndex = "0";
			ovrHtml.querySelector(".versenumber").innerHTML = '<span>' + currentVerse.verseNo + '<sub>[' + layersData.idToNumber.get(currentVerse.pageLayer.layerId) + ']</sub></span>';

			wltOCD.getOverlayById("ovrly" + verseId).element.className = osdDrawMode !== ""
				? "osd-music-overlay--highlight h-disabled"
				: "osd-music-overlay--highlight";
		}
	};
	const setMusicEdited = (verseId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId);
		let currentVerse = getVerseById(verseId);
		if(ovr && currentVerse){
			let ovrHtml = document.getElementById("ovrly" + verseId);
			currOverlay = ovrHtml;
			if (!ovrHtml) return;
			overlaysDisableMouseDown();
			ovrHtml.innerHTML =
				'<div class="versenumber"></div><div class="leftbottom"></div><div class="righttop"></div><div class="inner-handler"></div>';
			
			document.querySelector(".versenumber").innerHTML = '<span>' + currentVerse.verseNo + '<sub>[' + layersData.idToNumber.get(currentVerse.pageLayer.layerId) + ']</sub></span>';
			document.querySelector(".leftbottom").onmousedown = leftCornerStartDrag;
			document.querySelector(".righttop").onmousedown = rightCornerStartDrag;
			document.querySelector(".inner-handler").onmousedown = dragOverlayStart;
			ovrHtml.style.zIndex = "100";
			ovrHtml.className = "osd-music-overlay--highlight";
		}
	};
	//ANNOTATIONS
	const unsetAnnotation = (verseId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId);
		if(ovr){
			let ovrHtml = document.getElementById("ovrly" + verseId);
			ovrHtml.innerHTML = '';
			ovrHtml.style.zIndex = "0";
		}
	};
	const setAnnotationHighlighted = (verseId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId);
		let currentVerse = getVerseById(verseId);
		if(ovr && currentVerse){
			let ovrHtml = document.getElementById("ovrly" + verseId);
			ovrHtml.innerHTML = '<div class="versenumber"></div>';
			ovrHtml.style.zIndex = "0";
			ovrHtml.querySelector(".versenumber").innerHTML = '<span>' + currentVerse.verseNo + '<sub>[' + layersData.idToNumber.get(currentVerse.pageLayer.layerId) + ']</sub></span>';

			wltOCD.getOverlayById("ovrly" + verseId).element.className = osdDrawMode !== ""
				? "osd-annotation-overlay--highlight h-disabled"
				: "osd-annotation-overlay--highlight";
		}
	};
	const setAnnotationEdited = (verseId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId);
		let currentVerse = getVerseById(verseId);
		if(ovr && currentVerse){
			let ovrHtml = document.getElementById("ovrly" + verseId);
			currOverlay = ovrHtml;
			if (!ovrHtml) return;
			overlaysDisableMouseDown();
			ovrHtml.innerHTML =
				'<div class="versenumber"></div><div class="leftbottom"></div><div class="righttop"></div><div class="inner-handler"></div>';
			
			document.querySelector(".versenumber").innerHTML = '<span>' + currentVerse.verseNo + '<sub>[' + layersData.idToNumber.get(currentVerse.pageLayer.layerId) + ']</sub></span>';
			document.querySelector(".leftbottom").onmousedown = leftCornerStartDrag;
			document.querySelector(".righttop").onmousedown = rightCornerStartDrag;
			document.querySelector(".inner-handler").onmousedown = dragOverlayStart;
			ovrHtml.style.zIndex = "100";
			ovrHtml.className = "osd-annotation-overlay--highlight";
		}
	};

	//POINT ANNOTATIONS
	const unsetAnnotationPoint = (verseId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId);
		if(ovr){
			let ovrHtml = document.getElementById("ovrly" + verseId);
			ovrHtml.innerHTML = "";
			ovrHtml.style.zIndex = "0";
		}
	};
	const setAnnotationPointHighlighted = (verseId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId);
		let currentVerse = getVerseById(verseId);
		if(ovr && currentVerse){
			let ovrHtml = document.getElementById("ovrly" + verseId);
			ovrHtml.innerHTML = '<div class="versenumber"></div>';
			ovrHtml.style.zIndex = "0";
			ovrHtml.querySelector(".versenumber").innerHTML = '<span>' + currentVerse.verseNo + '<sub>[' + layersData.idToNumber.get(currentVerse.pageLayer.layerId) + ']</sub></span>';

			wltOCD.getOverlayById("ovrly" + verseId).element.className = osdDrawMode !== ""
				? "osd-annotation-point-overlay--highlight h-disabled"
				: "osd-annotation-point-overlay--highlight";
		}
	};
	const setAnnotationPointEdited = (verseId) => {
		let ovr = wltOCD && wltOCD.getOverlayById("ovrly" + verseId);
		let currentVerse = getVerseById(verseId);
		if(ovr && currentVerse){
			let ovrHtml = document.getElementById("ovrly" + verseId);
			currOverlay = ovrHtml;
			if (!ovrHtml) return;
			overlaysDisableMouseDown();
			ovrHtml.innerHTML =
				'<div class="versenumber"></div><div class="inner-point-handler"></div>';
			
			document.querySelector(".versenumber").innerHTML = '<span>' + currentVerse.verseNo + '<sub>[' + layersData.idToNumber.get(currentVerse.pageLayer.layerId) + ']</sub></span>';
			document.querySelector(".inner-point-handler").onmousedown = dragPointOverlayStart;
			ovrHtml.style.zIndex = "100";
			ovrHtml.className = "osd-annotation-point-overlay--highlight";
		}
	};

	useEffect(() => {
		if (editOverlayId) {
			if(editOverlayId.type === "verse"){
				setVerseEdited(editOverlayId.id);
			} else if (editOverlayId.type === "word") {
				setWordEdited(editOverlayId.id.verseId, editOverlayId.id.wordId);
			} else if (editOverlayId.type === "music") {
				setMusicEdited(editOverlayId.id);
			} else if (editOverlayId.type === "annotation") {
				setAnnotationEdited(editOverlayId.id);
			} else if (editOverlayId.type === "annotation-point") {
				setAnnotationPointEdited(editOverlayId.id);
			} else {
				overlaysAddOnMouseDown();
			}
		} else {
			overlaysAddOnMouseDown();
		}
		setResetCalled(resetCalled + 1);
	}, [editOverlayId]);

	const overlaysDisableMouseDown = () => {
		viewer.data.verses.forEach(verse => {
			// turn off possibility to select another overlay
			const overlayToDisable = document.getElementById(`ovrly${verse.id}`);
			if (overlayToDisable) {
				overlayToDisable.onmousedown = null;
			}

			if(verse.words){
				verse.words.forEach(word => {
					const wordOverlayToDisable = document.getElementById(`ovrly${verse.id}-${word.id}`);
					if (wordOverlayToDisable) {
						wordOverlayToDisable.onmousedown = null;
					}
				});
			}
		});
	};
	const overlaysAddOnMouseDown = () => {
		// turn back possibility to select overlays
		viewer.data.verses.forEach(verse => {
			// turn off possibility to select another overlay
			const overlayToAddEvListener = document.getElementById(`ovrly${verse.id}`);
			if (overlayToAddEvListener) {
				if (viewer.annotationLayer) {
					overlayToAddEvListener.elementMouseTracker.preProcessEventHandler = null;
					overlayToAddEvListener.elementMouseTracker.preProcessEventHandler = eventInfo => overlayPointerDownHandler(eventInfo, overlayToAddEvListener);
				
					if (verse.boundingBox.lowerRightX === verse.boundingBox.upperLeftX && verse.boundingBox.lowerRightY === verse.boundingBox.upperLeftY) {
						overlayToAddEvListener.className = "osd-annotation-point-overlay";

						if (viewer.editor.verseActive && viewer.editor.verseActive == verse.id ||
							viewer.editor.verseGroupActive.activeVersesIds.includes(verse.id)) {
							overlayToAddEvListener.className = "osd-annotation-point-overlay--highlight";
						}
					} else {
						overlayToAddEvListener.className = "osd-annotation-overlay";

						if (viewer.editor.verseActive && viewer.editor.verseActive == verse.id ||
							viewer.editor.verseGroupActive.activeVersesIds.includes(verse.id)) {
							overlayToAddEvListener.className = "osd-annotation-overlay--highlight";
						}
					}
				} else {
					if (verse.type === "MUSIC_NOTATION") {
						overlayToAddEvListener.elementMouseTracker.preProcessEventHandler = null;
						overlayToAddEvListener.elementMouseTracker.preProcessEventHandler = eventInfo => overlayPointerDownHandler(eventInfo, overlayToAddEvListener);
					
						overlayToAddEvListener.className = "osd-music-overlay";

						if (viewer.editor.verseActive && viewer.editor.verseActive == verse.id ||
							viewer.editor.verseGroupActive.activeVersesIds.includes(verse.id)) {
							overlayToAddEvListener.className = "osd-music-overlay--highlight";
						}
					} else {
						overlayToAddEvListener.elementMouseTracker.preProcessEventHandler = null;
						if ((!verse.words || verse.words.length === 0) && osdDrawMode !== "word") {
							overlayToAddEvListener.elementMouseTracker.preProcessEventHandler = eventInfo => overlayPointerDownHandler(eventInfo, overlayToAddEvListener);
						}

						overlayToAddEvListener.className = "osd-overlay";
						if(!verse.words || verse.words.length === 0){
							overlayToAddEvListener.className = "osd-overlay--visible";
						}

						if (viewer.editor.verseActive && viewer.editor.verseActive == verse.id ||
							viewer.editor.verseGroupActive.activeVersesIds.includes(verse.id)) {
							overlayToAddEvListener.className = "osd-overlay--highlight";
						}
					}
				}
			}
			if(verse.words){
				verse.words.forEach(word => {
					const wordOverlayToAddEvListener = document.getElementById(`ovrly${verse.id}-${word.id}`);
					if (wordOverlayToAddEvListener) {
						wordOverlayToAddEvListener.elementMouseTracker.preProcessEventHandler = null;
						wordOverlayToAddEvListener.elementMouseTracker.preProcessEventHandler = eventInfo => wordOverlayPointerDownHandler(eventInfo, wordOverlayToAddEvListener);
						wordOverlayToAddEvListener.className = "osd-word-overlay";
						if (viewer.editor.wordActive.wordId && viewer.editor.wordActive.wordId === word.id ||
							viewer.editor.wordGroupActive.activeWordsIds.includes(word.id)
						) {
							wordOverlayToAddEvListener.className = "osd-word-overlay--highlight";
						}
					}
				});
			}
		});
	};

	const setStartTransformOverlayData = () => {
		// before transform set all needed data
		const { width, height } = currOverlay.getBoundingClientRect();
		const { offsetLeft, offsetTop } = currOverlay;
		resizingData.startTransformOverlayBounds = {
			x: offsetLeft,
			y: offsetTop,
			width: width,
			height: height,
		};
		currOverlay.style.left = resizingData.startTransformOverlayBounds.x + "px";
		currOverlay.style.top = resizingData.startTransformOverlayBounds.y + "px";
		currOverlay.style.width = width;
		currOverlay.style.height = height;

		let imBounds = wltOCD.viewport.imageToViewportCoordinates(
			new OpenSeadragon.Point(viewer.data.boundingBox.lowerRightX, viewer.data.boundingBox.lowerRightY),
		);
		const ovBounds = wltOCD.getOverlayById(currOverlay.id).getBounds(wltOCD.viewport);

		const viewTopLeft = wltOCD.viewport.viewportToWindowCoordinates(new OpenSeadragon.Point(0, 0)); /// point 0 of viewport
		const viewBottomRight = wltOCD.viewport.viewportToWindowCoordinates(
			new OpenSeadragon.Point(imBounds.x, imBounds.y),
		); /// point max of viewport
		const elementTopLeft = wltOCD.viewport.viewportToWindowCoordinates(
			new OpenSeadragon.Point(ovBounds.x, ovBounds.y),
		); // point 0 of element
		const elementBottomRight = wltOCD.viewport.viewportToWindowCoordinates(
			new OpenSeadragon.Point(ovBounds.x + ovBounds.width, ovBounds.y + ovBounds.height),
		); // point max of element

		resizingData.maxMove = {
			left: elementTopLeft.x - viewTopLeft.x,
			top: elementTopLeft.y - viewTopLeft.y,
			right: viewBottomRight.x - elementBottomRight.x,
			bottom: viewBottomRight.y - elementBottomRight.y,
		};
	};

	const setStartTransformPointOverlayData = () => {
		// before transform set all needed data
		const { width, height } = currOverlay.getBoundingClientRect();

		const { offsetLeft, offsetTop } = currOverlay;
		resizingData.startTransformOverlayBounds = {
			x: offsetLeft + width / 4,
			y: offsetTop + height / 4,
			width: 0,
			height: 0,
		};
		currOverlay.style.left = resizingData.startTransformOverlayBounds.x + "px";
		currOverlay.style.top = resizingData.startTransformOverlayBounds.y + "px";
		currOverlay.style.width = 0;
		currOverlay.style.height = 0;

		let imBounds = wltOCD.viewport.imageToViewportCoordinates(
			new OpenSeadragon.Point(viewer.data.boundingBox.lowerRightX, viewer.data.boundingBox.lowerRightY),
		);
		const ovBounds = wltOCD.getOverlayById(currOverlay.id).getBounds(wltOCD.viewport);

		const viewTopLeft = wltOCD.viewport.viewportToWindowCoordinates(new OpenSeadragon.Point(0, 0)); /// point 0 of viewport
		const viewBottomRight = wltOCD.viewport.viewportToWindowCoordinates(
			new OpenSeadragon.Point(imBounds.x, imBounds.y),
		); /// point max of viewport
		const elementTopLeft = wltOCD.viewport.viewportToWindowCoordinates(
			new OpenSeadragon.Point(ovBounds.x, ovBounds.y),
		); // point 0 of element
		const elementBottomRight = wltOCD.viewport.viewportToWindowCoordinates(
			new OpenSeadragon.Point(ovBounds.x + ovBounds.width, ovBounds.y + ovBounds.height),
		); // point max of element

		resizingData.maxMove = {
			left: elementTopLeft.x - viewTopLeft.x,
			top: elementTopLeft.y - viewTopLeft.y,
			right: viewBottomRight.x - elementBottomRight.x,
			bottom: viewBottomRight.y - elementBottomRight.y,
		};
	};

	// dragging overlay in edit mode : always start -> move -> stop
	const dragPointOverlayStart = e => {
		const ev = e || window.event;
		ev.preventDefault();
		setStartTransformPointOverlayData();
		resizingData.drag.startX = ev.clientX;
		resizingData.drag.startY = ev.clientY;

		document.onmousemove = dragPointOverlayMove;
		document.onmouseup = dragPointOverlayStop;
	};
	const dragPointOverlayMove = e => {
		const ev = e || window.event;
		ev.preventDefault();
		// check that element hasn't exceeded the limits
		let xDif = ev.clientX - resizingData.drag.startX;
		if (xDif < -resizingData.maxMove.left) {
			xDif = -resizingData.maxMove.left;
		}
		if (xDif > resizingData.maxMove.right) {
			xDif = resizingData.maxMove.right;
		}
		let yDif = ev.clientY - resizingData.drag.startY;
		if (yDif < -resizingData.maxMove.top) {
			yDif = -resizingData.maxMove.top;
		}
		if (yDif > resizingData.maxMove.bottom) {
			yDif = resizingData.maxMove.bottom;
		}
		currOverlay.style.left = resizingData.startTransformOverlayBounds.x + xDif + "px";
		currOverlay.style.top = resizingData.startTransformOverlayBounds.y + yDif + "px";
	};
	const dragPointOverlayStop = ev => {
		let xDif = ev.clientX - resizingData.drag.startX;
		if (xDif < -resizingData.maxMove.left) {
			xDif = -resizingData.maxMove.left;
		}
		if (xDif > resizingData.maxMove.right) {
			xDif = resizingData.maxMove.right;
		}
		let yDif = ev.clientY - resizingData.drag.startY;
		if (yDif < -resizingData.maxMove.top) {
			yDif = -resizingData.maxMove.top;
		}
		if (yDif > resizingData.maxMove.bottom) {
			yDif = resizingData.maxMove.bottom;
		}
		currOverlay.style.left = resizingData.startTransformOverlayBounds.x + xDif + "px";
		currOverlay.style.top = resizingData.startTransformOverlayBounds.y + yDif + "px";
		recalculateEditedOverlayToOSD(xDif, yDif);
		if (viewer.editor.verseActive) {
			putCurrentPointOverlayIntoApi("ovrly" + viewer.editor.verseActive);
		}
		document.onmouseup = null;
		document.onmousemove = null;
	};

	const dragOverlayStart = e => {
		const ev = e || window.event;
		ev.preventDefault();
		setStartTransformOverlayData();
		resizingData.drag.startX = ev.clientX;
		resizingData.drag.startY = ev.clientY;

		document.onmousemove = dragOverlayMove;
		document.onmouseup = dragOverlayStop;
	};
	const dragOverlayMove = e => {
		const ev = e || window.event;
		ev.preventDefault();
		// check that element hasn't exceeded the limits
		let xDif = ev.clientX - resizingData.drag.startX;
		if (xDif < -resizingData.maxMove.left) {
			xDif = -resizingData.maxMove.left;
		}
		if (xDif > resizingData.maxMove.right) {
			xDif = resizingData.maxMove.right;
		}
		let yDif = ev.clientY - resizingData.drag.startY;
		if (yDif < -resizingData.maxMove.top) {
			yDif = -resizingData.maxMove.top;
		}
		if (yDif > resizingData.maxMove.bottom) {
			yDif = resizingData.maxMove.bottom;
		}
		currOverlay.style.left = resizingData.startTransformOverlayBounds.x + xDif + "px";
		currOverlay.style.top = resizingData.startTransformOverlayBounds.y + yDif + "px";
	};
	const dragOverlayStop = ev => {
		let xDif = ev.clientX - resizingData.drag.startX;
		if (xDif < -resizingData.maxMove.left) {
			xDif = -resizingData.maxMove.left;
		}
		if (xDif > resizingData.maxMove.right) {
			xDif = resizingData.maxMove.right;
		}
		let yDif = ev.clientY - resizingData.drag.startY;
		if (yDif < -resizingData.maxMove.top) {
			yDif = -resizingData.maxMove.top;
		}
		if (yDif > resizingData.maxMove.bottom) {
			yDif = resizingData.maxMove.bottom;
		}
		currOverlay.style.left = resizingData.startTransformOverlayBounds.x + xDif + "px";
		currOverlay.style.top = resizingData.startTransformOverlayBounds.y + yDif + "px";
		recalculateEditedOverlayToOSD(xDif, yDif);
		if (viewer.editor.verseActive) {
			putCurrentVerseOverlayIntoApi("ovrly" + viewer.editor.verseActive);
		}else if (viewer.editor.wordActive.wordId) {
			putCurrentWordOverlayIntoApi("ovrly" + viewer.editor.wordActive.verseId + "-" + viewer.editor.wordActive.wordId);
		}
		document.onmouseup = null;
		document.onmousemove = null;
	};

	const recalculateEditedOverlayToOSD = (xDif, yDif, wDif = 0, hDif = 0) => {
		let rect = null;
		if(viewer.editor.verseActive) {
			rect = wltOCD.viewport.viewportToViewerElementRectangle(
				wltOCD.getOverlayById("ovrly" + viewer.editor.verseActive).getBounds(wltOCD.viewport),
			);
		} else if (viewer.editor.wordActive.wordId) {
			rect = wltOCD.viewport.viewportToViewerElementRectangle(
				wltOCD.getOverlayById("ovrly" + viewer.editor.wordActive.verseId + "-" + viewer.editor.wordActive.wordId).getBounds(wltOCD.viewport),
			);
		}
		if (rect) {
			const newRect = new OpenSeadragon.Rect(rect.x + xDif, rect.y + yDif, rect.width + wDif, rect.height + hDif);
			const convertedRect = wltOCD.viewport.viewerElementToViewportRectangle(newRect);
			wltOCD.updateOverlay(currOverlay, convertedRect);
		}
	};

	// resizing by dragging corners
	const leftCornerStartDrag = ev => {
		// ev.stopPropagation();
		setStartTransformOverlayData();
		resizingData.drag.startX = ev.clientX;
		resizingData.drag.startY = ev.clientY;
		document.onmousemove = leftCornerDragging;
		document.onmouseup = cornerDraggingStop;
	};
	const leftCornerDragging = ev => {
		const difX =
			ev.clientX - resizingData.drag.startX < -resizingData.maxMove.left
				? -resizingData.maxMove.left
				: ev.clientX - resizingData.drag.startX;
		const difY =
			ev.clientY - resizingData.drag.startY > resizingData.maxMove.bottom
				? resizingData.maxMove.bottom
				: ev.clientY - resizingData.drag.startY;

		// don't let the currentOverlay to be smaller than 10x10px
		if (resizingData.startTransformOverlayBounds.width - difX > 10) {
			currOverlay.style.left = resizingData.startTransformOverlayBounds.x + difX + "px";
			currOverlay.style.width = resizingData.startTransformOverlayBounds.width - difX + "px";
		}
		if (resizingData.startTransformOverlayBounds.height + difY > 10) {
			currOverlay.style.height = resizingData.startTransformOverlayBounds.height + difY + "px";
		}
	};
	const rightCornerStartDrag = ev => {
		// ev.stopPropagation();
		setStartTransformOverlayData();
		resizingData.drag.startX = ev.clientX;
		resizingData.drag.startY = ev.clientY;
		document.onmousemove = rightCornerDragging;
		document.onmouseup = cornerDraggingStop;
	};
	const rightCornerDragging = ev => {
		const difX =
			ev.clientX - resizingData.drag.startX > resizingData.maxMove.right
				? resizingData.maxMove.right
				: ev.clientX - resizingData.drag.startX;
		const difY =
			ev.clientY - resizingData.drag.startY < -resizingData.maxMove.top
				? -resizingData.maxMove.top
				: ev.clientY - resizingData.drag.startY;

		// don't let the currentOverlay to be smaller than 10x10px
		if (resizingData.startTransformOverlayBounds.width + difX > 10) {
			currOverlay.style.width = resizingData.startTransformOverlayBounds.width + difX + "px";
		}
		if (resizingData.startTransformOverlayBounds.height - difY > 10) {
			currOverlay.style.top = resizingData.startTransformOverlayBounds.y + difY + "px";
			currOverlay.style.height = resizingData.startTransformOverlayBounds.height - difY + "px";
		}
	};
	const cornerDraggingStop = () => {
		const { width, height } = currOverlay.getBoundingClientRect();
		const { offsetLeft, offsetTop } = currOverlay;
		recalculateEditedOverlayToOSD(
			offsetLeft - resizingData.startTransformOverlayBounds.x,
			offsetTop - resizingData.startTransformOverlayBounds.y,
			width - resizingData.startTransformOverlayBounds.width,
			height - resizingData.startTransformOverlayBounds.height,
		);
		if (viewer.editor.verseActive) {
			putCurrentVerseOverlayIntoApi("ovrly" + viewer.editor.verseActive);
		}else if (viewer.editor.wordActive.wordId) {
			putCurrentWordOverlayIntoApi("ovrly" + viewer.editor.wordActive.verseId + "-" + viewer.editor.wordActive.wordId);
		}
		document.onmousemove = null;
		document.onmouseup = null;
	};
	// finally, wwhen move and resize is done send data to api:
	const putCurrentPointOverlayIntoApi = overlayId => {
		if (!wltOCD.getOverlayById(overlayId)) {
			// console.log('There is no overlay to put');
			return;
		}
		const ovBounds = wltOCD.getOverlayById(overlayId).getBounds(wltOCD.viewport);

		const centerPoint = wltOCD.viewport.viewportToImageCoordinates(
			ovBounds.x,
			ovBounds.y,
		);
		const newBounds = {
			upperLeftX: Math.round(centerPoint.x),
			upperLeftY: Math.round(centerPoint.y),
			lowerRightX: Math.round(centerPoint.x),
			lowerRightY: Math.round(centerPoint.y),
		};
		const activeVerseId = viewer.editor.verseActive;
		dispatch(appActions.setLoading(true));
		dispatch(viewerActions.setVerseBounds(activeVerseId, newBounds));
		api.put("/verses/" + activeVerseId, {
			...newBounds,
		})
			.then(() => {
				setDrawing(false);
				dispatch(viewerActions.allowUndoDisallowRedo());
				dispatch(appActions.setLoading(false));
				setEditOverlayMode(true);
			})
			.catch(() => {
				dispatch(viewerActions.setVerseBounds(activeVerseId, ovBounds));
				dispatch(appActions.setLoading(false));
			});
	};
	const putCurrentVerseOverlayIntoApi = overlayId => {
		if (!wltOCD.getOverlayById(overlayId)) {
			// console.log('There is no overlay to put');
			return;
		}
		const ovBounds = wltOCD.getOverlayById(overlayId).getBounds(wltOCD.viewport);

		const upLeft = wltOCD.viewport.viewportToImageCoordinates(ovBounds.x, ovBounds.y);
		const botRight = wltOCD.viewport.viewportToImageCoordinates(
			ovBounds.x + ovBounds.width,
			ovBounds.y + ovBounds.height,
		);
		const newBounds = {
			upperLeftX: Math.round(upLeft.x),
			upperLeftY: Math.round(upLeft.y),
			lowerRightX: Math.round(botRight.x),
			lowerRightY: Math.round(botRight.y),
		};
		const activeVerseId = viewer.editor.verseActive;
		dispatch(appActions.setLoading(true));
		dispatch(viewerActions.setVerseBounds(activeVerseId, newBounds));
		api.put("/verses/" + activeVerseId, {
			...newBounds,
		})
			.then(() => {
				setDrawing(false);
				dispatch(viewerActions.allowUndoDisallowRedo());
				dispatch(appActions.setLoading(false));
				setEditOverlayMode(true);
			})
			.catch(() => {
				dispatch(viewerActions.setVerseBounds(activeVerseId, ovBounds));
				dispatch(appActions.setLoading(false));
			});
	};
	const putCurrentWordOverlayIntoApi = overlayId => {
		if (!wltOCD.getOverlayById(overlayId)) {
			// console.log('There is no overlay to put');
			return;
		}
		const ovBounds = wltOCD.getOverlayById(overlayId).getBounds(wltOCD.viewport);

		const upLeft = wltOCD.viewport.viewportToImageCoordinates(ovBounds.x, ovBounds.y);
		const botRight = wltOCD.viewport.viewportToImageCoordinates(
			ovBounds.x + ovBounds.width,
			ovBounds.y + ovBounds.height,
		);
		const newBounds = {
			upperLeftX: Math.round(upLeft.x),
			upperLeftY: Math.round(upLeft.y),
			lowerRightX: Math.round(botRight.x),
			lowerRightY: Math.round(botRight.y),
		};
		const verseId = viewer.editor.wordActive.verseId;
		const wordId = viewer.editor.wordActive.wordId;
		dispatch(appActions.setLoading(true));
		dispatch(viewerActions.setWordBounds(verseId, wordId, newBounds));
		api.put("/verses/" + verseId + "/words", [{
			id: wordId,
			boundingBox: newBounds,
		}])
			.then((response) => {
				setDrawing(false);
				dispatch(viewerActions.reloadVerses(response.data.verses, transcriptionId));
				dispatch(viewerActions.reloadLayerBlocks(response.data.blocks, selectedLayer));
				dispatch(viewerActions.allowUndoDisallowRedo());
				dispatch(appActions.setLoading(false));
				setEditOverlayMode(true);
			})
			.catch(() => {
				dispatch(viewerActions.setWordBounds(verseId, wordId, ovBounds));
				dispatch(appActions.setLoading(false));
			});
	};

	//////////////////////////
	// Draw new overlay
	////////////////////////////
	const setDrawOverlayMode = drawMode => {
		// get into drawing new overlayMode mode
		wltOCD.panVertical = drawMode === "";
		wltOCD.panHorizontal = drawMode === "";
		if (drawMode !== "") {
			if (drawMode !== "word") {
				dispatch(viewerActions.setVerseActive(null));
			}

			wltOCD.removeAllHandlers("canvas-press");
			if (drawMode === 'annotation-point') {
				wltOCD.addHandler("canvas-press", onCanvasPointPress);
				wltOCD.addHandler("canvas-drag", onCanvasPointDrag);
				//wltOCD.addHandler("canvas-drag-end", onCanvasAnnotationPointDragEnd);
				wltOCD.addHandler("canvas-release", onCanvasPointRelease);
			} else {
				wltOCD.addHandler("canvas-press", onCanvasPress);
				wltOCD.addHandler("canvas-drag", onCanvasDrag);
				wltOCD.addHandler("canvas-drag-end", drawMode === "verse" ? onCanvasVerseDragEnd : drawMode === "word" ? onCanvasWordDragEnd : drawMode === "annotation" ? onCanvasVerseDragEnd : onCanvasMusicDragEnd);
				wltOCD.addHandler("canvas-release", onCanvasRelease);
			}
		} else {
			wltOCD.removeAllHandlers("canvas-press");
			wltOCD.addHandler("canvas-press", canvasPressHandler);
			wltOCD.removeAllHandlers("canvas-drag");
			wltOCD.removeAllHandlers("canvas-drag-end");
			wltOCD.removeAllHandlers("canvas-release");

			overlaysAddOnMouseDown();
		}
	};
	// as alway press-> drag -> release
	const onCanvasPress = ev => {
		let pt = wltOCD.viewport.pointFromPixel(ev.position);
		pt.x = Math.max(pt.x, 0);
		pt.y = Math.max(pt.y, 0);
		pt.x = Math.min(pt.x, imageBounds.x);
		pt.y = Math.min(pt.y, imageBounds.y);
		setDrawing(true);
		createNewOverlay(pt, osdDrawMode);
	};
	const onCanvasDrag = ev => {
		dragged = true;
		let point = wltOCD.viewport.pointFromPixel(ev.position);
		let tX = Math.max(Math.min(point.x, newOverlayStartPoint.x), 0);
		let tY = Math.max(Math.min(point.y, newOverlayStartPoint.y), 0);

		let bX =
			point.x < newOverlayStartPoint.x
				? newOverlayStartPoint.x - Math.max(point.x, 0)
				: Math.min(point.x, imageBounds.x) - newOverlayStartPoint.x;

		let bY =
			point.y < newOverlayStartPoint.y
				? newOverlayStartPoint.y - Math.max(point.y, 0)
				: Math.min(point.y, imageBounds.y) - newOverlayStartPoint.y;
		let rect = new OpenSeadragon.Rect(tX, tY, bX, bY);

		wltOCD.updateOverlay(newOverlay, rect);
	};
	const onCanvasPointPress = ev => {
		let pt = wltOCD.viewport.pointFromPixel(ev.position);
		pt.x = Math.max(pt.x, 0);
		pt.y = Math.max(pt.y, 0);
		pt.x = Math.min(pt.x, imageBounds.x);
		pt.y = Math.min(pt.y, imageBounds.y);
		setDrawing(true);
		createNewPointOverlay(pt, osdDrawMode);
	};
	const onCanvasPointDrag = ev => {
		let point = wltOCD.viewport.pointFromPixel(ev.position);

		newOverlayStartPoint = { x: point.x, y: point.y };

		let rect = new OpenSeadragon.Rect(point.x, point.y, 0, 0);

		wltOCD.updateOverlay(newOverlay, rect);
	};
	const onCanvasAnnotationPointDragEnd = () => {
		if (newOverlay) {
			const point = wltOCD.viewport.viewportToImageCoordinates(newOverlayStartPoint.x, newOverlayStartPoint.y);

			const newBounds = {
				upperLeftX: point.x,
				upperLeftY: point.y,
				lowerRightX: point.x,
				lowerRightY: point.y,
			};
			dispatch(appActions.setLoading(true));
			const pageLayer = selectedLayer;
			const url = `/verses/${viewer.data.id}?layerId=${pageLayer}`;
			api.post(url, {
				boundingBox: newBounds,
				type: null,
			}).then(res => {
				wltOCD.removeOverlay(newOverlay);
				setDrawing(false);

				dispatch(viewerActions.reloadVerses(res.data.verses, transcriptionId));
				dispatch(viewerActions.reloadLayerBlocks(res.data.blocks, pageLayer));
				dispatch(viewerActions.setPageEmpty(false));

				let newestVerseId = -1;
				res.data.verses.forEach((el) => {
					if (el.id > newestVerseId) {
						newestVerseId = el.id;
					}
				});
				
				if (newestVerseId > -1) {
					onOverlayClick(newestVerseId);
				}
				
				dispatch(viewerActions.allowUndoDisallowRedo());
				dispatch(appActions.setLoading(false));
				newOverlay = null;
				onToggleDrawMode("");
			});
		}
	};
	const onCanvasPointRelease = () => {
		onCanvasAnnotationPointDragEnd();

		wltOCD.removeOverlay(newOverlay);
		newOverlay = null;

		setDrawing(false);
	};
	const onCanvasRelease = () => {
		if (dragged) {
			dragged = false;
		} else {
			wltOCD.removeOverlay(newOverlay);
			newOverlay = null;
		}
		setDrawing(false);
	};

	const onCanvasVerseDragEnd = () => {
		if (newOverlay) {
			const ovBounds = wltOCD.getOverlayById(newOverlay.id).getBounds(wltOCD.viewport);

			const upLeft = wltOCD.viewport.viewportToImageCoordinates(ovBounds.x, ovBounds.y);
			const botRight = wltOCD.viewport.viewportToImageCoordinates(
				ovBounds.x + ovBounds.width,
				ovBounds.y + ovBounds.height,
			);
			const newBounds = {
				upperLeftX: Math.round(upLeft.x),
				upperLeftY: Math.round(upLeft.y),
				lowerRightX: Math.round(botRight.x),
				lowerRightY: Math.round(botRight.y),
			};
			dispatch(appActions.setLoading(true));
			const pageLayer = selectedLayer;
			const url = `/verses/${viewer.data.id}?layerId=${pageLayer}`;
			api.post(url, {
				boundingBox: newBounds,
				type: null,
			}).then(res => {
				wltOCD.removeOverlay(newOverlay);
				setDrawing(false);

				dispatch(viewerActions.reloadVerses(res.data.verses, transcriptionId));
				dispatch(viewerActions.reloadLayerBlocks(res.data.blocks, pageLayer));
				dispatch(viewerActions.setPageEmpty(false));

				let newestVerseId = -1;
				res.data.verses.forEach((el) => {
					if (el.id > newestVerseId) {
						newestVerseId = el.id;
					}
				});
				
				if (newestVerseId > -1) {
					onOverlayClick(newestVerseId);
				}
				
				dispatch(viewerActions.allowUndoDisallowRedo());
				dispatch(appActions.setLoading(false));
				newOverlay = null;
				onToggleDrawMode("");
			});
		}
	};
	const onCanvasWordDragEnd = () => {
		if (newOverlay) {
			const ovBounds = wltOCD.getOverlayById(newOverlay.id).getBounds(wltOCD.viewport);

			const upLeft = wltOCD.viewport.viewportToImageCoordinates(ovBounds.x, ovBounds.y);
			const botRight = wltOCD.viewport.viewportToImageCoordinates(
				ovBounds.x + ovBounds.width,
				ovBounds.y + ovBounds.height,
			);
			const newBounds = {
				upperLeftX: Math.round(upLeft.x),
				upperLeftY: Math.round(upLeft.y),
				lowerRightX: Math.round(botRight.x),
				lowerRightY: Math.round(botRight.y),
			};
			dispatch(appActions.setLoading(true));
			const verseId = viewer.editor.verseActive;
			const url = `/verses/${verseId}/words`;
			api.post(url, [{
				content: "",
				boundingBox: newBounds,
			}]).then(res => {
				wltOCD.removeOverlay(newOverlay);
				setDrawing(false);

				dispatch(viewerActions.reloadVerses(res.data.verses));
				dispatch(viewerActions.reloadLayerBlocks(res.data.blocks, selectedLayer));
				dispatch(viewerActions.allowUndoDisallowRedo());
				dispatch(appActions.setLoading(false));
				newOverlay = null;
				onToggleDrawMode("");
			});
		}
	};
	const onCanvasMusicDragEnd = () => {
		if (newOverlay) {
			const ovBounds = wltOCD.getOverlayById(newOverlay.id).getBounds(wltOCD.viewport);

			const upLeft = wltOCD.viewport.viewportToImageCoordinates(ovBounds.x, ovBounds.y);
			const botRight = wltOCD.viewport.viewportToImageCoordinates(
				ovBounds.x + ovBounds.width,
				ovBounds.y + ovBounds.height,
			);
			const newBounds = {
				upperLeftX: Math.round(upLeft.x),
				upperLeftY: Math.round(upLeft.y),
				lowerRightX: Math.round(botRight.x),
				lowerRightY: Math.round(botRight.y),
			};
			dispatch(appActions.setLoading(true));
			const pageLayer = selectedLayer;
			const url = `/verses/${viewer.data.id}?layerId=${pageLayer}`;
			api.post(url, {
				boundingBox: newBounds,
				type: "MUSIC_NOTATION",
			}).then(res => {
				wltOCD.removeOverlay(newOverlay);
				setDrawing(false);

				dispatch(viewerActions.reloadVerses(res.data.verses, transcriptionId));
				dispatch(viewerActions.reloadLayerBlocks(res.data.blocks, pageLayer));
				dispatch(viewerActions.setPageEmpty(false));

				let newestVerseId = -1;
				res.data.verses.forEach((el) => {
					if (el.id > newestVerseId) {
						newestVerseId = el.id;
					}
				});
				
				if (newestVerseId > -1) {
					onOverlayClick(newestVerseId);
				}

				dispatch(viewerActions.allowUndoDisallowRedo());
				dispatch(appActions.setLoading(false));
				newOverlay = null;
				onToggleDrawMode("");
			});
		}
	};
	const createNewOverlay = (point, drawMode) => {
		if (!newOverlay) {
			newOverlayStartPoint = { x: point.x, y: point.y };
			let tNewOverlay = document.createElement("div");
			tNewOverlay.id = "ovrly" + (Math.round(Math.random() * 1000) + 1000);
			tNewOverlay.className = "osd-overlay--highlight";
			if (drawMode === "word") {
				tNewOverlay.className = "osd-word-overlay--highlight";
			} else if (drawMode === "music") {
				tNewOverlay.className = "osd-music-overlay--highlight";
			} else if (drawMode === "annotation") {
				tNewOverlay.className = "osd-annotation-overlay--highlight";
			}

			osdAddOverlay(tNewOverlay, new OpenSeadragon.Rect(point.x, point.y, 0.001, 0.001), wltOCD, false);
			newOverlay = tNewOverlay;
		}
	};
	const createNewPointOverlay = (point, drawMode) => {
		if (!newOverlay) {
			newOverlayStartPoint = { x: point.x, y: point.y };
			let tNewOverlay = document.createElement("div");
			tNewOverlay.id = "ovrly" + (Math.round(Math.random() * 1000) + 1000);
			tNewOverlay.className = "osd-annotation-point-overlay--highlight";
			let rct = new OpenSeadragon.Rect(point.x, point.y, 0, 0);
			osdAddOverlay(tNewOverlay, rct, wltOCD, false);
			newOverlay = tNewOverlay;
		}
	};
	const resetOverlays = () => {
		if (wltOCD && viewer.data.verses.length) {
			for (let i = 0; i < viewer.data.verses.length; i++) {
				const verseElement = wltOCD.getOverlayById("ovrly" + viewer.data.verses[i].id);
				if (viewer.annotationLayer) {
					if (verseElement) {
						if (viewer.data.verses[i].boundingBox.lowerRightX === viewer.data.verses[i].boundingBox.upperLeftX && viewer.data.verses[i].boundingBox.lowerRightY === viewer.data.verses[i].boundingBox.upperLeftY) {
							if (editOverlayId.type === "annotation" && editOverlayId.id === viewer.data.verses[i].id) {
								setAnnotationPointEdited(editOverlayId.id);
							} else if (viewer.editor.verseActive === viewer.data.verses[i].id) {
								setAnnotationPointHighlighted(viewer.data.verses[i].id);
							} else {
								unsetAnnotationPoint(viewer.data.verses[i].id);
							}
						} else {
							if (editOverlayId.type === "annotation" && editOverlayId.id === viewer.data.verses[i].id) {
								setAnnotationEdited(editOverlayId.id);
							} else if (viewer.editor.verseActive === viewer.data.verses[i].id) {
								setAnnotationHighlighted(viewer.data.verses[i].id);
							} else {
								unsetAnnotation(viewer.data.verses[i].id);
							}
						}
					}
				} else {
					if (viewer.data.verses[i].type === null) {
						if (verseElement) {
							if (editOverlayId.type === "verse" && editOverlayId.id === viewer.data.verses[i].id) {
								setVerseEdited(editOverlayId.id);
							} else if (viewer.editor.verseActive === viewer.data.verses[i].id) {
								setVerseHighlighted(viewer.data.verses[i].id);
							} else {
								unsetVerse(viewer.data.verses[i].id);
							}
						}
						if (viewer.data.verses[i].words) {
							for(let j = 0; j < viewer.data.verses[i].words.length; ++j){
								const wordElement = wltOCD.getOverlayById("ovrly" + viewer.data.verses[i].id + "-" + viewer.data.verses[i].words[j].id);
								if (wordElement) {
									if (editOverlayId.type === "word" && editOverlayId.id.wordId === viewer.data.verses[i].words[j].id) {
										setWordEdited(viewer.data.verses[i].id, viewer.data.verses[i].words[j].id);
									} else if(viewer.editor.wordActive.wordId === viewer.data.verses[i].words[j].id) {
										setWordHighlighted(viewer.data.verses[i].id, viewer.data.verses[i].words[j].id);
									} else {
										unsetWord(viewer.data.verses[i].id, viewer.data.verses[i].words[j].id);
									}
								}
							}
						}
					} else if (viewer.data.verses[i].type === "MUSIC_NOTATION") {
						if (verseElement) {
							if(editOverlayId.type === "music" && editOverlayId.id === viewer.data.verses[i].id){
								setMusicEdited(editOverlayId.id);
							}else if(viewer.editor.verseActive === viewer.data.verses[i].id){
								setMusicHighlighted(viewer.data.verses[i].id);
							}else{
								unsetMusic(viewer.data.verses[i].id);
							}
						}
					}
				}
			}
			wltOCD.forceRedraw();
		}
	};

	useEffect(() => {
		if (wltOCD) {
			setOsdMethods({
				zoomInFunction: () => wltOCD.viewport.zoomBy(1.1),
				zoomOutFunction: () => wltOCD.viewport.zoomBy(0.9),
				fullScreen: () => wltOCD.setFullScreen(!wltOCD.isFullPage()),
				fitToPage: () => wltOCD.viewport.goHome(),
			});
			setResetCalled(resetCalled + 1);
		}
	}, [wltOCD]);

	useEffect(() => {
		if (viewerInitialised) {
			setResetCalled(resetCalled + 1);
			wltOCD.raiseEvent('update-viewport');
		}
	}, [viewerInitialised, wltOCD]);

	return (
		<>
			<div className={`ocd-div viewer__ocd${drawing && osdDrawMode === 'annotation-point' ? " osd-drawing-point" : drawing ? " osd-drawing" : ""}`} id={`ocd${viewer.data.id}`} />
		</>
	);
};

ViewerOSD.propTypes = {
	onToggleDrawMode: PropTypes.func,
	osdDrawMode: PropTypes.string,
	setOsdMethods: PropTypes.func,
	setZoom: PropTypes.func,
	setHomeZoom: PropTypes.func,
};

export default ViewerOSD;

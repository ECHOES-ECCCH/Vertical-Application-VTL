import FileSaver from "file-saver";
import api from "@/services/api";

import store from '@/store/store';
import { appActions } from "@/store/actions/app.actions";
import { hideModal } from "@/store/actions/modal.actions";

import { CheckViewModePermission } from "@/permissions/ViewModes";

const isLoaded = value => {
	return {
		type: "VIEWER_ISLOADED",
		isLoading: !value,
		value,
	};
};

const setIsMobile = value => {
	return {
		type: "VIEWER_ISMOBILE",
		value,
	};
};

const isProcessing = value => {
	return {
		type: "VIEWER_ISPROCESSING",
		value,
	};
};

const setNoAccess = value => {
	return {
		type: "VIEWER_NOACCESS",
		value,
	};
};

const setShowTextStructure = value => {
	return {
		type: "VIEWER_SHOW_TEXT_STRUCTURE",
		value,
	};
};

const receiveData = data => {
	return {
		type: "VIEWER_RECEIVE_DATA",
		data,
	};
};

const receiveVerseFormatting = (data, verseId) => {
	return {
		type: "VIEWER_RECEIVE_VERSE_FORMATTING",
		data,
		verseId,
	};
};

const receiveInfoJson = data => {
	return {
		type: "VIEWER_RECEIVE_INFOJSON",
		data,
	};
};

const setVerseActive = (verseId, multiSelect) => {
	if(verseId == null){
		return {
			type: "VIEWER_RESET_VERSES_ACTIVE",
		};
	} else {
		if (multiSelect) {
			return {
				type: "VIEWER_SET_VERSES_ACTIVE",
				verseId,
			};
		} else {
			return {
				type: "VIEWER_SET_VERSE_ACTIVE",
				verseId,
			};
		}
	}
};

const setVerseActiveRange = (firstVerseNo, lastVerseNo) => {
	return {
		type: "VIEWER_SET_VERSES_ACTIVE_RANGE",
		firstVerseNo: firstVerseNo,
		lastVerseNo: lastVerseNo,
	};
};

const setVerseEdit = data => {
	return {
		type: "VIEWER_SET_VERSE_EDIT",
		data,
	};
};

const setWordActive = (value, multiSelect) => {
	if (multiSelect) {
		return {
			type: "VIEWER_SET_WORDS_ACTIVE",
			wordId: value.wordId,
			verseId: value.verseId,
		};
	} else {
		return {
			type: "VIEWER_SET_WORD_ACTIVE",
			wordId: value.wordId,
			verseId: value.verseId,
		};
	}
};

const setFirstWordInVerseActive = (value) => {
	return {
		type: "VIEWER_SET_FIRST_WORD_IN_VERSE_ACTIVE",
		verseId: value,
	};
};

const setWordEdit = data => {
	return {
		type: "VIEWER_SET_WORD_EDIT",
		data,
	};
};

const setSelectionEdit = data => {
	return {
		type: "VIEWER_SET_SELECTION_EDIT",
		data,
	};
};

const setStyleActive = data => {
	return {
		type: "VIEWER_SET_STYLE_ACTIVE",
		data,
	};
};

const setStyleCurrent = data => {
	return {
		type: "VIEWER_SET_STYLE_CURRENT",
		data,
	};
};

const setInsertCharacter = data => {
	return {
		type: "VIEWER_SET_INSERT_CHARACTER",
		data,
	};
};

const setTeiPbValue = data => {
	return {
		type: "VIEWER_SET_TEI_PB_VALUE",
		data,
	};
};

const setPageEmpty = data => {
	return {
		type: "VIEWER_SET_PAGE_EMPTY",
		data,
	};
};

const setVerseContent = (verseId, verseContent) => {
	return {
		type: "VIEWER_SET_VERSE_CONTENT",
		verseId,
		verseContent,
	};
};

const setVerseBounds = (verseId, bounds) => {
	return {
		type: "VIEWER_SET_VERSE_BOUNDS",
		verseId,
		bounds,
	};
};

const setWordBounds = (verseId, wordId, bounds) => {
	return {
		type: "VIEWER_SET_WORD_BOUNDS",
		verseId,
		wordId,
		bounds,
	};
};

const setVerseStudentContent = (verseId, verseContent) => {
	return {
		type: "VIEWER_SET_VERSE_STUDENT_CONTENT",
		verseId,
		verseContent,
	};
};

const setVerseStudentStatus = (verseId, verseStudentStatus) => {
	return {
		type: "VIEWER_SET_VERSE_STUDENT_STATUS",
		verseId,
		verseStudentStatus,
	};
};

const setVerseStatus = (verseId, verseStatus) => {
	return {
		type: "VIEWER_SET_VERSE_STATUS",
		verseId,
		verseStatus,
	};
};

const updateVerseCommentsCount = (verseId, verseCommentsCount) => {
	return {
		type: "VIEWER_UPDATE_VERSE_COMMENTS_COUNT",
		verseId,
		verseCommentsCount,
	};
};

const updateRemovedIndex = (verseId, wordId, indexId) => {
	return {
		type: "VIEWER_UPDATE_REMOVED_INDEX",
		verseId,
		wordId,
		indexId,
	};
};

const updateAddedIndex = (verseId, wordId, index) => {
	return {
		type: "VIEWER_UPDATE_ADDED_INDEX",
		verseId,
		wordId,
		index,
	};
};

const updateEditedIndex = (index) => {
	return {
		type: "VIEWER_UPDATE_EDITED_INDEX",
		index,
	};
};

const setCorrectTranscription = () => {
	return {
		type: "VIEWER_SET_CORRECT_TRANSCRIPTION",
	};
};

const clearTranscriptionData = () => {
	return {
		type: "CLEAR_TRANSCRIPTION_DATA",
	};
};

const disableUndoRedo = () => {
	return {
		type: "VIEWER_DISABLE_UNDO_AND_REDO",
	};
};

const updateUndoRedo = (canUndo, canRedo) => {
	return {
		type: "VIEWER_UPDATE_UNDO_AND_REDO",
		canUndo, 
		canRedo,
	};
};

const allowUndoDisallowRedo = () => {
	return {
		type: "VIEWER_ALLOW_UNDO_DISALLOW_REDO",
	};
};

const setShiftPressed = data => {
	return {
		type: "VIEWER_SET_SHIFT_PRESSED",
		data,
	};
};

const loadData = (transcriptionId, pageId) => {
	return dispatch => {
		dispatch(isLoaded(false));
		dispatch(setNoAccess(false));
		api.get(`/transcriptions/${transcriptionId}/pages/${pageId}`)
			.then(response => {
				let data = response.data;

				data.verses.sort((verse1, verse2) => { 
					if(verse1.verseNo === verse2.verseNo){
						return verse1.pageLayer?.layerId - verse2.pageLayer?.layerId;
					}
					return verse1.verseNo - verse2.verseNo; 
				});

				if(data.studentVerses){
					data.studentVerses.sort((verse1, verse2) => { 
						if(verse1.verseNo === verse2.verseNo){
							return verse1.pageLayer?.layerId - verse2.pageLayer?.layerId;
						}
						return verse1.verseNo - verse2.verseNo; 
					});
				}

				let layerIdToNumber = new Map();
				let layerNumberToId = new Map();
	
				response.data.pageLayers.sort((a, b) => {
					return a.layer.id - b.layer.id;
				});

				let savedLayerData = localStorage.getItem("transcription" + transcriptionId + "LayerData");
				let currentActiveLayer;
				if(savedLayerData){
					let saveLayerDataObject = JSON.parse(savedLayerData);
					if(saveLayerDataObject.selectedLayer && response.data.pageLayers.find(l => { return l.layer.id == saveLayerDataObject.selectedLayer; })){
						dispatch(selectTranscriptionLayer(transcriptionId, saveLayerDataObject.selectedLayer, pageId));
						currentActiveLayer = saveLayerDataObject.selectedLayer;
					}else{
						dispatch(selectTranscriptionLayer(transcriptionId, response.data.pageLayers[0].layer.id, pageId));
						currentActiveLayer = response.data.pageLayers[0].layer.id;
					}
				}else{
					dispatch(selectTranscriptionLayer(transcriptionId, response.data.pageLayers[0].layer.id, pageId));
				}

				let layerNumber = 0;
				for(let singleLayer of response.data.pageLayers){
					let visibility = true;
					if(savedLayerData){
						let saveLayerDataObject = JSON.parse(savedLayerData);
						visibility = saveLayerDataObject?.layerVisibility?.[singleLayer.layer.id];
						if(visibility === undefined) { visibility = true; }
					}

					layerNumber++;
					layerIdToNumber.set(singleLayer.layer.id, layerNumber);
					layerNumberToId.set(layerNumber, singleLayer.layer.id);
					singleLayer.isShown = CheckViewModePermission("hasAdvancedLayerViewing", data.viewMode) && visibility || singleLayer.layer.id == currentActiveLayer;
					dispatch(selectTranscriptionLayerVisibility(transcriptionId, singleLayer.layer.id, singleLayer.isShown));
				}

				dispatch(setTranscriptionLayersData({idToNumber: layerIdToNumber, numberToId: layerNumberToId, layersNum: layerNumber, layers: response.data.pageLayers}));

				dispatch(changeWorkingPageLayer(1));
				dispatch(receiveData(data));
				dispatch(loadInfoJson(pageId));
			})
			.catch(error => {
				if (error.status === 403) {
					dispatch(setNoAccess(true));
				}
			});
	};
};

const loadDataLite = (transcriptionId, pageId) => {
	return dispatch => {
		api.get(`/transcriptions/${transcriptionId}/pages/${pageId}`)
			.then(response => {
				let data = response.data;

				data.verses.sort((verse1, verse2) => { 
					if(verse1.verseNo === verse2.verseNo){
						return verse1.pageLayer?.layerId - verse2.pageLayer?.layerId;
					}
					return verse1.verseNo - verse2.verseNo; 
				});

				if(data.studentVerses){
					data.studentVerses.sort((verse1, verse2) => { 
						if(verse1.verseNo === verse2.verseNo){
							return verse1.pageLayer?.layerId - verse2.pageLayer?.layerId;
						}
						return verse1.verseNo - verse2.verseNo; 
					});
				}

				let layerIdToNumber = new Map();
				let layerNumberToId = new Map();
	
				response.data.pageLayers.sort((a, b) => {
					return a.layer.id - b.layer.id;
				});

				let savedLayerData = localStorage.getItem("transcription" + transcriptionId + "LayerData");
				let currentActiveLayer;
				if(savedLayerData){
					let saveLayerDataObject = JSON.parse(savedLayerData);
					if(saveLayerDataObject.selectedLayer && response.data.pageLayers.find(l => { return l.layer.id == saveLayerDataObject.selectedLayer; })){
						dispatch(selectTranscriptionLayer(transcriptionId, saveLayerDataObject.selectedLayer, pageId));
						currentActiveLayer = saveLayerDataObject.selectedLayer;
					}else{
						dispatch(selectTranscriptionLayer(transcriptionId, response.data.pageLayers[0].layer.id, pageId));
						currentActiveLayer = response.data.pageLayers[0].layer.id;
					}
				}else{
					dispatch(selectTranscriptionLayer(transcriptionId, response.data.pageLayers[0].layer.id, pageId));
				}

				let layerNumber = 0;
				for(let singleLayer of response.data.pageLayers){
					let visibility = true;
					if(savedLayerData){
						let saveLayerDataObject = JSON.parse(savedLayerData);
						visibility = saveLayerDataObject?.layerVisibility?.[singleLayer.layer.id];
						if(visibility === undefined) { visibility = true; }
					}

					layerNumber++;
					layerIdToNumber.set(singleLayer.layer.id, layerNumber);
					layerNumberToId.set(layerNumber, singleLayer.layer.id);
					singleLayer.isShown = CheckViewModePermission("hasAdvancedLayerViewing", data.viewMode) && visibility || singleLayer.layer.id == currentActiveLayer;
					dispatch(selectTranscriptionLayerVisibility(transcriptionId, singleLayer.layer.id, singleLayer.isShown));
				}

				dispatch(setTranscriptionLayersData({idToNumber: layerIdToNumber, numberToId: layerNumberToId, layersNum: layerNumber, layers: response.data.pageLayers}));
				dispatch(receiveData(data));
			})
			.catch(error => {
				if (error.status === 403) {
					dispatch(setNoAccess(true));
				}
			});
	};
};

const loadInfoJson = pageId => {
	return dispatch => {
		api.get(`/iiif/pages/${pageId}/info.json`).then(response => {
			dispatch(receiveInfoJson(response.data));
			dispatch(isLoaded(true));
		}).catch(() => {});
	};
};

const deleteVerse = (verseId, nextVerseToSelect, layerId) => {
	return dispatch => {
		dispatch(appActions.setLoading(true));
		api.delete(`/verses/${verseId}`).then((response) => {
			dispatch(appActions.setLoading(false));
			dispatch(setVerseActive(nextVerseToSelect));
			dispatch(setVerseEdit(null));
			dispatch(reloadVerses(response.data.verses));
			dispatch(reloadLayerBlocks(response.data.blocks, layerId));
			dispatch(allowUndoDisallowRedo());
			dispatch(hideModal());
		}).catch(() => {
			dispatch(appActions.setLoading(false));
			dispatch(hideModal());
		});
	};
};

const deleteWord = (verseId, wordId, nextWordToSelect, layerId) => {
	return dispatch => {
		dispatch(appActions.setLoading(true));
		api.delete(`/verses/${verseId}/words`, {data: [wordId]}).then((response) => {
			dispatch(appActions.setLoading(false));
			dispatch(setWordActive(nextWordToSelect));
			dispatch(setWordEdit({verseId: null, wordId: null}));
			dispatch(reloadVerses(response.data.verses));
			dispatch(reloadLayerBlocks(response.data.blocks, layerId));
			dispatch(allowUndoDisallowRedo());
			dispatch(hideModal());
		}).catch(() => {
			dispatch(appActions.setLoading(false));
			dispatch(hideModal());
		});
	};
};

const deleteVerses = (versesIds, nextVerseToSelect, layerId) => {
	return dispatch => {
		dispatch(appActions.setLoading(true));
		api.delete('/verses', {data: versesIds}).then((response) => {
			dispatch(appActions.setLoading(false));
			dispatch(setVerseActive(nextVerseToSelect));
			dispatch(setVerseEdit(null));
			dispatch(reloadVerses(response.data.verses));
			dispatch(reloadLayerBlocks(response.data.blocks, layerId));
			dispatch(allowUndoDisallowRedo());
			dispatch(hideModal());
		}).catch(() => {
			dispatch(appActions.setLoading(false));
			dispatch(hideModal());
		});
	};
};

const deleteWords = (verseId, wordsIds, nextWordToSelect, layerId) => {
	return dispatch => {
		dispatch(appActions.setLoading(true));
		api.delete(`/verses/${verseId}/words`, {data: wordsIds}).then((response) => {
			dispatch(appActions.setLoading(false));
			dispatch(setWordActive(nextWordToSelect));
			dispatch(setWordEdit({verseId: null, wordId: null}));
			dispatch(reloadVerses(response.data.verses));
			dispatch(reloadLayerBlocks(response.data.blocks, layerId));
			dispatch(allowUndoDisallowRedo());
			dispatch(hideModal());
		}).catch(() => {
			dispatch(appActions.setLoading(false));
			dispatch(hideModal());
		});
	};
};

const submitVerseFormatting = (verseId, typeFormatting, layerId) => {
	return dispatch => {
		api.put(`/verses/${verseId}/${typeFormatting}`, "").then(response => {
			dispatch(setVerseEdit(null));
			dispatch(reloadVerses(response.data.verses));
			dispatch(reloadLayerBlocks(response.data.blocks, layerId));
			dispatch(allowUndoDisallowRedo());
		}).catch(() => {});
	};
};

const moveVerses = (versesIds, direction, layerId) => {
	return dispatch => {
		api.put(`/verses/${direction}`, versesIds).then(response => {
			dispatch(setVerseEdit(null));
			dispatch(reloadVerses(response.data.verses));
			dispatch(reloadLayerBlocks(response.data.blocks, layerId));
			dispatch({
				type: "VIEWER_SET_VERSES_ACTIVE_RANGE",
				firstVerseId: versesIds[0],
				lastVerseId: versesIds[versesIds.length - 1],
			});
			dispatch(allowUndoDisallowRedo());
		}).catch(() => {});
	};
};

const clearVerses = (layerId, versesIds) => {
	return dispatch => {
		api.put(`/verses/clear-content`, versesIds).then(response => {
			dispatch(setVerseEdit(null));
			dispatch(reloadVerses(response.data.verses));
			dispatch(reloadLayerBlocks(response.data.blocks, layerId));
			dispatch(allowUndoDisallowRedo());
		}).catch(() => {});
	};
};

const createNewBlock = (transcriptionId, pageId, layerId, blockType, firstVerseNo, lastVerseNo) => {
	return dispatch => {
		api.post(
			`/transcriptions/${transcriptionId}/pages/${pageId}/layers/${layerId}/blocks`, 
			{
				"type": blockType,
				"firstVerseNo": firstVerseNo < lastVerseNo ? firstVerseNo : lastVerseNo,
				"lastVerseNo": firstVerseNo < lastVerseNo ? lastVerseNo : firstVerseNo,
			},
		).then(response => {
			dispatch(reloadLayerBlocks(response.data, layerId));
			dispatch(allowUndoDisallowRedo());
		}).catch(() => {});
	};
};

const deleteBlock = (transcriptionId, pageId, layerId, blockId) => {
	return dispatch => {
		api.delete(
			`/transcriptions/${transcriptionId}/pages/${pageId}/layers/${layerId}/blocks/${blockId}`,
		).then(response => {
			dispatch(reloadLayerBlocks(response.data, layerId));
			dispatch(allowUndoDisallowRedo());
		}).catch(() => {});
	};
};

const submitVerseContent = (verseId, verseContent, transcriptionId, pageId, layerId) => {
	return dispatch => {
		api.put(`/verses/${verseId}/modify-content`, JSON.stringify(verseContent.replace(/\n/g, "").toString())).then(response => {
			dispatch(setVerseEdit(null));
			dispatch(setVerseContent(verseId, verseContent.replace(/\n/g, "")));
			dispatch(reloadVerses(response.data.verses));
			dispatch(reloadLayerBlocks(response.data.blocks, layerId));
			dispatch(allowUndoDisallowRedo());
		}).catch(() => {});
	};
};

const submitVerseStudentContent = (verseId, verseContent) => {
	return dispatch => {
		dispatch(setVerseStudentStatus(verseId, "none"));
		api.put(`/verses/${verseId}/student-content`, verseContent.replace(/\n/g, "")).then(({ data }) => {
			dispatch(setVerseActive(null));
			dispatch(setVerseEdit(null));
			dispatch(setVerseStudentContent(verseId, verseContent.replace(/\n/g, "")));
			dispatch(setVerseStudentStatus(verseId, data));
		}).catch(() => {});
	};
};

const submitWordContent = (transcriptionId, layerId, verseId, wordId, wordContent) => {
	return dispatch => {
		api.put(`/verses/${verseId}/words`, JSON.stringify([{id: wordId, content: wordContent.replace(/\n/g, "")}])).then(response => {
			dispatch(setWordEdit({verseId: null, wordId: null}));
			dispatch(reloadVerses(response.data.verses));
			dispatch(reloadLayerBlocks(response.data.blocks, layerId));
			dispatch(allowUndoDisallowRedo());
		}).catch(() => {});
	};
};

const submitCorrectTranscription = pageId => {
	return dispatch => {
		api.put(`/verses/${pageId}/change-all-status`).then(() => {
			dispatch(setVerseActive(null));
			dispatch(setVerseEdit(null));
			dispatch(setCorrectTranscription());
		}).catch(() => {});
	};
};

const submitAutoTranscribeVerses = (transcriptionId, layerId, versesIds, langs) => {
	return dispatch => {
		dispatch(appActions.setLoading(true));
		api.put(`/verses/content-recognition`, {verseIds: versesIds, languageCodes: langs})
			.then(response => {
				dispatch(appActions.setLoading(false));
				dispatch(setVerseActive(null));
				dispatch(setVerseEdit(null));
				dispatch(reloadVerses(response.data.verses));
				dispatch(reloadLayerBlocks(response.data.blocks, layerId));
				dispatch(allowUndoDisallowRedo());
				dispatch(hideModal());
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
				dispatch(hideModal());
			});
	};
};

const submitAutoTranscribePage = (transcriptionId, pageId, langs, ocrMode, handleColumns, selectedLayer) => {
	return dispatch => {
		const lang = encodeURIComponent(langs.join('+'));
		dispatch(appActions.setLoading(true));
		api.put(
			`/transcriptions/${transcriptionId}/pages/${pageId}/recognize`
				+ `?lang=${lang}`
				+ `&layerId=${selectedLayer}`
				+ `&ocrMode=${ocrMode}`
				+ `&handleColumns=${handleColumns && ocrMode !== "UPDATE"}`,
		)
			.then(response => {
				dispatch(appActions.setLoading(false));
				dispatch(setVerseActive(null));
				dispatch(setVerseEdit(null));
				dispatch(reloadVerses(response.data.verses));
				dispatch(reloadLayerBlocks(response.data.blocks, selectedLayer));
				dispatch(setPageEmpty(false));
				dispatch(checkRedoAndUndoPossibility(selectedLayer, pageId));
				dispatch(hideModal());
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
				dispatch(hideModal());
			});
	};
};

const getFileByFormat = (pageId, format) => {
	return () => {
		api.get(`/pages/${pageId}/content?type=${format}`, { responseType: "blob" }).then(response => {
			let fileName = response.headers["content-disposition"].split("filename=")[1];
			FileSaver.saveAs(response.data, fileName);
		}).catch(() => {});
	};
};

const reloadVerses = (verses) => {
	return dispatch => {
		verses.sort((verse1, verse2) => { 
			if(verse1.verseNo === verse2.verseNo){
				return verse1.pageLayer.layerId - verse2.pageLayer.layerId;
			}
			return verse1.verseNo - verse2.verseNo; 
		});
		dispatch(reloadVersesAfterAddingIndexes(verses));
	};
};

const reloadLayerBlocks = (newBlocks, layerId) => {
	return {
		type: "VIEWER_RELOAD_BLOCKS",
		newBlocks,
		layerId,
	};
};
	
const reloadVersesAfterAddingIndexes = verses => {
	return {
		type: "VIEWER_RELOAD_VERSES",
		verses,
	};
};

const submitVersesStatus = (versesIds, verseStatus, layerId) => {
	return dispatch => {
		api.put(`/verses/status?newStatus=${verseStatus}`, versesIds)
			.then(function (response) {
				dispatch(reloadVerses(response.data.verses));
				dispatch(reloadLayerBlocks(response.data.blocks, layerId));
			})
			.catch(function () {
			});
	};
};

const changeWorkingPageLayer = workingPageLayer => {
	return {
		type: "VIEWER_CHANGE_WORKING_PAGE_LAYER",
		workingPageLayer,
	};
};

const setLayerVisibility = (layerId, value) => {
	return {
		type: "VIEWER_SET_LAYER_VISIBILITY",
		layerId,
		value,
	};
};

const setSingleVisibleLayer = layerId => {
	return {
		type: "VIEWER_SET_SINGLE_VISIBLE_LAYER",
		layerId,
	};
};

const reloadVersesAfterChangingVisibility = verses => {
	return {
		type: "VIEWER_RELOAD_VERSES_AFTER_CHANGING_VISIBILITY",
		verses,
	};
};

const searchTranscriptionForPhrase = (transcriptionId, phrase) => {
	return dispatch => {
		api.get(`/iiif/transcriptions/${transcriptionId}/search?q=${phrase}`)
			.then(response => {
				let totalHits = 0;
				let pages = new Map();
			
				if(response.data.resources){
					for(let i = 0; i < response.data.resources.length; ++i){
						let id = response.data.resources[i]["@id"];
						let verseId = id.split("/").pop();

						let on = response.data.resources[i]["on"];
						let pageId = on.split("#")[0].split("/").pop();

						totalHits++;

						if(pages.has(pageId)){
							pages.get(pageId).verses.push(verseId);
						}else{
							pages.set(pageId, {verses:[verseId]});
						}
					}
				}

				let results = {
					totalHits: totalHits,
					pages: pages,
				};

				dispatch(setSearchResults(phrase, results));
			})
			.catch(() => {});
	};
};

const setSearchResults = (phraseSearchedFor, results) => {
	return {
		type: "VIEWER_SET_SEARCH_RESULTS",
		phraseSearchedFor,
		results,
	};
};

const getTranscriptionPagesData = (transcriptionId) => {
	return dispatch => {
		api.get(`/transcriptions/${transcriptionId}/pages?fields=id,page-no`)
			.then(response => {
				let idToPageno = new Map();
				let pagenoToId = new Map();

				for(const singlePage of response.data){
					idToPageno.set(singlePage.id, singlePage.pageNo);
					pagenoToId.set(singlePage.pageNo, singlePage.id);
				}

				dispatch(setTranscriptionPagesData({idToPage: idToPageno, pageToId: pagenoToId, pageNum: idToPageno.size}));
			})
			.catch(() => {});
	};
};

const setTranscriptionPagesData = (transcriptionPagesData) => {
	return {
		type: "VIEWER_SET_TRANSCRIPTION_PAGES_DATA",
		transcriptionPagesData,
	};
};

const setTranscriptionLayersData = (transcriptionLayersData) => {
	return {
		type: "VIEWER_SET_TRANSCRIPTION_LAYERS_DATA",
		transcriptionLayersData,
	};
};

const selectTranscriptionLayer = (transcriptionId, layerId, pageId) => {
	return dispatch => {
		let currentItem = localStorage.getItem("transcription" + transcriptionId + "LayerData");

		let newItem = { layerVisibility: {} };
		if(currentItem){
			newItem = JSON.parse(currentItem);
			newItem.selectedLayer = layerId;
		}else{
			newItem.selectedLayer = layerId;
		}
		localStorage.setItem("transcription" + transcriptionId + "LayerData", JSON.stringify(newItem));

		dispatch(setSelectedTranscriptionLayer(layerId));
		dispatch(checkRedoAndUndoPossibility(layerId, pageId));
	};
};

const setSelectedTranscriptionLayer = (selectedLayer) => {
	return {
		type: "VIEWER_SET_SELECTED_TRANSCRIPTION_LAYER",
		selectedLayer,
	};
};

const selectTranscriptionLayerVisibility = (transcriptionId, layerId, visibility) => {
	return dispatch => {
		let currentItem = localStorage.getItem("transcription" + transcriptionId + "LayerData");

		let newItem = { layerVisibility: {} };
		if(currentItem){
			newItem = JSON.parse(currentItem);
			newItem.layerVisibility[layerId] = visibility;
		}else{
			newItem.layerVisibility[layerId] = visibility;
		}
		localStorage.setItem("transcription" + transcriptionId + "LayerData", JSON.stringify(newItem));

		dispatch(setLayerVisibility(layerId, visibility));
	};
};

const setAllVersesOnPageAndLayerStatus = (transcriptionId, pageId, layerId, status) => {
	return dispatch => {
		api.put(`/verses/${pageId}/change-all-status?layerId=${layerId}&status=${status}`)
			.then((res) => {
				dispatch(reloadVerses(res.data.verses));
				dispatch(reloadLayerBlocks(res.data.blocks, layerId));
			})
			.catch(() => {});
	};
};

const checkRedoAndUndoPossibility = (layerId, pageId) => {
	if(layerId === null) { return () => {}; }

	return dispatch => {
		dispatch(disableUndoRedo());
		if (store.getState().auth.credentials.isLoggedIn) {
			api.get(`/pageLayers/layer/${layerId}/page/${pageId}/undo-redo`).then((response) => {
				dispatch(updateUndoRedo(response.data.canUndo, response.data.canRedo));
			}).catch(() => {});
		}
	};
};

const markWord = (wordIds, type, subtype, transcriptionId, layerId) => {
	if (type === "ADD") {
		return dispatch => {
			dispatch(appActions.setLoading(true));
			api.put(`/words/tei_elements`, {
				wordsIdentifiers: wordIds,
				teiElement: type,
				teiElementValue: subtype,
			}).then((res) => {
				dispatch(appActions.setLoading(false));
				dispatch(reloadVerses(res.data.verses));
				dispatch(reloadLayerBlocks(res.data.blocks, layerId));
			}).catch(() => {
				dispatch(appActions.setLoading(false));
			});
		};
	} else {
		return dispatch => {
			dispatch(appActions.setLoading(true));
			api.put(`/words/tei_elements`, {
				wordsIdentifiers: wordIds,
				teiElement: type,
			}).then((res) => {
				dispatch(appActions.setLoading(false));
				dispatch(reloadVerses(res.data.verses));
				dispatch(reloadLayerBlocks(res.data.blocks, layerId));
			}).catch(() => {
				dispatch(appActions.setLoading(false));
			});
		};
	}
};

const undo = (transcriptionId, pageId, selectedLayer) => {
	return dispatch => {
		dispatch(appActions.setLoading(true));
		api.put(`/pageLayers/layer/${selectedLayer}/page/${pageId}/undo`).then(() => {
			dispatch(checkRedoAndUndoPossibility(selectedLayer, pageId));
			dispatch(loadDataLite(parseInt(transcriptionId), parseInt(pageId)));
			dispatch(appActions.setLoading(false));
		}).catch(() => {
			dispatch(appActions.setLoading(false));
		});
	};
};
const redo = (transcriptionId, pageId, selectedLayer) => {
	return dispatch => {
		dispatch(appActions.setLoading(true));
		api.put(`/pageLayers/layer/${selectedLayer}/page/${pageId}/redo`).then(() => {
			dispatch(checkRedoAndUndoPossibility(selectedLayer, pageId));
			dispatch(loadDataLite(parseInt(transcriptionId), parseInt(pageId)));
			dispatch(appActions.setLoading(false));
		}).catch(() => {
			dispatch(appActions.setLoading(false));
		});
	};
};

const moveSelect = (verseActive, verseGroupActiveFirstNo, verseGroupActiveLastNo, shiftPressed, offset) => {
	if (shiftPressed) {
		if (verseActive) {
			return {
				type: "VIEWER_SET_VERSES_ACTIVE_RANGE",
				firstVerseId: verseActive,
				lastVerseId: verseActive,
				lastOffset: offset,
			};
		} else if (verseGroupActiveFirstNo !== null && verseGroupActiveLastNo !== null) {
			return {
				type: "VIEWER_SET_VERSES_ACTIVE_RANGE",
				firstVerseNo: verseGroupActiveFirstNo,
				lastVerseNo: verseGroupActiveLastNo,
				lastOffset: offset,
			};
		} else {
			return () => {};
		}
	} else {
		if (verseActive) {
			return {
				type: "VIEWER_SET_VERSES_ACTIVE_RANGE",
				firstVerseId: verseActive,
				lastVerseId: verseActive,
				firstOffset: offset,
				lastOffset: offset,
			};
		} else if (verseGroupActiveFirstNo !== null && verseGroupActiveLastNo !== null) {
			return {
				type: "VIEWER_SET_VERSES_ACTIVE_RANGE",
				firstVerseNo: verseGroupActiveLastNo,
				lastVerseNo: verseGroupActiveLastNo,
				firstOffset: offset,
				lastOffset: offset,
			};
		} else {
			return () => {};
		}
	}
};

const moveWordSelect = (verseId, wordActiveId, wordGroupActiveFirstNo, wordGroupActiveLastNo, shiftPressed, offset) => {
	if (shiftPressed) {
		if (wordActiveId) {
			return {
				type: "VIEWER_SET_WORDS_ACTIVE_RANGE",
				verseId: verseId,
				firstWordId: wordActiveId,
				lastWordId: wordActiveId,
				lastOffset: offset,
			};
		} else if (wordGroupActiveFirstNo !== null && wordGroupActiveLastNo !== null) {
			return {
				type: "VIEWER_SET_WORDS_ACTIVE_RANGE",
				verseId: verseId,
				firstWordNo: wordGroupActiveFirstNo,
				lastWordNo: wordGroupActiveLastNo,
				lastOffset: offset,
			};
		} else {
			return () => {};
		}
	} else {
		if (wordActiveId) {
			return {
				type: "VIEWER_SET_WORDS_ACTIVE_RANGE",
				verseId: verseId,
				firstWordId: wordActiveId,
				lastWordId: wordActiveId,
				firstOffset: offset,
				lastOffset: offset,
			};
		} else if (wordGroupActiveFirstNo !== null && wordGroupActiveLastNo !== null) {
			return {
				type: "VIEWER_SET_WORDS_ACTIVE_RANGE",
				verseId: verseId,
				firstWordNo: wordGroupActiveLastNo,
				lastWordNo: wordGroupActiveLastNo,
				firstOffset: offset,
				lastOffset: offset,
			};
		} else {
			return () => {};
		}
	}
};

export const viewerActions = {
	loadData,
	loadDataLite,
	submitVerseFormatting,
	moveVerses,
	clearVerses,
	createNewBlock,
	deleteBlock,
	deleteVerse,
	deleteWord,
	deleteVerses,
	deleteWords,
	setVerseBounds,
	setWordBounds,
	submitVerseContent,
	submitVerseStudentContent,
	submitWordContent,
	submitVersesStatus,
	submitCorrectTranscription,
	submitAutoTranscribeVerses,
	submitAutoTranscribePage,
	setVerseActive,
	setVerseActiveRange,
	setVerseEdit,
	setWordActive,
	setFirstWordInVerseActive,
	setWordEdit,
	setSelectionEdit,
	setStyleActive,
	setStyleCurrent,
	setInsertCharacter,
	setTeiPbValue,
	setPageEmpty,
	reloadVerses,
	reloadLayerBlocks,
	isLoaded,
	setIsMobile,
	isProcessing,
	setShowTextStructure,
	getFileByFormat,
	clearTranscriptionData,
	allowUndoDisallowRedo,
	setShiftPressed,
	updateVerseCommentsCount,
	updateRemovedIndex,
	updateAddedIndex,
	updateEditedIndex,
	receiveVerseFormatting,
	changeWorkingPageLayer,
	setLayerVisibility,
	setSingleVisibleLayer,
	reloadVersesAfterChangingVisibility,
	searchTranscriptionForPhrase,
	setSearchResults,
	getTranscriptionPagesData,
	setTranscriptionPagesData,
	setTranscriptionLayersData,
	selectTranscriptionLayer,
	setSelectedTranscriptionLayer,
	selectTranscriptionLayerVisibility,
	setAllVersesOnPageAndLayerStatus,
	checkRedoAndUndoPossibility,
	markWord,
	undo,
	redo,
	moveSelect,
	moveWordSelect,
};

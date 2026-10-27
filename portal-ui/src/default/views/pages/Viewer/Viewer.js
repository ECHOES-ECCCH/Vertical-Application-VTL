import React, { useEffect, useState, useCallback } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useParams, useHistory } from "react-router";
import PropTypes from "prop-types";

import { viewerActions } from '@/store/actions/viewer.actions';
import { appActions } from '@/store/actions/app.actions';

import ViewerOSD from "@Pages/Viewer/parts/ViewerOSD/ViewerOSD";
import ViewerOSDMenu from "@Pages/Viewer/parts/ViewerOSD/ViewerOSDMenu";

import ViewerEditor from "@Pages/Viewer/parts/ViewerEditor/ViewerEditor";
import ViewerEditorMenu from "@Pages/Viewer/parts/ViewerEditor/ViewerEditorMenu";

import Button from "@Elements/Button/Button";
import Error403 from "@Pages/Error/Error403";

import getTranscriptionProcessingStatus from '@/utils/transcriptionProcessingStatus';
import canBlockBeCreated from "@/utils/checkBlockCreation";

import { useShowModal, useModal, useHideModal } from '@Elements/Modal/ModalHooks';
import { CheckViewModePermission } from "@/permissions/ViewModes";

const ViewerTogglePanel = ({ type }) => {
	const dispatch = useDispatch();
	const { t } = useTranslation();

	const togglePanels = useSelector((state) => state.app.viewer.togglePanels);

	const togglePanel = (value) => {
		return value === 'osd' ?
			dispatch(appActions.setTogglePanelsOSD(!togglePanels.osd)) :
			dispatch(appActions.setTogglePanelsEditor(!togglePanels.editor));
	};

	return (
		<Button
			variant='viewer-full'
			className='viewer__button--fold'
			onClick={ () => togglePanel(type) }
		>
			{!togglePanels[type] ? (
				<span>{t('viewer.panels.fold')}</span>
			) : (
				<span>{t('viewer.panels.expand')}</span>
			)}
		</Button>
	);
};

ViewerTogglePanel.propTypes = {
	type: PropTypes.string,
};

const ViewerContentHeader = ({ panel }) => {
	const dispatch = useDispatch();
	const { t } = useTranslation();

	const { transcriptionId, pageId } = useParams();

	const [ showMobileLayerSelect, setShowMobileLayerSelect ] = useState(false);

	const isMobile = useSelector((state) => state.viewer.isMobile);
	const viewMode = useSelector((state) => state.viewer.data.viewMode);
	const student = viewMode === 'student';

	const mobilePanels = useSelector((state) => state.app.viewer.mobilePanels);
	const wordActive = useSelector((state) => state.viewer.editor.wordActive);
	const verseActive = useSelector((state) => state.viewer.editor.verseActive);
	const verseEdit = useSelector((state) => state.viewer.editor.verseEdit);
	const selectionEdit = useSelector((state) => state.viewer.ocd.selection);

	const layersData = useSelector((state) => state.viewer.transcriptionLayersData);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);

	const mobilePanel = (mode) => {
		dispatch(appActions.setMobilePanels(mode));
	};

	useEffect(() => {
		dispatch(viewerActions.clearTranscriptionData());
	}, [ dispatch ]);

	useEffect(() => {
		mobilePanel(student ? 'student' : 'osd');
	}, [ viewMode ]);

	useEffect(() => {
		if (!isMobile) {
			setShowMobileLayerSelect(false);
		}
	}, [ isMobile ]);

	useEffect(() => {
		if (selectionEdit) {
			// when change active verse leave edit mode
			dispatch(viewerActions.setSelectionEdit(false));
		}
	}, [ wordActive, verseActive ]);

	return (
		<>
			<Button
				variant='viewer-title'
				className={ `viewer__button--title viewer__button--title-${panel} ${mobilePanels === "osd" ? 'viewer__button--title-active' : ''}` }
				onClick={ () => mobilePanel('osd') }
			>
				{t('viewer.panels.title.osd')}
			</Button>
			<Button
				variant='viewer-title'
				className={ `viewer__button--title viewer__button--title-${panel} ${mobilePanels === "student" ? 'viewer__button--title-active' : ''} ${student ? 'viewer__button--student-mode' : 'd-none'}` }
				onClick={ () => mobilePanel('student') }
				disabled={ verseEdit }
			>
				{t('viewer.panels.title.student')}
			</Button>
			<Button
				variant='viewer-title'
				className={ `viewer__button--title viewer__button--title-${panel} ${mobilePanels === "editor" ? 'viewer__button--title-active' : ''} ${student ? 'viewer__button--student-mode' : ''}` }
				onClick={ () => { if(mobilePanels === 'editor' && isMobile) { setShowMobileLayerSelect(!showMobileLayerSelect); } else { mobilePanel('editor'); } } }
				disabled={ verseEdit }
			>
				{layersData.layers.map((value) => {
					if(value.layer.id == selectedLayer) {
						return <div key={value.layer.id} style={{ display: "flex", textOverflow: "ellipsis", overflow: "hidden", textWrap: "nowrap" }}>[{layersData.idToNumber.get(value.layer.id)}] { value.layer.name }</div>;
					}else{
						return "";
					}
				})}
				{isMobile && <i style={{marginLeft: 'auto'}} className={ showMobileLayerSelect ? 'icon-arrow-up-full' : 'icon-arrow-down-full' } />}
			</Button>
			{ !isMobile && panel === 'editor' && (
				<div className='viewer__editor-help'>
					<i className='icon-question-reversed' data-tooltip-id='custom-tooltip' data-tooltip-html={`
						${t('viewer.panels.editorHelp.start')}<br />
						${t('viewer.panels.editorHelp.nextVerse')}<br />
						${t('viewer.panels.editorHelp.previousVerse')}<br />
						${t('viewer.panels.editorHelp.nextVerse10')}<br />
						${t('viewer.panels.editorHelp.previousVerse10')}<br />
						${t('viewer.panels.editorHelp.firstVerse')}<br />
						${t('viewer.panels.editorHelp.lastVerse')}<br />
						${t('viewer.panels.editorHelp.addNextVerse')}<br />
						${t('viewer.panels.editorHelp.addPreviousVerse')}<br />
						${t('viewer.panels.editorHelp.addNext10Verses')}<br />
						${t('viewer.panels.editorHelp.addPrevious10Verses')}<br />
						${t('viewer.panels.editorHelp.addToFirstVerses')}<br />
						${t('viewer.panels.editorHelp.addToLastVerses')}<br />
						${t('viewer.panels.editorHelp.enterVerse')}<br />
						${t('viewer.panels.editorHelp.exitVerse')}<br />
						${t('viewer.panels.editorHelp.moveVersesDown')}<br />
						${t('viewer.panels.editorHelp.moveVersesUp')}<br />
						${t('viewer.panels.editorHelp.moveVerses10Down')}<br />
						${t('viewer.panels.editorHelp.moveVerses10Up')}<br />
						${t('viewer.panels.editorHelp.deleteVerses')}
						`} 
					/>
				</div>
			)}
			{showMobileLayerSelect && 
			<div className='viewer__mobile-layer-select'>
				{layersData.layers.map((value) => {
					return (
						<div 
							style={{ display: 'flex' }} 
							key={value.layer.id} 
							onClick={() => { 
								dispatch(viewerActions.selectTranscriptionLayer(transcriptionId, value.layer.id, pageId));
								dispatch(viewerActions.setSingleVisibleLayer(value.layer.id)); 
							}}
						>
							<div className='viewer__mobile-layer-select--selection-icon'>
								{ value.layer.id == selectedLayer ? <i className='icon-accept'/> : null }
							</div>
							<div className='viewer__mobile-layer-select--layer-name'>
								[{layersData.idToNumber.get(value.layer.id)}] 
								{value.layer.name}
							</div>
						</div>
					);
				})}
			</div>}
		</>
	);
};

ViewerContentHeader.propTypes = {
	panel: PropTypes.string,
};

const Viewer = () => {
	const [ osdDrawMode, setDrawMode ] = useState("");
	const [ osdMethods, setOsdMethods ] = useState({});
	const [ zoom, setZoom ] = useState(0);
	const [ homeZoom, setHomeZoom ] = useState(0);
	const [ dataLoaded, setDataLoaded ] = useState(false);
	const [ anyVerseActive, setAnyVerseActive ] = useState(false);
	const [ anyWordActive, setAnyWordActive ] = useState(false);
	const [ creatingBlockPossible, setCreatingBlockPossible ] = useState(false);

	const [ transcriptionProcessingStatus, setTranscriptionProcessingStatus ] = useState(false);
	const [ transcriptionProcessingStatusPrevious, setTranscriptionProcessingStatusPrevious ] = useState('');
	const [ transcriptionProcessingStatusRequest, setTranscriptionProcessingStatusRequest ] = useState(false);

	const dispatch = useDispatch();
	const history = useHistory();

	const { transcriptionId, pageId } = useParams();

	const { t } = useTranslation();

	const isLoggedIn = useSelector((state) => state.auth.credentials.isLoggedIn);
	const isLoaded = useSelector((state) => state.viewer.isLoaded);
	const authToken = useSelector((state) => state.auth.credentials.token);
	const noAccess = useSelector((state) => state.viewer.noAccess);
	const verses = useSelector(state => state.viewer.data.verses);
	
	const togglePanels = useSelector((state) => state.app.viewer.togglePanels);
	const swapPanels = useSelector((state) => state.app.viewer.swapPanels);
	const mobilePanels = useSelector((state) => state.app.viewer.mobilePanels);

	const redoPossible = useSelector((state) => state.viewer.editor.redoPossible);
	const undoPossible = useSelector((state) => state.viewer.editor.undoPossible);
	const isMobile = useSelector((state) => state.viewer.isMobile);
	const verseActive = useSelector((state) => state.viewer.editor.verseActive);
	const verseGroupActive = useSelector((state) => state.viewer.editor.verseGroupActive);
	const verseEdit = useSelector((state) => state.viewer.editor.verseEdit);
	const wordActive = useSelector((state) => state.viewer.editor.wordActive);
	const wordGroupActive = useSelector((state) => state.viewer.editor.wordGroupActive);
	const wordEdit = useSelector((state) => state.viewer.editor.wordEdit);
	const pageLayers = useSelector(state => state.viewer.transcriptionLayersData);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const modalActive = useSelector((state) => state.modal.show);
	const searchResults = useSelector((state) => state.viewer.searchResults);
	const annotationLayer = useSelector((state) => state.viewer.annotationLayer);

	const viewMode = useSelector((state) => state.viewer.data.viewMode);
	const student = viewMode === 'student';

	const [verseFromUrlSet, setVerseFromUrlSet] = useState(false);

	const readOnly = viewMode === 'read-only' || viewMode === 'student';

	const { modal } = useModal();
	const modalResize = useShowModal({
		title:t('modals.viewerResize.title'),
		bodyHTML:t('modals.viewerResize.body'),
		type: 'viewer',
		name: 'viewerResize',
		withFooter: true,
		actionButton:t('modals.viewerResize.button'),
	});

	const getVerseByNo = no => {
		for(let singleVerse of verses){
			if(+singleVerse.verseNo === +no) {
				return singleVerse;
			}
		}
		return false;
	};

	useEffect(() => {
		if (isLoaded) {
			if (verseActive) {
				setVerseActiveInUrl(verseActive);
			} else if (wordActive.verseId) {
				setVerseActiveInUrl(wordActive.verseId);
			} else if (verseGroupActive.firstVerseNo) {
				let firstVerse = getVerseByNo(verseGroupActive.firstVerseNo);
				if (firstVerse) {
					setVerseActiveInUrl(firstVerse.id);
				} else {
					setVerseActiveInUrl(null);
				}
			} else if (wordGroupActive.verseId) {
				setVerseActiveInUrl(wordGroupActive.verseId);
			} else {
				setVerseActiveInUrl(null);
			}
		}
	}, [verseActive, wordActive, verseGroupActive, wordGroupActive]);

	useEffect(() => {
		let layer;

		pageLayers.layers.map(layerToWork => {
			if(layerToWork.layer.id == selectedLayer) {
				layer = layerToWork;
			}
		});

		setAnyVerseActive(!!verseActive || verseGroupActive.firstVerseNo !== null);
		setAnyWordActive(!!wordActive.wordId || wordGroupActive.firstWordNo !== null);

		setCreatingBlockPossible(
			verseActive !== null || 
			verseGroupActive.firstVerseNo !== null && 
			canBlockBeCreated(
				layer.blocks,
				{ firstVerseNo: verseGroupActive.firstVerseNo, lastVerseNo: verseGroupActive.lastVerseNo },
			),
		);
	}, [ verseActive, verseGroupActive, selectedLayer ]);

	const { handleOnClose } = useHideModal();

	const setVerseActiveInUrl = (verseId) => {
		let currentUrlParams = new URLSearchParams(window.location.search);
		if (verseId){ currentUrlParams.set('verse', verseId); } else { currentUrlParams.delete('verse'); }
		history.replace(window.location.pathname + "?" + currentUrlParams.toString());
	};

	const handleResize = () => {
		const mobile = window.innerWidth < 1200;

		if (mobile) {
			dispatch(viewerActions.setIsMobile(true));
		} else {
			dispatch(viewerActions.setIsMobile(false));
		}
	};

	const onToggleDrawMode = useCallback((newDrawMode) => {
		setDrawMode(newDrawMode);
	}, [ dispatch ]);

	const toggleVerseEdit = useCallback(
		value => {
			dispatch(viewerActions.setVerseActive(value ? value : null));
			dispatch(viewerActions.setVerseEdit(value ? value : null));
		},
		[dispatch],
	);

	const toggleWordEdit = useCallback(
		value => {
			dispatch(viewerActions.setWordActive(value ? value : {verseId: null, wordId: null}));
			dispatch(viewerActions.setWordEdit(value ? value : {verseId: null, wordId: null}));
		},
		[dispatch],
	);

	const keyboardShortcutHandler = (event) => {
		let activeElement = document.activeElement;

		if(!wordEdit.wordId && !verseEdit && !modalActive && activeElement.nodeName !== 'INPUT') {
			if (event.key === "ArrowUp" || event.key === "ArrowDown" || event.key === "PageUp" || event.key === "PageDown" || event.key === "Home" || event.key === "End") {
				event.preventDefault();
			}
		
			if (!event.repeat && !readOnly) {
				if (event.ctrlKey && event.key === 'z' && undoPossible) {
					dispatch(viewerActions.undo(transcriptionId, pageId, selectedLayer));
				}
				if (event.ctrlKey && event.key === 'y' && redoPossible) {
					dispatch(viewerActions.redo(transcriptionId, pageId, selectedLayer));
				}
				if (annotationLayer) {
					if (event.key === 'w') {
						if (osdDrawMode === "annotation") {
							onToggleDrawMode("");
						} else {
							onToggleDrawMode("annotation");
						}
					}
					if (event.key === 's') {
						if (osdDrawMode === "annotation-point") {
							onToggleDrawMode("");
						} else {
							onToggleDrawMode("annotation-point");
						}
					}
				} else {
					if (verseActive && event.key === 's') {
						if (osdDrawMode === "word") {
							onToggleDrawMode("");
						} else {
							onToggleDrawMode("word");
						}
					}
					if (event.key === 'w') {
						if (osdDrawMode === "verse") {
							onToggleDrawMode("");
						} else {
							onToggleDrawMode("verse");
						}
					}
					if (event.key === 'n') {
						if (osdDrawMode === "music") {
							onToggleDrawMode("");
						} else {
							onToggleDrawMode("music");
						}
					}
				}
				if (verseActive) {
					if (event.key === 'c') {
						dispatch(viewerActions.submitVersesStatus([verseActive], 'none', selectedLayer));
					}
					if (event.key === 'r') {
						dispatch(viewerActions.submitVersesStatus([verseActive], 'transcribed', selectedLayer));
					}
					if (CheckViewModePermission("canAcceptAndRejectVerses", viewMode)) {
						if (event.key === 'a') {
							dispatch(viewerActions.submitVersesStatus([verseActive], 'verified', selectedLayer));
						}
						if (event.key === 'x') {
							dispatch(viewerActions.submitVersesStatus([verseActive], 'incorrect', selectedLayer));
						}
					}
				}
				if (verseGroupActive.firstVerseNo !== null && verseGroupActive.lastVerseNo !== null) {
					if (event.key === 'c') {
						dispatch(viewerActions.submitVersesStatus(verseGroupActive.activeVersesIds, 'none', selectedLayer));
					}
					if (event.key === 'r') {
						dispatch(viewerActions.submitVersesStatus(verseGroupActive.activeVersesIds, 'transcribed', selectedLayer));
					}
					if (CheckViewModePermission("canAcceptAndRejectVerses", viewMode)) {
						if (event.key === 'a') {
							dispatch(viewerActions.submitVersesStatus(verseGroupActive.activeVersesIds, 'verified', selectedLayer));
						}
						if (event.key === 'x') {
							dispatch(viewerActions.submitVersesStatus(verseGroupActive.activeVersesIds, 'incorrect', selectedLayer));
						}
					}
				}
			}
			if (event.keyCode === 107) {
				osdMethods.zoomInFunction();
			}
			if (event.keyCode === 109) {
				osdMethods.zoomOutFunction();
			}
			if (!event.repeat && !readOnly) {
				if (wordActive.wordId && event.keyCode === 13) {
					toggleWordEdit(wordActive);
				}
				if (verseActive && event.keyCode === 13) {
					toggleVerseEdit(verseActive);
				}
				if ((verseActive || wordActive.wordId || verseGroupActive.firstVerseNo !== null || wordGroupActive.firstWordNo !== null) && event.keyCode === 46) {
					removeSelectedArea();
				}
			}
			if (!event.repeat && !event.ctrlKey && event.key === 'f') {
				osdMethods.fullScreen();
			}
			if (!event.repeat && event.keyCode === 96) {
				osdMethods.fitToPage();
			}
			if(!event.repeat && verseActive && event.keyCode === 39) {
				dispatch(viewerActions.setFirstWordInVerseActive(verseActive));
			}
			if (!event.repeat && event.keyCode === 37) {
				if (wordActive.wordId !== null) {
					dispatch(viewerActions.setVerseActive(wordActive.verseId));
				} else if (wordGroupActive.verseId !== null) {
					dispatch(viewerActions.setVerseActive(wordGroupActive.verseId));
				}
			}
			if (!event.repeat && event.keyCode === 27) {
				dispatch(viewerActions.setVerseActive(null));
				dispatch(viewerActions.setWordActive({ verseId: null, wordId: null }));
			}
			if (event.altKey) {
				if (!event.repeat && !readOnly) {
					if (verseActive) {
						if (!event.shiftKey && event.keyCode === 38) {
							dispatch(viewerActions.submitVerseFormatting(verseActive, 'move-up', selectedLayer));
						} else if (!event.shiftKey && event.keyCode === 40) {
							dispatch(viewerActions.submitVerseFormatting(verseActive, 'move-down', selectedLayer));
						} else if (event.shiftKey && event.keyCode === 38) {
							dispatch(viewerActions.submitVerseFormatting(verseActive, 'move-up?times=10', selectedLayer));
						} else if (event.shiftKey && event.keyCode === 40) {
							dispatch(viewerActions.submitVerseFormatting(verseActive, 'move-down?times=10', selectedLayer));
						}
					} else if (verseGroupActive.firstVerseNo !== null) {
						if (!event.shiftKey && event.keyCode === 38) {
							dispatch(viewerActions.moveVerses(verseGroupActive.activeVersesIds, 'move-up', selectedLayer));
						} else if (!event.shiftKey && event.keyCode === 40) {
							dispatch(viewerActions.moveVerses(verseGroupActive.activeVersesIds, 'move-down', selectedLayer));
						} else if (event.shiftKey && event.keyCode === 38) {
							dispatch(viewerActions.moveVerses(verseGroupActive.activeVersesIds, 'move-up?times=10', selectedLayer));
						} else if (event.shiftKey && event.keyCode === 40) {
							dispatch(viewerActions.moveVerses(verseGroupActive.activeVersesIds, 'move-down?times=10', selectedLayer));
						}
					}
				}
			} else {
				if (verseActive || verseGroupActive.firstVerseNo != null) {
					if (event.keyCode === 38) {
						dispatch(viewerActions.moveSelect(verseActive, verseGroupActive.firstVerseNo, verseGroupActive.lastVerseNo, event.shiftKey, -1));
					} else if (event.keyCode === 40) {
						dispatch(viewerActions.moveSelect(verseActive, verseGroupActive.firstVerseNo, verseGroupActive.lastVerseNo, event.shiftKey, 1));
					} else if (event.key === "PageUp") {
						dispatch(viewerActions.moveSelect(verseActive, verseGroupActive.firstVerseNo, verseGroupActive.lastVerseNo, event.shiftKey, -10));
					} else if (event.key === "PageDown") {
						dispatch(viewerActions.moveSelect(verseActive, verseGroupActive.firstVerseNo, verseGroupActive.lastVerseNo, event.shiftKey, 10));
					} else if (event.key === "End") {
						dispatch(viewerActions.moveSelect(verseActive, verseGroupActive.firstVerseNo, verseGroupActive.lastVerseNo, event.shiftKey, 1000000));
					} else if (event.key === "Home") {
						dispatch(viewerActions.moveSelect(verseActive, verseGroupActive.firstVerseNo, verseGroupActive.lastVerseNo, event.shiftKey, -1000000));
					}
				} else if (wordActive.wordId || (wordGroupActive.firstWordNo != null && wordGroupActive.lastWordNo != null)) {
					let verseId = wordActive.verseId ? wordActive.verseId : wordGroupActive.verseId;
					if (event.keyCode === 38) {
						dispatch(viewerActions.moveWordSelect(verseId, wordActive.wordId, wordGroupActive.firstWordNo, wordGroupActive.lastWordNo, event.shiftKey, -1));
					} else if (event.keyCode === 40) {
						dispatch(viewerActions.moveWordSelect(verseId, wordActive.wordId, wordGroupActive.firstWordNo, wordGroupActive.lastWordNo, event.shiftKey, 1));
					} else if (event.key === "PageUp") {
						dispatch(viewerActions.moveWordSelect(verseId, wordActive.wordId, wordGroupActive.firstWordNo, wordGroupActive.lastWordNo, event.shiftKey, -10));
					} else if (event.key === "PageDown") {
						dispatch(viewerActions.moveWordSelect(verseId, wordActive.wordId, wordGroupActive.firstWordNo, wordGroupActive.lastWordNo, event.shiftKey, 10));
					} else if (event.key === "End") {
						dispatch(viewerActions.moveWordSelect(verseId, wordActive.wordId, wordGroupActive.firstWordNo, wordGroupActive.lastWordNo, event.shiftKey, 1000000));
					} else if (event.key === "Home") {
						dispatch(viewerActions.moveWordSelect(verseId, wordActive.wordId, wordGroupActive.firstWordNo, wordGroupActive.lastWordNo, event.shiftKey, -1000000));
					}
				}
			}
		}
	};

	useEffect(() => {
		if (isMobile) {
			modalResize();
		}
		if (!isMobile && modal['name'] === 'viewerResize') {
			handleOnClose();
		}
	}, [ isMobile ]);

	useEffect(() => {
		dispatch(viewerActions.getTranscriptionPagesData(parseInt(transcriptionId)));
	}, [true]);

	useEffect(() => {
		if (!dataLoaded && authToken) {
			dispatch(viewerActions.loadData(parseInt(transcriptionId), parseInt(pageId)));
			setDataLoaded(true);
		}
	}, [ isLoggedIn, pageId, transcriptionId, dispatch ]);

	useEffect(() => {
		if (!authToken && !dataLoaded) {
			dispatch(viewerActions.loadData(parseInt(transcriptionId), parseInt(pageId)));
			setDataLoaded(true);
		}
	}, [ authToken, dispatch ]);

	useEffect(() => {

		let mounted = true;

		if (transcriptionProcessingStatusRequest) {
			if (transcriptionProcessingStatus) {
				dispatch(viewerActions.isProcessing(true));

				setTimeout(() => {
					if (mounted) getTranscriptionProcessingStatus(transcriptionId, setTranscriptionProcessingStatus, setTranscriptionProcessingStatusRequest);
				}, 5 * 1000);
			} else {
				dispatch(viewerActions.isProcessing(false));
			}

		}

		return () => {
			mounted = false;
		};

	}, [ transcriptionProcessingStatusRequest ]);

	useEffect(() => {
		if (transcriptionProcessingStatusPrevious?.status === 'PROCESSING' && transcriptionProcessingStatus?.status !== 'PROCESSING') {
			dispatch(viewerActions.loadData(parseInt(transcriptionId), parseInt(pageId)));
			dispatch(viewerActions.isProcessing(false));
		}

		setTranscriptionProcessingStatusPrevious(transcriptionProcessingStatus);

	}, [ transcriptionProcessingStatus ]);

	useEffect(() => {
		handleResize();
		window.addEventListener('resize', handleResize);

		getTranscriptionProcessingStatus(transcriptionId, setTranscriptionProcessingStatus, setTranscriptionProcessingStatusRequest);

		return () => {
			dispatch(viewerActions.isLoaded(false));
			window.removeEventListener('resize', handleResize);
		};

	}, []);

	useEffect(() => {
		window.addEventListener('keydown', keyboardShortcutHandler);
		return () => {
			window.removeEventListener('keydown', keyboardShortcutHandler);
		};
	}, [osdMethods, osdDrawMode, selectedLayer, document.activeElement, verseActive, verseGroupActive, readOnly, transcriptionId, pageId, redoPossible, undoPossible]);

	useEffect(() => {
		if (isLoaded && !verseFromUrlSet) {
			let currentUrlParams = new URLSearchParams(window.location.search);
			const activeVerseId = currentUrlParams.get("verse");
			if (activeVerseId) {
				dispatch(viewerActions.setVerseActive(+activeVerseId, false));
			}
			setVerseFromUrlSet(true);
		}
	}, [isLoaded]);

	const removeSelectedArea = () => {
		if (verseActive && !verseEdit && !modalActive) {
			modalDeleteVerse();
		} else if (wordActive.wordId && !wordEdit.wordId && !modalActive) {
			modalDeleteWord();
		} else if (verseGroupActive.firstVerseNo !== null && !verseEdit && !modalActive) {
			modalDeleteSelectedVerses();
		} else if (wordGroupActive.firstWordNo !== null && !wordEdit.wordId && !modalActive) {
			modalDeleteSelectedWords();
		}
	};

	const modalDeleteVerse = useShowModal({
		title:t('modals.deleteVerse.title'),
		type: 'viewer',
		name: 'deleteVerse',
		componentPath: './parts/ModalDeleteVerse',
		componentProps: { transcriptionId: parseInt(transcriptionId) },
	});

	const modalDeleteSelectedVerses = useShowModal({
		title:t('modals.deleteSelectedVerses.title'),
		type: 'viewer',
		name: 'deleteSelectedVerses',
		componentPath: './parts/ModalDeleteSelectedVerses',
		componentProps: { transcriptionId: parseInt(transcriptionId) },
	});

	const modalDeleteWord = useShowModal({
		title:t('modals.deleteWord.title'),
		type: 'viewer',
		name: 'deleteWord',
		componentPath: './parts/ModalDeleteWord',
		componentProps: { transcriptionId: parseInt(transcriptionId) },
	});

	const modalDeleteSelectedWords = useShowModal({
		title:t('modals.deleteSelectedWords.title'),
		type: 'viewer',
		name: 'deleteSelectedWords',
		componentPath: './parts/ModalDeleteSelectedWords',
		componentProps: { transcriptionId: parseInt(transcriptionId) },
	});
	
	return (!isLoggedIn || !noAccess) ? (
		<main className={ `viewer ${swapPanels ? 'viewer--toggle' : ''}` }>
			{transcriptionProcessingStatus?.status === 'PROCESSING' && (
				<div className='viewer__alert'>
					<div className='viewer__alert--box'>
						{ t('transcription.alert.processing' + transcriptionProcessingStatus.details) }
					</div>
				</div>
			)}

			<div className={ `viewer__panel viewer__panel--osd ${(togglePanels.osd) ? 'viewer__panel--fold' : ''} ${(mobilePanels === 'osd') ? 'viewer__panel--mobile' : ''} ` }>
				<aside className='viewer__aside viewer__aside--left'>
					<div className='viewer__aside--container'>
						<div
							className='viewer__fold'
							data-tooltip-id='custom-tooltip' data-tooltip-content={ (!togglePanels.osd) ? t('viewer.tooltip.header.collapseObject') : t('viewer.tooltip.header.expandObject') }
							data-place={ (swapPanels && togglePanels.osd) ? 'left' : 'right' }
						>
							<ViewerTogglePanel type='osd' />
						</div>
						<div 
							className={`viewer__menu ${searchResults.searchedPhrase !== '' ? (isMobile ? 'viewer__menu--mobile-with-search' : 'viewer__menu--with-search') : ''}`}
						>
							<ViewerOSDMenu
								onToggleDrawMode={ onToggleDrawMode }
								removeSelectedArea={ removeSelectedArea }
								drawMode={ osdDrawMode }
								osdMethods={ osdMethods }
								zoom={ zoom }
								homeZoom={ homeZoom }
							/>
						</div>
					</div>
				</aside>
				<article className='viewer__content'>
					<div className='viewer__content--header'>
						<ViewerContentHeader
							panel='osd'
							osdDrawMode={ osdDrawMode }
							isMobile={ isMobile }
						/>
					</div>
					<div className='viewer__content--inner'>
						<ViewerOSD
							onToggleDrawMode={ onToggleDrawMode }
							osdDrawMode={ osdDrawMode }
							setOsdMethods={ setOsdMethods }
							setZoom={ setZoom }
							setHomeZoom={ setHomeZoom }
						/>
					</div>
				</article>
			</div>
			<div className={ `viewer__panel viewer__panel--editor ${togglePanels.editor ? 'viewer__panel--fold' : ''} ${(mobilePanels === 'editor' || mobilePanels === 'student') ? 'viewer__panel--mobile' : ''} ` }>
				<aside className='viewer__aside'>
					<div className='viewer__aside--container'>
						<div
							className='viewer__fold'
							data-tooltip-id='custom-tooltip' data-tooltip-content={ (!togglePanels.editor) ?t('viewer.tooltip.header.collapseTranscription') :t('viewer.tooltip.header.expandTranscription') }
							data-place={ (!swapPanels && togglePanels.editor) ? 'left' : 'right' }
						>
							<ViewerTogglePanel type='editor' />
						</div>
						<div 
							className={`viewer__menu ${searchResults.searchedPhrase !== '' ? (isMobile ? 'viewer__menu--mobile-with-search' : 'viewer__menu--with-search') : ''}`}
						>
							{(CheckViewModePermission("canSeeEditorMenu", viewMode) || (student && mobilePanels === 'student')) &&
							<ViewerEditorMenu anyVerseActive={ anyVerseActive } anyWordActive={ anyWordActive } canBlockBeCreated={ creatingBlockPossible } />}
						</div>
					</div>
				</aside>
				<article className='viewer__content'>
					<div className='viewer__content--header'>
						<ViewerContentHeader panel='editor' />
					</div>
					<div className='viewer__content--inner'>
						<ViewerEditor />
					</div>
				</article>
			</div>
		</main>
	) : (
		<Error403 />
	);

};

export default Viewer;
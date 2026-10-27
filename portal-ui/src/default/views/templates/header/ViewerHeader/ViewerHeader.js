import React, { useEffect, useState } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Link, useLocation, useHistory } from "react-router-dom";
import { reverse } from 'named-urls';
import { Dropdown } from 'react-bootstrap';
import { useParams } from 'react-router';

import { appActions } from '@/store/actions/app.actions';
import { viewerActions } from '@/store/actions/viewer.actions';

import api from "@/services/api";

import { useShowModal } from '@Elements/Modal/ModalHooks';
import Button from "@Elements/Button/Button";

import ViewerLayersChooser from "@Pages/Viewer/parts/ViewerLayers/ViewerLayersChooser";

import url from "@/router/urls";

import updateUrl from "@/utils/viewerHelper";

import LinkButton from "@Elements/Button/LinkButton";
import Spinner from "@Elements/Spinner/Spinner";
import AuthHeader from "@Templates/header/AuthHeader/AuthHeader";

import ModalWrapperChildren from "@Elements/Modal/ModalWrapperChildren";
import ModalHistoricalPageNumber from '@Elements/Modal/parts/ModalHistoricalPageNumber';
import ModalImportTextToLayer from '@Elements/Modal/parts/ModalImportTextToLayer';

import SearchInput from "@Templates/footer/ViewerFooter/SearchInput";
import HowToStartMenu from "@Templates/header/ViewerHeader/parts/HowToStartMenu";

import wlt_logo_pl from '@Assets/images/wlt_logo_small_pl.svg';
import wlt_logo_en from '@Assets/images/wlt_logo_small_en.svg';
import wlt_logo_de from '@Assets/images/wlt_logo_small_de.svg';

const ViewerHeader = () => {
	const viewer = useSelector((state) => state.viewer);
	const viewerData = useSelector((state) => state.viewer.data);
	const { t } = useTranslation();

	const isMobile = useSelector((state) => state.viewer.isMobile);
	const swapPanels = useSelector((state) => state.app.viewer.swapPanels);
	const transcriptionPagesData = useSelector((state) => state.viewer.transcriptionPagesData);
	const searchResults = useSelector((state) => state.viewer.searchResults);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const layersData = useSelector((state) => state.viewer.transcriptionLayersData);
	const dispatch = useDispatch();
	const location = useLocation();
	const history = useHistory();
	const { transcriptionId, pageId } = useParams();

	const [ layers, setLayers ] = useState();

	const viewMode = viewerData.viewMode;
	const readOnly = viewMode === 'read-only' || viewMode === 'student';
	const noAccess = useSelector( (state) => state.viewer.noAccess );

	const [ anyVerseVerified, setAnyVerseVerified ] = useState(false);
	const [ isMenuShown, setMenuShown ] = useState(false);
	const [ showTextStructureLoaded, setShowTextStructureLoaded] = useState(false);

	const [ pageNumber, setPageNumber ] = useState("");
	const [ currentPageNumber, setCurrentPageNumber ] = useState("");

	const [ isSettingHistoricalPageNumber, setIsSettingHistoricalPageNumber ] = useState(false);
	const [ isImportingTextToLayer, setIsImportingTextToLayer ] = useState(false);

	const [ searchedPhrase, setSearchedPhrase ] = useState(searchResults.searchedPhrase);

	const currentLang = useSelector((state) => state.app.currentLang);
	
	const wlt_logo = {
		'pl': wlt_logo_pl,
		'en': wlt_logo_en,
		'de': wlt_logo_de,
	};

	useEffect(() => {
		setSearchedPhrase(searchResults.searchedPhrase);
	}, [searchResults.searchedPhrase]);

	useEffect(() => {
		let new_layers = [];
		for(let i = 0; i < layersData.layers.length; ++i){
			new_layers.push(layersData.layers[i].layer);
		}
		setLayers(new_layers);
	}, [layersData]);

	const changeSwapPanels = () => {
		dispatch(appActions.setSwapPanels(!swapPanels));
	};

	const downloadFileFormat = (format) => {
		dispatch(viewerActions.getFileByFormat(viewerData.id, format));
	};

	const changeTextStructureVisibility = (visible) => {
		localStorage.setItem("showTextStructure", visible);
		dispatch(viewerActions.setShowTextStructure(visible));
	};

	const setPageAsNotEmpty = () => {
		api.put(`/pages/${pageId}/empty?empty=false`).then(() => {
			dispatch(viewerActions.setPageEmpty(false));
		});
	};

	const modalExportTextLayers = useShowModal({
		title:t('modals.viewerExportTextLayers.title'),
		type: 'viewer',
		name: 'viewerExportTextLayers',
		componentPath: './parts/ModalExportTextLayers',
		componentProps: {
			downloadFileFunction: downloadFileFormat,
		},
		withClose: true,
	});

	const modalAutoTranscribePage = useShowModal({
		title:t('modals.autoTranscribePage.titleSinglePage'),
		type: 'viewer',
		name: 'autoTranscribePage',
		componentPath: './parts/ModalAutoTranscribePage',
		componentProps: {
			transcriptionId: parseInt(transcriptionId),
			pageId: parseInt(pageId),
			anyVerseVerified: anyVerseVerified,
			viewMode: viewMode,
			layers: layers,
		},
	});

	const modalMarkTranscriptionPageAsEmpty = useShowModal({
		title: t('modals.markTranscriptionPageAsEmpty.title'),
		name: "markTranscriptionPageAsEmpty",
		type: 'viewer',
		componentPath: "./parts/ModalMarkTranscriptionPageAsEmpty",
		componentProps: {
			pageId: viewerData.id,
			handleOnComplete: () => {
				dispatch(viewerActions.setPageEmpty(true));
				dispatch(viewerActions.allowUndoDisallowRedo());
				dispatch(viewerActions.loadData(parseInt(transcriptionId), parseInt(pageId)));
			},
		},
	});

	useEffect(() => {
		if(!showTextStructureLoaded){
			if(localStorage.getItem("showTextStructure") === null) {
				localStorage.setItem("showTextStructure", viewer.showTextStructure);
			} else {
				changeTextStructureVisibility(JSON.parse(localStorage.getItem("showTextStructure")));
			}
			setShowTextStructureLoaded(true);
		}
	}, [showTextStructureLoaded]);

	useEffect(() => {
		const verses = [ ...viewerData.verses ];
		const versesVerified = verses.filter(item => item.status === 'verified');

		setAnyVerseVerified(!!versesVerified.length);
	}, [ viewerData.verses ]);

	useEffect(() => {
		if(viewerData.pageNo != currentPageNumber){
			setCurrentPageNumber(viewerData.pageNo ? viewerData.pageNo : "");
			setPageNumber(viewerData.pageNo ? viewerData.pageNo : "");
		}
	}, [viewerData.pageNo]);

	return !noAccess ? (
		<>
			<ModalWrapperChildren
				modalVisible={isSettingHistoricalPageNumber}
				setModalVisible={setIsSettingHistoricalPageNumber}
				title={t("modals.historicalPageNumber.title")}
				type='viewer'
			>
				<ModalHistoricalPageNumber
					handleOnClose={() => setIsSettingHistoricalPageNumber(false)}
					pageId={+pageId}
				/>
			</ModalWrapperChildren>
			<ModalWrapperChildren
				modalVisible={isImportingTextToLayer}
				setModalVisible={setIsImportingTextToLayer}
				title={t("modals.importTextToLayer.titleSinglePage")}
				type='viewer'
			>
				<ModalImportTextToLayer
					handleOnClose={() => setIsImportingTextToLayer(false)}
					transcriptionId={ parseInt(transcriptionId) }
					pageId={parseInt(pageId)}
					layers={layers}
				/>
			</ModalWrapperChildren>
			<Spinner
				stateFromStore
				viewer
			/>
			<header className='viewer-header'>
				<div className={`viewer-header__main ${isMobile && isMenuShown ? 'viewer-header__main--mobile-close' : '' }`}>
					{!isMobile && 
					<div
						className='viewer-header__logo'
						data-tooltip-id='custom-tooltip' data-tooltip-content={t('viewer.tooltip.header.homeBt') }
						data-place='right'
					>
						<Link
							to={ url.home }
							className='viewer-header__logo--href'
						>
							<img
								src={ wlt_logo[currentLang] }
								alt={t('headers.logoAlt') }
								className='viewer-header__logo--image'
							/>
						</Link>
					</div>}
					{isMobile &&
					<div className='viewer-header__nav--left'>
						<Dropdown
							className='viewer-header__dropdown'
							onToggle={(isShown) => { setMenuShown(isShown); }}
							show={isMenuShown}
						>
							<div
								className='viewer-btn__wrapper'
								data-place='bottom'
							>
								<Dropdown.Toggle
									className='viewer-header__menu-button'
								>
									{!isMenuShown && <i className='icon-hamburger' />}
									{isMenuShown && <i className='icon-close' />}
								</Dropdown.Toggle>
							</div>

							<Dropdown.Menu className='viewer-header__mobile-dropdown-menu'>
								{!readOnly && <Dropdown.Item onClick={ () => setIsSettingHistoricalPageNumber(true) }>{t('viewer.header.setHistoricalPageNumber')}</Dropdown.Item> }
								{!readOnly && <Dropdown.Item onClick={ () => viewerData.empty ? setPageAsNotEmpty() : modalMarkTranscriptionPageAsEmpty() }>{t('viewer.header.markAsEmpty')} {viewerData.empty && <i className='icon-accept' />}</Dropdown.Item> }
								{!readOnly && <Dropdown.Item onClick={ () => modalAutoTranscribePage() }>{t('viewer.header.autoTranscribe')}</Dropdown.Item> }
								<Dropdown.Item onClick={ () => modalExportTextLayers() }>{t('viewer.header.download')}</Dropdown.Item>
								{!readOnly && <Dropdown.Item onClick={ () => setIsImportingTextToLayer(true) }>{t('viewer.header.import')}</Dropdown.Item> }
								{!readOnly && <Dropdown.Item href={ reverse(url.transcription.layers, { transcriptionId: parseInt(transcriptionId) }) }>{t('viewer.header.layerManagement')}</Dropdown.Item> }
								<div className='dropdown-item'>
									<input type='checkbox' disabled={viewer.annotationLayer} checked={viewer.showTextStructure && !viewer.annotationLayer} onChange={(e)=>{ changeTextStructureVisibility(e.target.checked); }} />&nbsp;
									<span>{t('viewer.header.workWithTextStructure')}</span>
								</div>
								<div className='viewer-header__mobile-dropdown-menu__search-input'>
									<SearchInput searchedPhrase={searchedPhrase} setSearchedPhrase={setSearchedPhrase} onSearch={() => { setMenuShown(false); }}/>
								</div>
							</Dropdown.Menu>
						</Dropdown>
					</div>}
					<div className='viewer-header__title'>
						<p className='viewer-header__text--title'>
							{ 
								isMobile && isMenuShown 
									? 
									t("viewer.header.closeMenu")
									:
									viewerData.titleLabel ? viewerData.titleLabel + " [" + t('viewer.header.transcriptionPage') + " " + currentPageNumber + (viewerData.teiPbValue ? " | " + viewerData.teiPbValue : "") + "]" : ""
							}
						</p>
					</div>
					{!isMobile && 
					<div className='viewer-header__how-to-start'>
						<Dropdown align='end'>
							<Dropdown.Toggle
								id='dropdown-custom-11'
								className='viewer-header__how-to-start-dropdown__button'
								variant='viewer-header__how-to-start-dropdown__button'
							>{t('transcriptionHelpPage.howToTranscribe.title')}
							</Dropdown.Toggle>

							<HowToStartMenu />
						</Dropdown>
					</div>}
					{(!isMobile || isMobile && !isMenuShown) && 
					<div className='viewer-header__info'>
						<LinkButton
							variant='icon'
							className='viewer-header__close'
							onClick={()=>{ dispatch(viewerActions.setSearchResults("", { results: { resources: [], hits: [] } })); }}
							to={ `${reverse(url.transcription.details, { transcriptionId: transcriptionId })}${location.state && location.state.paginationPage ? `?page=${location.state.paginationPage}` : ''}` }
						>
							<span className='d-none'>X</span>
							<i className='icon-close' />
						</LinkButton>
					</div>}
				</div>
				{!isMobile &&
				<nav className='viewer-header__nav'>
					<div className='viewer-header__nav--left'>
						<Dropdown
							className='viewer-header__dropdown'
							onToggle={(isShown) => { setMenuShown(isShown); }}
							show={isMenuShown}
						>
							<div
								className='viewer-btn__wrapper'
								data-place='bottom'
							>
								<Dropdown.Toggle
									className='viewer-header__menu-button'
								>
									{!isMenuShown && <i className='icon-hamburger' />}
									{isMenuShown && <i className='icon-close' />}
									<span>{t('viewer.header.menu')}</span>
								</Dropdown.Toggle>
							</div>

							<Dropdown.Menu>
								<Dropdown.Item onClick={ changeSwapPanels }>{t('viewer.header.swapPanels')}</Dropdown.Item>
								{!readOnly && <Dropdown.Item onClick={ () => setIsSettingHistoricalPageNumber(true) }>{t('viewer.header.setHistoricalPageNumber')}</Dropdown.Item> }
								{!readOnly && <Dropdown.Item onClick={ () => viewerData.empty ? setPageAsNotEmpty() : modalMarkTranscriptionPageAsEmpty() }>{t('viewer.header.markAsEmpty')} {viewerData.empty && <i className='icon-accept' />}</Dropdown.Item> }
								{!readOnly && <Dropdown.Item onClick={ () => modalAutoTranscribePage() }>{t('viewer.header.autoTranscribe')}</Dropdown.Item> }
								<Dropdown.Item onClick={ () => modalExportTextLayers() }>{t('viewer.header.download')}</Dropdown.Item>
								{!readOnly && <Dropdown.Item onClick={ () => setIsImportingTextToLayer(true) }>{t('viewer.header.import')}</Dropdown.Item> }
								{!readOnly && <Dropdown.Item href={ reverse(url.transcription.layers, { transcriptionId: parseInt(transcriptionId) }) }>{t('viewer.header.layerManagement')}</Dropdown.Item> }
							</Dropdown.Menu>
						</Dropdown>
					</div>
					{!readOnly &&
					<div className='viewer-header__nav--left'>
						<div
							data-tooltip-id='custom-tooltip' data-tooltip-content={t('viewer.header.undo')}
						>
							<Button
								className='viewer__button--menu viewer-header__nav--redo-undo-button'
								variant='viewer-button--active'
								onClick={()=>{ dispatch(viewerActions.undo(transcriptionId, pageId, selectedLayer)); }}
								disabled={!viewer.editor.undoPossible}
							>
								<i className='icon-undo viewer__menu--icon' />
							</Button>
						</div>
						<div
							data-tooltip-id='custom-tooltip' data-tooltip-content={t('viewer.header.redo')}
						>
							<Button
								className='viewer__button--menu viewer-header__nav--redo-undo-button'
								variant='viewer-button--active'
								onClick={()=>{ dispatch(viewerActions.redo(transcriptionId, pageId, selectedLayer)); }}
								disabled={!viewer.editor.redoPossible}
							>
								<i className='icon-redo viewer__menu--icon' />
							</Button>
						</div>
					</div>}
					<div className='viewer-header__nav--left'>
						<div className='viewer-header__divider'/>
						<input id='textStructureVisibilityCheckbox' type='checkbox' disabled={viewer.annotationLayer} checked={viewer.showTextStructure && !viewer.annotationLayer} onChange={(e)=>{ changeTextStructureVisibility(e.target.checked); }} />&nbsp;
						<label htmlFor='textStructureVisibilityCheckbox' style={{fontSize: "0.875rem", marginBottom: '0px'}}>{t('viewer.header.workWithTextStructure')}</label>&nbsp;
						<i
							className='icon-question-reversed'
							data-tooltip-id='custom-tooltip' data-tooltip-content={ t("viewer.header.workWithTextStructureTooltip") }
							data-place='bottom'
						/>
						<div className='viewer-header__divider'/>
					</div>
					<div className='viewer-header__nav--left'>
						<ViewerLayersChooser/>
					</div>
					<div className='viewer-header__nav--right'>
						<div className='viewer-header__prev-page'>
							<span
								onClick={() => {
									if(viewerData.prevPageId){
										dispatch(viewerActions.loadData(parseInt(transcriptionId), parseInt(viewerData.prevPageId)));
										updateUrl(history, transcriptionId, viewerData.prevPageId, searchResults.searchedPhrase);
									}
								}}
							>
								<i className='icon-arrow-left-full'/>
								{t('viewer.header.pages.prev') + " " +t('viewer.header.pages.page') }
							</span>
						</div>
						<div className='viewer-header__page-number'>
							<input
								type='number'
								value={pageNumber || ""}
								onChange={e => { setPageNumber(e.target.value); }}
								onKeyDown={e => {
									if(e.key === 'Enter'){
										if(transcriptionPagesData.pageToId.get(+pageNumber)){
											dispatch(viewerActions.loadData(parseInt(transcriptionId), parseInt(transcriptionPagesData.pageToId.get(+pageNumber))));
											updateUrl(history, transcriptionId, transcriptionPagesData.pageToId.get(+pageNumber), searchResults.searchedPhrase);
										}
									}
								}}
							/>
							<span> {t('viewer.header.pages.fromNumber') + " " + transcriptionPagesData.pageNum }</span>
						</div>
						<div className='viewer-header__next-page'>
							<span
								onClick={() => {
									if(viewerData.nextPageId){
										dispatch(viewerActions.loadData(parseInt(transcriptionId), parseInt(viewerData.nextPageId)));
										updateUrl(history, transcriptionId, viewerData.nextPageId, searchResults.searchedPhrase);
									}
								}}
							>
								{t('viewer.header.pages.next') + " " +t('viewer.header.pages.page') }
								<i className='icon-arrow-right-full'/>
							</span>
						</div>
					</div>
				</nav>}
			</header>
		</>
	) : (
		<AuthHeader />
	);
};

export default ViewerHeader;
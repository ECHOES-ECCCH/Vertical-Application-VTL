import React, { useEffect, useState } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useHistory } from "react-router-dom";
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router';

import { viewerActions } from '@/store/actions/viewer.actions';

import updateUrl from "@/utils/viewerHelper";

import SearchInput from "@Templates/footer/ViewerFooter/SearchInput";

import PropTypes from 'prop-types';

const MobileViewerFooter = ({searchedPhrase, setSearchedPhrase}) => {
	const { t } = useTranslation();

	const viewerData = useSelector((state) => state.viewer.data);
	const searchResults = useSelector((state) => state.viewer.searchResults);
	const transcriptionPagesData = useSelector((state) => state.viewer.transcriptionPagesData);

	const [ pageNumber, setPageNumber ] = useState("");
	const [ currentPageNumber, setCurrentPageNumber ] = useState("");

	const dispatch = useDispatch();
	const history = useHistory();
	const { transcriptionId, pageId } = useParams();

	useEffect(() => {
		if(viewerData.pageNo != currentPageNumber){
			setCurrentPageNumber(viewerData.pageNo ? viewerData.pageNo : "");
			setPageNumber(viewerData.pageNo ? viewerData.pageNo : "");
		}
	}, [viewerData.pageNo]);

	const previousResultClick = () => {
		if(searchResults.pages.size === 0) { return; }

		let currentPageNo = transcriptionPagesData.idToPage.get(+pageId);
		let newPageNo = parseInt(currentPageNo);

		for(let i = 0; i < transcriptionPagesData.pageNum; ++i){
			if(newPageNo <= 1){
				newPageNo = transcriptionPagesData.pageNum;
			} else {
				newPageNo = newPageNo - 1;
			}
			if(searchResults.pages.has(transcriptionPagesData.pageToId.get(newPageNo) + "")){
				break;
			}
		}
		let newPageId = transcriptionPagesData.pageToId.get(+newPageNo);

		dispatch(viewerActions.loadData(transcriptionId, newPageId));
		updateUrl(history, transcriptionId, newPageId, searchedPhrase);
	};

	const nextResultClick = () => {
		if(searchResults.pages.size === 0) { return; }

		let currentPageNo = transcriptionPagesData.idToPage.get(+pageId);
		let newPageNo = parseInt(currentPageNo);

		for(let i = 0; i < transcriptionPagesData.pageNum; ++i){
			if(newPageNo >= transcriptionPagesData.pageNum){
				newPageNo = 1;
			} else {
				newPageNo = newPageNo + 1;
			}
			if(searchResults.pages.has(transcriptionPagesData.pageToId.get(newPageNo) + "")){
				break;
			}
		}
		let newPageId = transcriptionPagesData.pageToId.get(+newPageNo);

		dispatch(viewerActions.loadData(transcriptionId, newPageId));
		updateUrl(history, transcriptionId, newPageId, searchedPhrase);
	};

	return (
		<>
			{searchResults.searchedPhrase !== '' && (
				<>
					<div className='viewer-footer__search-results-mobile'>
						<span className='found-number'>{t('viewer.footer.searchResults.foundNumberPart1') + " " + searchResults.totalHits + " " +t('viewer.footer.searchResults.foundNumberPart2') + " " + searchResults.pages.size + " " +t('viewer.footer.searchResults.foundNumberPart3') + ": " + searchResults.searchedPhrase }</span>
						<SearchInput searchedPhrase={searchedPhrase} setSearchedPhrase={setSearchedPhrase} onSearch={() => {}}/>
						{searchResults.searchedPhrase !== '' && 
						<div className='move-between-results-mobile'>
							<span className='previous-result' onClick={ previousResultClick }>{t('viewer.footer.searchResults.previousResult') }</span>
							<span className='next-result' onClick={ nextResultClick }>{t('viewer.footer.searchResults.nextResult') }</span>
						</div>}
					</div>
				</>
			)}
			<div className='viewer-footer'>
				<div className='viewer-footer__pages'>
					<div
						className='viewer-footer__pages--move'
						onClick={() => {
							if(viewerData.prevPageId){
								dispatch(viewerActions.loadData(parseInt(transcriptionId), parseInt(viewerData.prevPageId)));
								updateUrl(history, transcriptionId, viewerData.prevPageId, searchResults.searchedPhrase);
							}
						}}
					>
						<i className='icon-arrow-left-full'/>
					</div>
					<div className='viewer-footer__pages--numbers'>
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
					<div
						className='viewer-footer__pages--move'
						onClick={() => {
							if(viewerData.nextPageId){
								dispatch(viewerActions.loadData(parseInt(transcriptionId), parseInt(viewerData.nextPageId)));
								updateUrl(history, transcriptionId, viewerData.nextPageId, searchResults.searchedPhrase);
							}
						}}
					>
						<i className='icon-arrow-right-full'/>
					</div>
				</div>
			</div>
		</>
	);
};

MobileViewerFooter.propTypes = {
	searchedPhrase: PropTypes.string, 
	setSearchedPhrase: PropTypes.func,
};

export default MobileViewerFooter;
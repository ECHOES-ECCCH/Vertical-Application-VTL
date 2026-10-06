import React from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useHistory } from "react-router-dom";
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router';

import { viewerActions } from '@/store/actions/viewer.actions';

import updateUrl from "@/utils/viewerHelper";

import SearchResultsBar from "@Templates/footer/ViewerFooter/SearchResultsBar";
import SearchInput from "@Templates/footer/ViewerFooter/SearchInput";
import PropTypes from 'prop-types';

const DesktopViewerFooter = ({searchedPhrase, setSearchedPhrase}) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();
	const history = useHistory();
	const searchResults = useSelector((state) => state.viewer.searchResults);
	const transcriptionPagesData = useSelector((state) => state.viewer.transcriptionPagesData);

	const { transcriptionId, pageId } = useParams();

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
					<SearchResultsBar searchedPhrase={searchedPhrase}/>
				</>
			)}
			<div className='viewer-footer'>
				{searchResults.searchedPhrase !== '' && (
					<>
						<span className='found-number'>{t('viewer.footer.searchResults.foundNumberPart1') + " " + searchResults.totalHits + " " +t('viewer.footer.searchResults.foundNumberPart2') + " " + searchResults.pages.size + " " +t('viewer.footer.searchResults.foundNumberPart3') + ": " + searchResults.searchedPhrase }</span>
						<span className='previous-result' onClick={ previousResultClick }>{t('viewer.footer.searchResults.previousResult') }</span>
					</>
				)}
				<SearchInput searchedPhrase={searchedPhrase} setSearchedPhrase={setSearchedPhrase} onSearch={() => {}}/>
				{searchResults.searchedPhrase !== '' && 
				<span className='next-result' onClick={ nextResultClick }>
					{t('viewer.footer.searchResults.nextResult') }
				</span>}
			</div>
		</>
	);
};

DesktopViewerFooter.propTypes = {
	searchedPhrase: PropTypes.string, 
	setSearchedPhrase: PropTypes.func,
};

export default DesktopViewerFooter;
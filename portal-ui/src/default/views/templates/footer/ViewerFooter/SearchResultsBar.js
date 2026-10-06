import React from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useHistory } from "react-router-dom";
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router';
import PropTypes from "prop-types";

import { viewerActions } from '@/store/actions/viewer.actions';

import updateUrl from "@/utils/viewerHelper";

const SearchResultsBar = ({searchedPhrase}) => {
	const { t } = useTranslation();

	const searchResults = useSelector((state) => state.viewer.searchResults);
	const transcriptionPagesData = useSelector((state) => state.viewer.transcriptionPagesData);

	const dispatch = useDispatch();
	const history = useHistory();

	const { transcriptionId, pageId } = useParams();

	const flagClick = (event) => {
		let selected_flags = document.getElementsByClassName("selected-flag");
		for(let selected_flag of selected_flags){
			selected_flag.classList.remove("selected-flag");
		}

		let parentNode = event.target.parentNode;
		parentNode.classList.add("selected-flag");

		let newPageId = parentNode.getAttribute("pagenum");

		updateUrl(history, transcriptionId, newPageId, searchedPhrase);
		dispatch(viewerActions.loadData(transcriptionId, newPageId));
	};

	return (
		<div className='viewer-footer__search-results'>
			<div className='search-line'>
				<div className='line' />
				<div className='end-line-left' />
				<div className='end-line-right' />
				<div className='page-flags'>
					{Array.from(searchResults.pages.entries()).map((entry) => {
						const [key, value] = entry; // eslint-disable-line

						let calculated_left = ((transcriptionPagesData.idToPage.get(+key) - 1) / (transcriptionPagesData.pageNum - 1) * 100);

						const flagStyle = {
							left: calculated_left + "%",
						};

						let classToSet = 'page-flag';
						if(pageId === key){
							classToSet += ' selected-flag';
						}
						if(calculated_left > 90){
							classToSet += ' flag-on-right';
						}

						return (
							<div className={classToSet} 
								id={"flag-" + key} 
								key={key}
								style={flagStyle}
								// eslint-disable-next-line react/no-unknown-property
								pagenum={key}
							>
								<div className='page-number'>{t('viewer.footer.searchResults.page') + " " + transcriptionPagesData.idToPage.get(+key) + " / " + transcriptionPagesData.pageNum }</div>
								<div className='flag' onClick={ flagClick }/>
							</div>
						);
					})}
				</div>
			</div>
		</div>
	);
};

SearchResultsBar.propTypes = {
	searchedPhrase: PropTypes.string,
};

export default SearchResultsBar;
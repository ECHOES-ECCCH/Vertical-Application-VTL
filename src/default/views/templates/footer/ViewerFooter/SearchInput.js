import React from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useHistory } from "react-router-dom";
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router';

import { viewerActions } from '@/store/actions/viewer.actions';

import updateUrl from "@/utils/viewerHelper";

import PropTypes from 'prop-types';

const SearchInput = ({searchedPhrase, setSearchedPhrase, onSearch}) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();
	const history = useHistory();

	const { transcriptionId, pageId } = useParams();

	const searchResults = useSelector((state) => state.viewer.searchResults);

	const clearSearchInput = () => {
		setSearchedPhrase("");
		dispatch(viewerActions.setSearchResults("", { results: { resources: [], hits: [] } }));
		updateUrl(history, transcriptionId, pageId, "");
	};

	const searchTranscription = (phrase) => {
		dispatch(viewerActions.searchTranscriptionForPhrase(transcriptionId, phrase));
		updateUrl(history, transcriptionId, pageId, phrase);
	};

	return (
		<div className='viewer-footer__search'>
			{searchResults.searchedPhrase !== '' && (
				<i className='icon-close-circle' style={{color: "#C9E2F1"}} onClick={ () => clearSearchInput() } />
			)}
			{searchResults.searchedPhrase === '' && (
				<div className='search-button' onClick={ () => { searchTranscription(searchedPhrase); onSearch(); } }>
					<i className='icon-search' />
				</div>
			)}
			<input
				type='text'
				className='viewer-footer__search--input'
				placeholder={t('viewer.footer.search')}
				value={searchedPhrase}
				onChange={e => {
					setSearchedPhrase(e.target.value);
				}}
				onKeyDown={e => {
					if(e.key === 'Enter'){
						searchTranscription(searchedPhrase);
						onSearch();
					}
				}}
			/>
		</div>
	);
};

SearchInput.propTypes = {
	searchedPhrase: PropTypes.string, 
	setSearchedPhrase: PropTypes.func,
	onSearch: PropTypes.func,
};

export default SearchInput;
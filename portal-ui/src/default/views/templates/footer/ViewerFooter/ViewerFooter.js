import React, { useEffect, useState } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useLocation, useHistory } from "react-router-dom";
import { useParams } from 'react-router';

import { viewerActions } from '@/store/actions/viewer.actions';

import updateUrl from "@/utils/viewerHelper";

import DesktopViewerFooter from "@Templates/footer/ViewerFooter/DesktopViewerFooter";
import MobileViewerFooter from "@Templates/footer/ViewerFooter/MobileViewerFooter";

const ViewerFooter = () => {
	const isMobile = useSelector((state) => state.viewer.isMobile);
	const noAccess = useSelector((state) => state.viewer.noAccess);
	const searchResults = useSelector((state) => state.viewer.searchResults);
	const dispatch = useDispatch();
	const location = useLocation();
	const history = useHistory();
	const { transcriptionId, pageId } = useParams();

	const [ searchedPhrase, setSearchedPhrase ] = useState("");
	const [ firstTimeOpen, setFirstTimeOpen ] = useState(true);

	const searchTranscription = (phrase) => {
		dispatch(viewerActions.searchTranscriptionForPhrase(transcriptionId, phrase));
		updateUrl(history, transcriptionId, pageId, phrase);
	};

	useEffect(() => {
		setSearchedPhrase(searchResults.searchedPhrase);
	}, [searchResults.searchedPhrase]);

	useEffect(()=>{
		let query = new URLSearchParams(location.search).get('q');
		if(query && firstTimeOpen){
			setSearchedPhrase(query);
			searchTranscription(query);
			setFirstTimeOpen(false);
		}
	},[location]);

	return !noAccess ? (
		isMobile ? 
			<MobileViewerFooter searchedPhrase={searchedPhrase} setSearchedPhrase={setSearchedPhrase} /> : 
			<DesktopViewerFooter searchedPhrase={searchedPhrase} setSearchedPhrase={setSearchedPhrase}/>
	) : null;
};

export default ViewerFooter;
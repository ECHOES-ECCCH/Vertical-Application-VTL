import React, { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { history } from "@/utils/history";

import Financing from "@Pages/Home/parts/Financed/Financing";
import MainDescription from "@Pages/Home/parts/MainDescription/MainDescription";
import HomeTranscriptions from "@Pages/Home/parts/HomeTranscriptions/HomeTranscriptions";
import SearchTranscriptions from "@Elements/Search/SearchTranscriptions";
import Alert from "@Elements/Alert/Alert";

import api from "@/services/api";
import { alertActions } from "@/store/actions/alert.actions";
import { appActions } from '@/store/actions/app.actions';

const Home = () => {
	const dispatch = useDispatch();
	const [ lastTranscriptions, setLastTranscriptions ] = useState({});
	const isLoggedIn = useSelector(state => state.auth.credentials.isLoggedIn);

	useEffect(() => {
		getLastTranscriptionsData();
	}, [ isLoggedIn, dispatch ]);
    
	const getLastTranscriptionsData = () => {
		dispatch(appActions.setLoading(true));
		api.get('/last-transcriptions')
			.then(({data}) => {                              	
				setLastTranscriptions(data);
				dispatch(appActions.setLoading(false));				
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
			});
	};

	useEffect(() => {
		if (window.location.search === '?userdeleted') {
			dispatch(alertActions.setAlert({
				type: "success",
				icon: "icon-accept",
				text: "auth.deleteUser.success",
				close: true,
			}));
			// remove the 'userdeleted' param from url
			history.replace(window.location.pathname);
		}
	}, []);

	return (
		<>
			<MainDescription />
			<Alert />
			<main>
				<SearchTranscriptions isHomePage/>

				{lastTranscriptions.length > 0 &&
					<HomeTranscriptions
						transcriptions={ lastTranscriptions } 
						homeComponent='lastTranscribed'
					/>}
			</main>
			<Financing />
		</>
	);
};

export default Home;

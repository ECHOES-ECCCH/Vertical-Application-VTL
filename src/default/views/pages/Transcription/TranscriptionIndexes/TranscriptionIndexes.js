import React, { useState, useEffect } from "react";
import { useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { useParams } from "react-router";
import { Container } from "react-bootstrap";
import { reverse } from "named-urls";

import url from "@/router/urls";
import api from "@/services/api";
import { appActions } from "@/store/actions/app.actions";
import Breadcrumbs from "@Elements/Breadcrumbs/Breadcrumbs";
import TranscriptionIndexesItemsByTag from "@Pages/Transcription/TranscriptionIndexes/parts/TranscriptionIndexesItemsByTag";
import TranscriptionBackLink from "@Pages/Transcription/parts/TranscriptionBackLink";
import TranscriptionIndexesProjectInfo from "@Pages/Transcription/TranscriptionIndexes/parts/TranscriptionIndexesProjectInfo";
import TranscriptionIndexesTag from "@Pages/Transcription/TranscriptionIndexes/parts/TranscriptionIndexesTag";

import SearchTranscriptions from "@Elements/Search/SearchTranscriptions";
import SearchTypeEnum from "@/utils/SearchTypeEnum";

import CommonDropdownValue from '@Elements/Dropdown/CommonDropdownValue';

import styles from "@Pages/Transcription/TranscriptionIndexes/TranscriptionIndexes.module.sass";

const TranscriptionIndexes = () => {
	const { t } = useTranslation();

	const dispatch = useDispatch();
	const { transcriptionId } = useParams();
	const [title, setTitle] = useState("");
	const [projectOwner, setProjectOwner] = useState("");
	const [pagesArray, setPagesArray] = useState([]);
	const [indexData, setIndexData] = useState({});
	const [cachedIndexData, setCachedIndexData] = useState({});
	const [chosenTag, setChosenTag] = useState(t('transcription.indexes.filters.availableTags', { returnObjects: true })[0].tag);
	const [indexesCount, setIndexesCount] = useState({});
	const [filters, setFilters] = useState();
	const [searchPhrase, setSearchPhrase] = useState("");
	const alphabet = "abcdefghijklmnopqrstuvwxyz".toUpperCase().split("");
	const [ selectedLetter, setSelectedLetter ] = useState("");
	const [ selectedPage, setSelectedPage ] = useState("");
	const [ searchString, setSearchString ] = useState("");
	const [ useMetadata, setUseMetadata ] = useState(SearchTypeEnum.ALL.toString());

	const TAG = {PERSON: "person", PLACE: "place", TERM: "term"};

	useEffect(() => {
		getTranscriptionData();
	}, []);

	useEffect(() => {
		getIndexesData(filters);
	}, [chosenTag]);

	useEffect(() => {
		if(cachedIndexData.length > 0) {
			const filteredIndexes = filterIndexes(cachedIndexData);
			const indexesByTag = filteredIndexes.filter(index => index.type === chosenTag);
			setIndexesCount(getIndexesCountPerTag(filteredIndexes));
			setIndexData(indexesByTag);
		}
	},[filters]);

	useEffect(()=>{
		searchForIndexes(searchPhrase);
	},[searchPhrase]);

	const getIndexesCountPerTag = (data) => {
		let personCount, placeCount, termCount;
		personCount = data.filter(index => index.type === TAG.PERSON).length;
		placeCount = data.filter(index => index.type === TAG.PLACE).length;
		termCount = data.filter(index => index.type === TAG.TERM).length;
		return {personCount, placeCount, termCount};
	};
	const resetFilters = () => {
		setFilters();
		setSearchPhrase("");
	};

	const filterIndexes = (indexes) => {
		let filteredIndexes = indexes;
		if (filters) {
			if(filters.letter){
				filteredIndexes = filteredIndexes.filter(index => index.name[0].toUpperCase() === filters.letter);
			}
			if(filters.page){
				filteredIndexes = filteredIndexes.filter(index => {
					for(let i =0; i < index.pages.length; i++){
						if(index.pages[i].pageNo === filters.page){
							return index;
						}
					}
				});
			}
			return filteredIndexes;
		}
		else{
			return filteredIndexes;
		}
		
	};

	const getIndexesData = (filters) => {
		dispatch(appActions.setLoading(true));
		api.get(`/transcriptions/${transcriptionId}/indexes`)
			.then(({ data }) =>
			{
				const filteredIndexes = filterIndexes(data);
				const indexesByTag = filteredIndexes.filter(index => index.type === chosenTag);
				setIndexesCount(getIndexesCountPerTag(filteredIndexes));
				setIndexData(indexesByTag);
				setCachedIndexData(data);
				dispatch(appActions.setLoading(false));
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
			});
	};

	const getTranscriptionData = async () => {
		dispatch(appActions.setLoading(true));
		await api.get(`/transcriptions/${transcriptionId}`)
			.then(({ data }) => {
				setTitle(data.analyticInfo.title);
				setProjectOwner(data.creator.email);
				let pages = [];
				for (let i = 1; i <= data.pages.hits; i++) {
					pages.push({ value: i, label: i });
				}
				setPagesArray(pages);
				dispatch(appActions.setLoading(false));
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
			});
	};

	const searchForIndexes = (searchPhrase) => {
		api.get(`/transcriptions/${transcriptionId}/indexes/search?searchPhrase=${searchPhrase}`)
			.then(({ data }) =>
			{
				const indexesByTag = data.filter(index => index.type === chosenTag);
				setIndexesCount(getIndexesCountPerTag(data));
				setIndexData(indexesByTag);
				setCachedIndexData(data);
				dispatch(appActions.setLoading(false));
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
			});
	};

	const breadcrumbsList = [
		{ id: 1, label: "home", link: url.home },
		{ id: 2, label:t('breadcrumbs.transcriptions'), link: url.transcriptions },
		{ id: 3, label: title, link: reverse(url.transcription.details, { transcriptionId: transcriptionId }) },
		{ id: 4, label:t('transcription.indexes.transcriptionIndex') },
	];

	const selectStyles = {
		control: styles => ({
			...styles,
			backgroundColor: "white",
			width: "150px",
			borderRadius: "20px",
			borderColor: "#161615",
			marginRight: "5px",
		}),
		option: (styles, { data, isDisabled, isFocused, isSelected }) => {
			return {
				...styles,
				cursor: isDisabled ? "not-allowed" : "default",
				backgroundColor: isFocused ? "#F6F6F6" : isSelected ? "#F6F6F6" : "white",
				color: "#161615",
			};
		},
	};

	return (
		<main>
			<SearchTranscriptions
				setSearchString={ setSearchString }
				searchString={ searchString }
				setUseMetadata={ setUseMetadata }
			/>
			<Container>
				<Breadcrumbs breadcrumbsList={breadcrumbsList} />
				<TranscriptionBackLink transcriptionId={transcriptionId} />

				<section className={styles["indexes"]}>
					<div>
						<h1 className={styles["indexes__title"]}>{t('transcription.indexes.title')}</h1>
					</div>
					<TranscriptionIndexesProjectInfo title={title} projectOwner={projectOwner} />
					<div className={styles["indexes__header"]}>
						<div className={styles["indexes__header--chosen-index"]}>
							{t('transcription.indexes.header.chosenIndex')}
						</div>
						<div className={styles["indexes__header--indexes-in-project"]}>
							{t('transcription.indexes.header.instancesInProject')}
						</div>
					</div>
					<section className={styles["indexes__table"]}>
						<div className={styles["indexes__table--aside"]}>
							<div className={styles["indexes__table--aside-element"]}>
								{t('transcription.indexes.filters.availableTags', { returnObjects: true }).map((tagToUse) => {
									const countToUse = tagToUse.tag === TAG.PERSON ? indexesCount.personCount: tagToUse.tag === TAG.PLACE ? indexesCount.placeCount : indexesCount.termCount;
									return (
										<TranscriptionIndexesTag onClick={()=> setChosenTag(tagToUse.tag)} tagText={tagToUse.label} key={tagToUse.tag} chosenTag={chosenTag}
											tag={tagToUse.tag} transcriptionId={transcriptionId} count={countToUse}
										/>
									);
								})}
							</div>
							<div className={styles["indexes__table--aside-element"]}>
								<label className={styles["indexes__filters--label"]}>
									{t('transcription.indexes.filters.letterLabel')}
								</label>
								<CommonDropdownValue
									options={
										[
											{value: "", label:t('transcription.indexes.filters.selectDefaultLetter') },
											...alphabet.map(letter => ({ value: letter, label: letter })),
										]
									} 
									selectedValue={selectedLetter}
									setSelectedValue={ (value)=>{ setSelectedLetter(value); setFilters({ ...filters, letter: value}); } }
									className={"dropdown-new " + styles["indexes__filters--dropdown-alphabet"] }
								/>
							</div>
							<div className={styles["indexes__table--aside-element"]}>
								<label className={styles["indexes__filters--label"]}>
									{t('transcription.indexes.filters.numberLabel')}
								</label> 
								<CommonDropdownValue
									options={
										[
											{value: "", label:t('transcription.indexes.filters.selectDefaultFile') },
											...pagesArray,
										]
									} 
									selectedValue={selectedPage}
									setSelectedValue={ (value)=>{ setSelectedPage(value); setFilters({ ...filters, page: value}); } }
									className={"dropdown-new " + styles["indexes__filters--dropdown-pages"] }
								/>
							</div>
						</div>
						<div className={ styles["indexes__table--content"] }>
							<div className={styles["indexes__table--search-input"]}>
								<i className='icon-search' />
								<input
									type='text'
									placeholder={
										chosenTag == "term" ?t('transcription.indexes.filters.searchTermPlaceholder') :
											chosenTag == "place" ?t('transcription.indexes.filters.searchPlacePlaceholder') :t('transcription.indexes.filters.searchPersonPlaceholder')
									}
									value={searchPhrase}
									onChange={e => {
										setSearchPhrase(e.target.value);
									}}
								/>
							</div>
						
							{indexData.length > 0 ?
								<TranscriptionIndexesItemsByTag indexes={ indexData } transcriptionId={transcriptionId}/>:
								<div style={{marginTop: "20px"}}>
									<div style={{fontWeight: "bold"}}>
										{chosenTag == "term" &&t('transcription.indexes.empty.term')}
										{chosenTag == "place" &&t('transcription.indexes.empty.place')}
										{chosenTag == "person" &&t('transcription.indexes.empty.person')}
									</div>
									<div>{t('transcription.indexes.empty.general')}</div>
								</div>}
						</div>
					</section>

				</section>
			</Container>
		</main>
	);
};

export default TranscriptionIndexes;

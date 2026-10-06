import React from 'react';
import { useState, useCallback, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useForm, useWatch } from 'react-hook-form';
import { Container } from "react-bootstrap";
import { useLocation, useHistory } from "react-router-dom";
import PropTypes from "prop-types";

import styles from '@Elements/Search/SearchTranscriptions.module.sass';
import url from "@/router/urls";
import SearchTypeEnum from "@/utils/SearchTypeEnum";
import Button from "@Elements/Button/Button";
import CommonDropdown from '@Elements/Dropdown/CommonDropdown';

const SearchTranscriptions = ({ searchString, setSearchString, isHomePage }) => {
	const [chosenFilter, setChosenFilter] = useState(0); //0-everywhere, 1-descriptions, 2-texts
	const [isFiltersHidden, setIsFiltersHidden] = useState(true);

	const isLoggedIn = useSelector(state => state.auth.credentials.isLoggedIn);

	const { t } = useTranslation();

	const history = useHistory();
	const { register, handleSubmit, formState, reset, setValue, control } = useForm({ mode: 'onSubmit', defaultValues: {searchText: searchString} });
	const location = useLocation();
	const watchVal = useWatch({ control, name: 'searchText' });

	useEffect(() => {
		setValue('searchText', searchString ? searchString : "");
	}, [searchString]);

	useEffect(() => {
		if (!isHomePage) {
			const usp = new URLSearchParams(location.search);
			const metadataFromUrl = usp.get("type");
			if (metadataFromUrl !== undefined) {
				let filter;
				switch (metadataFromUrl) {
					case SearchTypeEnum.ALL.toString():
						filter = 0;
						break;
					case SearchTypeEnum.METADATA.toString():
						filter = 1;
						break;
					case SearchTypeEnum.CONTENT.toString():
						filter = 2;
						break;

				}
				setChosenFilter(filter);
			}
		}
	}, [true]);

	const goToTranscriptions = (type) => {
		let destination;
		if (type === 'all') {
			destination = url.transcriptions;
		}
		else {
			destination = url.transcriptionsUser;
		}
		history.push(destination);
	};

	const handleFilterClick = (type) => {
		setChosenFilter(type);
		setIsFiltersHidden(!isFiltersHidden);
	};

	const handleClearSearch = () => {
		searchSubmit({ searchText: '' });
	};

	const searchSubmit = (data) => {
		if (data.searchText === '' && !isHomePage) {
			clearField();
		}
		if (isHomePage) {
			history.push(url.transcriptions + '?q=' + data.searchText + `&type=${chosenFilter === 0 ? SearchTypeEnum.ALL.toString() : chosenFilter === 1 ? SearchTypeEnum.METADATA.toString() : SearchTypeEnum.CONTENT.toString()}`);
		} else {
			setSearchString(data.searchText);
			history.push(url.transcriptions + '?q=' + data.searchText + `&type=${chosenFilter === 0 ? SearchTypeEnum.ALL.toString() : chosenFilter === 1 ? SearchTypeEnum.METADATA.toString() : SearchTypeEnum.CONTENT.toString()}`);
		}

	};

	const clearField = useCallback(() => {
		setSearchString('');
		reset();
	}, [setSearchString]);

	return (
		<>
			{isHomePage &&
				<section className={styles["search-section"]}>
					<Container>
						<h2 className={styles["search-section__header"]}>{t('main.search.title')}</h2>
						<p
							className={styles["search-section__additional-info"]}
							dangerouslySetInnerHTML={{ __html: t('main.search.additionalInfo') }}
						/>
					</Container>
				</section>}
			<div className={styles["search-section__wrapper"]}>
				<Container>
					<div className={styles["search-section__wrapper--container"]}>
						<form
							onSubmit={handleSubmit(searchSubmit)}
							noValidate
							className={styles['search-section__wrapper--container-form']}
						>
							<div className={styles["search-section__wrapper--container-inner-left"]}>

								<CommonDropdown
									className={styles["search-section__wrapper--container-dropdown"]}
									options={
										t('main.search.filters', { returnObjects: true }).map((filter) => (filter))
									}
									selectedOption={chosenFilter}
									setSelectedOption={handleFilterClick}
								/>
								<div className={styles["search-section__wrapper--container-input-wrapper"]}>
									<i className='icon-search' />
									<label htmlFor='searchText' className={styles["search-section__wrapper--container-label"]}>{t('main.search.inputPlaceholder')}</label>
									<input
										type='search'
										id='searchText'
										placeholder={t('main.search.inputPlaceholder')}
										value={formState.searchText}
										className={styles["search-section__wrapper--container-input"]}
										{...register('searchText')}
									/>
								</div>
								<Button
									variant='secondary'
									className={styles["search-section__wrapper--container-submit"]}
									disabled={watchVal === ""}
									type='submit'
								>
									{t('main.search.searchbtn')}
								</Button>
							</div>
							<div className={styles["search-section__wrapper--container-inner-right"]}>
								{isLoggedIn &&
									<Button
										variant='primary'
										className={styles["search-section__wrapper--button"]}
										onClick={() => { goToTranscriptions('user'); }}
									>{t('main.search.seeMine')}
									</Button>}

								<Button
									variant='primary'
									className={styles["search-section__wrapper--button"]}
									onClick={() => {
										handleClearSearch();
										goToTranscriptions('all');
									}}
								>{t('main.search.seeAll')}
								</Button>
							</div>
						</form>
					</div>
				</Container>
			</div>
		</>
	);
};

SearchTranscriptions.propTypes = {
	searchString: PropTypes.string,
	setSearchString: PropTypes.func,
	isHomePage: PropTypes.bool,
};

export default SearchTranscriptions;
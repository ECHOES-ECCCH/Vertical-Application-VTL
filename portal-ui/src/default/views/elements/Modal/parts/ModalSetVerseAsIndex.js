import React, { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { Form, Modal } from "react-bootstrap";
import PropTypes from "prop-types";
import { useForm } from "react-hook-form";
import { Typeahead, Menu, MenuItem } from "react-bootstrap-typeahead";

import { appActions } from "@/store/actions/app.actions";
import { viewerActions } from "@/store/actions/viewer.actions";
import FormTextarea from '@Elements/Form/FormTextarea';

import Button from "@Elements/Button/Button";
import FormInput from "@Elements/Form/FormInput";

import api from "@/services/api";

const ModalSetVerseAsIndex = ({
	handleOnClose,
	ModalSpinner,
	transcriptionId,
	verseId,
	wordId,
	indexType,
	indexId,
}) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	const [ indexName, setIndexName ] = useState("");

	const [ singleIndexSelections, setSingleIndexSelections ] = useState([]);

	const [ possibleIndexes, setPossibleIndexes ] = useState([]);

	const [ indexesLoading, setIndexesLoading ] = useState(false);

	const [ isEditMode, setIsEditMode ] = useState(indexId ? true : false);
	const dictToUse =
		indexType === "person"
			?t('modals.setAsPerson',{ returnObjects: true })
			: indexType === "place"
				?t('modals.setAsPlace',{ returnObjects: true })
				:t('modals.setAsTerm',{ returnObjects: true });

	useEffect(() => {
		if (isEditMode) {
			setIndexesLoading(true);
			api.get(`/indexes/${indexId}`).then(({ data }) => {
				setIndexesLoading(false);
				setSingleIndexSelections([data.name]);
				if(data.type === "place"){
					setValue("placeUrl", data.accessibleOn);
				}
				if(data.type === "term"){
					setValue("termDescription", data.description);
				}
			});
		}
	}, [ indexId ]);

	useEffect(() => {
		if(singleIndexSelections[0]){
			setIndexName(singleIndexSelections[0].name);
			if(singleIndexSelections[0].type === "place"){
				setValue("placeUrl", singleIndexSelections[0].accessibleOn);
			}
			if(singleIndexSelections[0].type === "term"){
				setValue("termDescription", singleIndexSelections[0].description);
			}
		}
	}, [singleIndexSelections]);

	useEffect(() => {
		api.get(`/transcriptions/${transcriptionId}/indexes/`).then(({ data }) => {
			setPossibleIndexes(data.filter(el => el.type == indexType));
		});
	}, [true]);

	const { register, handleSubmit, setError, formState, setValue } = useForm({
		defaultValues: {
			content: "",
			placeUrl: "",
		},
		mode: "onChange",
	});

	const contentInputChange = (text, event) => {
		setIndexName(text);
	};

	const handleIndexesEdit = formData => {
		setIndexesLoading(true);
		const data = {
			name: indexName,
			verseId: verseId,
			indexType: indexType,
			accessibleOn: formData.placeUrl === "" ? null : formData.placeUrl,
			description: formData.termDescription === "" ? null : formData.termDescription,
		};
		api.put(`/indexes/${indexId}`, data)
			.then(response => {
				dispatch(viewerActions.updateEditedIndex(response.data));
				dispatch(appActions.setLoading(false));
				handleOnClose();
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
				setError("content", {type: "wrong", message: dictToUse.failed});
			});
	};

	const handleIndexesAdd = formData => {
		const searchedForName = indexName;

		setIndexesLoading(true); 

		let foundIndexId = null;

		for(let singleIndex in possibleIndexes){
			if (searchedForName == possibleIndexes[singleIndex].name && indexType == possibleIndexes[singleIndex].type) {
				foundIndexId = possibleIndexes[singleIndex].id;
				break;
			}
		}

		if (foundIndexId) {
			api.put(`/indexes/${foundIndexId}` + (wordId ? `/words/${wordId}` : `/verses/${verseId}`))
				.then(response => {
					dispatch(viewerActions.updateAddedIndex(verseId, wordId, response.data));
					dispatch(viewerActions.allowUndoDisallowRedo());
					dispatch(appActions.setLoading(false));

					handleOnClose();
				})
				.catch(() => {
					dispatch(appActions.setLoading(false));
					setError("content", {type: "wrong", message: dictToUse.failed});
				});
		} else {
			const data = {
				name: searchedForName,
				indexType: indexType,
				accessibleOn: formData.placeUrl === "" ? null : formData.placeUrl,
				description: formData.termDescription === "" ? null : formData.termDescription,
			};
			api.post(`/transcriptions/${transcriptionId}/indexes/`, data)
				.then(response => {
					let newIndexId = response.data.id;

					api.put(`/indexes/${newIndexId}` + (wordId ? `/words/${wordId}` : `/verses/${verseId}`))
						.then(response2 => {
							dispatch(viewerActions.updateAddedIndex(verseId, wordId, response2.data));
							dispatch(viewerActions.allowUndoDisallowRedo());
							dispatch(appActions.setLoading(false));
		
							handleOnClose();
						})
						.catch(() => {
							dispatch(appActions.setLoading(false));
							setError("content", {type: "wrong", message: dictToUse.failed});
						});
				})
				.catch(() => {
					dispatch(appActions.setLoading(false));
					setError("content", {type: "wrong", message: dictToUse.failed});
				});
		}
	};

	return (
		<>
			{indexesLoading ? (
				<ModalSpinner />
			) : (
				<Modal.Body>
					<div className='modal__index--addbox'>
						<form onSubmit={ handleSubmit(isEditMode ? handleIndexesEdit : handleIndexesAdd) }>
							<div className='modal__index--content'>
								<Form.Group>
									<Form.Label>{ dictToUse.label }</Form.Label>
									<Typeahead
										id='content'
										labelKey='name'
										onChange={setSingleIndexSelections}
										onInputChange={contentInputChange}
										options={possibleIndexes}
										selected={singleIndexSelections}
										renderMenu={(results, menuProps) => (
											<>
												<Menu className='modal__index__typeahead' {...menuProps}>
													{results.length > 0 && 
													<>
														<p className='modal__index__typeahead--choose-label'>{ t("modals.listOfIndex.chooseFrom") }</p>
														{results.map((result, index) => (
															<MenuItem className='modal__index__typeahead__item' option={result} position={index} key={result.verseIds}>
																<span className='modal__index__typeahead__item--label'>{result.name}</span> {result.verseIds && <span className='modal__index__typeahead__item--count'>({result.verseIds.length})</span>}
															</MenuItem>
														))}
													</>}
												</Menu>
											</>
										)}
									/>
									{indexType === "place" &&
										<FormInput
											name='placeUrl'
											register={ register }
											label={ dictToUse.placeUrlLabel }
										/>}
									{indexType === "term" && 
										<FormTextarea
											name='termDescription'
											register={ register }
											label={ dictToUse.termDescriptionLabel }
										/>}
								</Form.Group>
							</div>
							<div className='modal__index--buttons'>
								<Button
									variant='secondary'
									onClick={ handleOnClose }
									className='modal__button--cancel'
								>
									{dictToUse.cancel}
								</Button>
								<Button
									variant='primary'
									type='submit'
									disabled={ !formState.isDirty && !indexName }
									className='modal__button--submit'
								>
									{dictToUse.button}
								</Button>
							</div>
						</form>
					</div>
				</Modal.Body>
			)}
		</>
	);
};

ModalSetVerseAsIndex.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
	verseId: PropTypes.number,
	wordId: PropTypes.number,
	ModalSpinner: PropTypes.func,
	indexType: PropTypes.string,
	indexId: PropTypes.number,
};

export default ModalSetVerseAsIndex;

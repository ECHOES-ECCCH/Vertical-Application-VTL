import React from "react";
import PropTypes from "prop-types";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useParams } from "react-router";

import { Dropdown } from "react-bootstrap";

import api from "@/services/api";

import { viewerActions } from "@/store/actions/viewer.actions";

import addAbove from '@Assets/icons/add/add_above.svg';
import addBelow from '@Assets/icons/add/add_below.svg';
import addMargin from '@Assets/icons/add/add_margin.svg';

const WordMark = ({ wordId, type, additionalInfo }) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	const viewMode = useSelector(state => state.viewer.data.viewMode);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const readOnly = viewMode === "read-only";

	const { transcriptionId } = useParams();

	const getIcon = () => {
		if (type === 'gap') {
			return <i className='icon-tei-gap' />;
		} else if (type === 'add') {
			if (additionalInfo === 'above') {
				return <img src={addAbove}/>;
			} else if (additionalInfo === 'below') {
				return <img src={addBelow}/>;
			} else if (additionalInfo === 'margin') {
				return <img src={addMargin}/>;
			} else {
				return <i className='icon-tei-add' />;
			}
		} else if (type === 'subst') {
			return <><i className='icon-subst' /><sup className='verse-word__mark__icon--subst-id'>{additionalInfo}</sup></>;
		} else if (type === 'del') {
			return <i className='icon-usunięta-partia-tekstu-_del_' />;
		}
	};

	const deleteRequest = () => {
		if (type === 'gap') {
			api.delete(`/words/${wordId}/gap`).then((res) => {
				dispatch(viewerActions.allowUndoDisallowRedo());
				dispatch(viewerActions.reloadVerses(res.data.verses, transcriptionId));
				dispatch(viewerActions.reloadLayerBlocks(res.data.blocks, selectedLayer));
			}).catch(() => {});
		} else if (type === 'add') {
			api.delete(`/words/${wordId}/tei_add`).then((res) => {
				dispatch(viewerActions.allowUndoDisallowRedo());
				dispatch(viewerActions.reloadVerses(res.data.verses, transcriptionId));
				dispatch(viewerActions.reloadLayerBlocks(res.data.blocks, selectedLayer));
			}).catch(() => {});
		} else if (type === 'subst') {
			api.delete(`/words/${wordId}/tei_subst`).then((res) => {
				dispatch(viewerActions.allowUndoDisallowRedo());
				dispatch(viewerActions.reloadVerses(res.data.verses, transcriptionId));
				dispatch(viewerActions.reloadLayerBlocks(res.data.blocks, selectedLayer));
			}).catch(() => {});
		} else if (type === 'del') {
			api.delete(`/words/${wordId}/tei_del`).then((res) => {
				dispatch(viewerActions.allowUndoDisallowRedo());
				dispatch(viewerActions.reloadVerses(res.data.verses, transcriptionId));
				dispatch(viewerActions.reloadLayerBlocks(res.data.blocks, selectedLayer));
			}).catch(() => {});
		}
	};

	return (
		<div className='verse-word__mark'>
			<Dropdown className='viewer__verses--item-dropdown'>
				<Dropdown.Toggle
					className='verse-word__mark__icon'
					variant='verseinfo'
					data-place='left'
					disabled={ readOnly }
				>
					{ getIcon() }
				</Dropdown.Toggle>

				<Dropdown.Menu 
					alignRight
				>
					<Dropdown.Item onClick={ () => deleteRequest() }>
						{t('viewer.verse.info.delete')}
					</Dropdown.Item>
				</Dropdown.Menu>
			</Dropdown>
		</div> 
	);
};

WordMark.propTypes = {
	wordId: PropTypes.number,
	type: PropTypes.string,
	additionalInfo: PropTypes.string,
};

export default WordMark;
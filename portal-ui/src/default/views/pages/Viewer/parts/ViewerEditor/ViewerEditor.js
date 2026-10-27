import React, { useEffect } from "react";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useParams} from "react-router";

import { viewerActions } from "@/store/actions/viewer.actions";

import { CheckViewModePermission } from "@/permissions/ViewModes";
import { useShowModal } from "@Elements/Modal/ModalHooks";

import ViewerEditorVerses from "@Pages/Viewer/parts/ViewerEditor/parts/ViewerEditorVerses";
import ViewerEditorVersesWithStructure from "@Pages/Viewer/parts/ViewerEditor/parts/ViewerEditorVersesWithStructure";

const ViewerEditor = () => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	const isMobile = useSelector((state) => state.viewer.isMobile);
	const viewMode = useSelector((state) => state.viewer.data.viewMode);
	const mobilePanels = useSelector((state) => state.app.viewer.mobilePanels);
	const student = mobilePanels === 'student';
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const showTextStructure = useSelector(state => (state.viewer.showTextStructure && !state.viewer.annotationLayer));
	const searchResults = useSelector((state) => state.viewer.searchResults);

	const { transcriptionId, pageId } = useParams();

	const modalAcceptAllVerses = useShowModal({
		title:t('modals.acceptAllVerses.title'),
		type: "viewer",
		name: "acceptAllVerses",
		componentPath: "./parts/ModalAcceptAllVerses",
		componentProps: {transcriptionId: parseInt(transcriptionId), pageId: parseInt(pageId), layerId: parseInt(selectedLayer)},
	});

	const modalRejectAllVerses = useShowModal({
		title:t('modals.rejectAllVerses.title'),
		type: "viewer",
		name: "rejectAllVerses",
		componentPath: "./parts/ModalRejectAllVerses",
		componentProps: {transcriptionId: parseInt(transcriptionId), pageId: parseInt(pageId), layerId: parseInt(selectedLayer)},
	});

	function downHandler({key}) {
		if (key === 'Shift') {
			dispatch(viewerActions.setShiftPressed(true));
		}
	}
  
	function upHandler({key}) {
		if (key === 'Shift') {
			dispatch(viewerActions.setShiftPressed(false));
		}
	}
  
	useEffect(() => {
		window.addEventListener('keydown', downHandler);
		window.addEventListener('keyup', upHandler);
		return () => {
			window.removeEventListener('keydown', downHandler);
			window.removeEventListener('keyup', upHandler);
		};
	}, []);

	return (
		<div 
			className={`viewer__verses ${searchResults.searchedPhrase !== '' ? (isMobile ? 'viewer__verses--mobile-with-search' : 'viewer__verses--with-search') : ''}`}
		>
			{ !isMobile && CheckViewModePermission("canCheckAllVerses", viewMode) && 
			<div 
				className='viewer__verses--all-verses-status'
			>
				<button
					className='verse__checkbox verse__checkbox--active'
					onClick={()=>{ modalAcceptAllVerses(); }}
				>
					<div className='verse__checkbox--accept-double'>
						<i className='icon-accept' />
						<i className='icon-accept' />
					</div>
				</button>
				{t('viewer.editor.acceptAllVerses')}
				<button
					className='verse__checkbox verse__checkbox--active'
					onClick={()=>{ modalRejectAllVerses(); }}
				>
					<div className='verse__checkbox--decline'>
						<i className='icon-close' />
					</div>
				</button>
				{t('viewer.editor.rejectAllVerses')}
			</div> }
			{ (!showTextStructure || student) ? (
				<ViewerEditorVerses /> 
			) : ( 
				<ViewerEditorVersesWithStructure/> 
			)}
		</div>
	);
};

export default ViewerEditor;

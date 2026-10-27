/* eslint-disable no-console */
import React, { useState, useEffect, memo, useRef } from "react";
import { useParams } from "react-router";
import PropTypes from "prop-types";
import { useDispatch, useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import FileSaver from "file-saver";

import api from "@/services/api";

import IsVerseOnScreen from "@Pages/Viewer/parts/Verse/IsVerseOnScreen";

import { viewerActions } from "@/store/actions/viewer.actions";
import { appActions } from "@/store/actions/app.actions";

import { useShowModal } from '@Elements/Modal/ModalHooks';

import Button from "@Elements/Button/Button";

import ModalBaseCancelConfirm from '@Elements/Modal/ModalBaseCancelConfirm';

const Music = ({ verse, toggleVerseActive }) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	const { transcriptionId, pageId } = useParams();
	const isMobile = useSelector((state) => state.viewer.isMobile);
	const viewMode = useSelector((state) => state.viewer.data.viewMode);
	const verseActive = useSelector((state) => state.viewer.editor.verseActive);
	const verseGroupActive = useSelector((state) => state.viewer.editor.verseGroupActive);
	const verseEdit = useSelector((state) => state.viewer.editor.verseEdit);
	const modalActive = useSelector((state) => state.modal.show);
	const mobilePanels = useSelector((state) => state.app.viewer.mobilePanels);
	const layersData = useSelector((state) => state.viewer.transcriptionLayersData);

	const verseRef = useRef();
	const isVisible = IsVerseOnScreen(verseRef);

	const musicFileRef = useRef();

	const [deleteMusicNotes, setDeleteMusicNotes] = useState(false);	

	const readOnly = viewMode === 'read-only';

	const onAddMusicFile = () => {
		musicFileRef.current.click();
	};

	const onMusicFileChange = data => {
		const formData = new FormData();

		formData.append('payload', data.target.files[0]);

		dispatch(appActions.setLoading(true));

		const url = `/verses/${verse.id}/payload`;
		api.post(url, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
			.then(() => {
				dispatch(viewerActions.loadData(parseInt(transcriptionId), parseInt(pageId)));
				dispatch(appActions.setLoading(false));
			});
	};

	const onDeleteMusicConfirmed = async () => {
		dispatch(appActions.setLoading(true));

		api.delete(`/verses/${verse.id}/payload`)
			.then(() => {
				dispatch(viewerActions.loadData(parseInt(transcriptionId), parseInt(pageId)));
				dispatch(appActions.setLoading(false));
				setDeleteMusicNotes(false);
			})
			.catch(() => {
				dispatch(appActions.setLoading(false));
				setDeleteMusicNotes(false);
			});
	};

	const downloadMusicNotes = () => {
		api.get(`/verses/${verse.id}/payload`, { responseType: "blob" }).then(response => {
			let fileName = response.headers["content-disposition"].split("filename=")[1].replace(/^"+/, '').replace(/"+$/, '');
			FileSaver.saveAs(response.data, fileName);
		}).catch(() => {});
	};

	const editCondition = !(
		readOnly ||
        isMobile ||
        verse.image ||

        (viewMode === 'student' && !(mobilePanels === 'student'))
	);

	useEffect(() => {
		if (verseActive === verse.id && !isVisible) {
			verseRef.current.scrollIntoView({
				behavior: "smooth",
				block: 'center',
			});
		}
	}, [ verse.id, verseActive ]);

	let layerNumber = layersData.idToNumber.get(verse.layerId ? `${verse.layerId}` : verse.pageLayer.layerId);

	return (
		<>
			<ModalBaseCancelConfirm
				modalVisible={deleteMusicNotes}
				setModalVisible={setDeleteMusicNotes}
				title={t('modals.removeMusicNotesFromVerse.title')}
				bodyHTML={t('modals.removeMusicNotesFromVerse.removeFile')}
				cancelButtonName={t('form.cancel')}
				confirmButtonName={t('modals.removeMusicNotesFromVerse.button')}
				onConfirm={onDeleteMusicConfirmed}
				type='viewer'
			/>
			<div
				className={ `verse__block-music verse__block-music--inactive${verse.id === verseActive || verseGroupActive.activeVersesIds.includes(verse.id) ? ' active' : ''} ${verseEdit ? ' activeEdit' : ''}` }
				ref={ verseRef }
			>
				<Button
					onClick={ () => toggleVerseActive(verse.id) }
					variant='verse'
				>
					<div className='verse__num'>
						{verse.verseNo}
						<sub>[{layerNumber}]</sub>
					</div>
					<div
						className='verse__content'
						dangerouslySetInnerHTML={ { __html: verse.payload ? verse.payload.payloadFileName : "" } }
					/>

				</Button>
				{editCondition && verse.id === verseActive ? (
					!verse.payload ? (
						<form>
							<input
								type='file' 
								name='file' 
								ref={musicFileRef} 
								onChange={onMusicFileChange} 
								style={{display: "none"}} 
							/>
							<Button
								onClick={() => {
									onAddMusicFile();
								}}
								variant='verse-music-add'
							>
								<span>{t("viewer.music.addMusic")}</span>
							</Button>
						</form>
					) : (
						<>
							<Button
								onClick={() => setDeleteMusicNotes(true)}
								variant='verse-music-remove'
							>
								<span>{t("viewer.music.deleteMusic")}</span>
							</Button>
							<Button
								onClick={() => {
									downloadMusicNotes();
								}}
								variant='verse-music-download'
							>
								<span>{t("viewer.music.downloadMusic")}</span>
							</Button>
						</>
					)
				) : (
					verse.payload && verse.id === verseActive && (
						<Button
							onClick={() => {
								downloadMusicNotes();
							}}
							variant='verse-music-download'
						>
							<span>{t("viewer.music.downloadMusic")}</span>
						</Button>
					)
				)}
			</div>
		</>
	);
};

Music.propTypes = {
	verse: PropTypes.object,
	toggleVerseActive: PropTypes.func,
};

export default memo(Music);
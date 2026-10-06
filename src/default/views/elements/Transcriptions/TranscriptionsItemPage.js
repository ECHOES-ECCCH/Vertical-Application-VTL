import React, { useEffect, useState, useRef, useMemo } from "react";
import PropTypes from "prop-types";
import { useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { Link, useHistory } from "react-router-dom";
import { reverse } from "named-urls";

import Button from "@Elements/Button/Button";
import { useShowModal } from "@Elements/Modal/ModalHooks";

import { appActions } from "@/store/actions/app.actions";

import { CheckViewModePermission } from "@/permissions/ViewModes";

import api from "@/services/api";
import url from "@/router/urls";
import styles from "@Elements/Transcriptions/TranscriptionsItem.module.sass";

import imageOnError from "@Assets/images/icon-image.svg";

const TranscriptionsItemPage = ({
	page,
	viewMode = "read-only",
	creationMode,
	isInTranscriptionDetails = false,
	transcriptionId,
	currentPage,
	transcriptionProcessingStatusDisabledButtons,
	matchesPerTranscription,
	layers,
}) => {
	const dispatch = useDispatch();
	const { t } = useTranslation();
	const history = useHistory();

	const availablePageLayers = useMemo(() => page.pageLayers.sort((a, b) => {
		const firstId = parseInt(a.layer.id);
		const secondId = parseInt(b.layer.id);
		if (firstId < secondId) {
			return -1;
		}
		if (firstId > secondId) {
			return 1;
		}
		return 0;
	}), [page.pageLayers]);

	const linkToTranscriptionView = reverse(url.transcription.viewer, {
		transcriptionId: parseInt(transcriptionId),
		pageId: parseInt(page.id),
	});

	const setPageAsNotEmpty = () => {
		api.put(`/pages/${page.id}/empty?empty=false`).then(() => {
			history.go(0);
		});
	};

	const [transcriptionOptions, setTranscriptionOptions] = useState([]);

	const modalDeleteTranscriptionPage = useShowModal({
		title: t('modals.deleteTranscriptionPage.title'),
		name: "deleteTranscriptionPage",
		componentPath: "./parts/ModalDeleteTranscriptionPage",
		componentProps: { transcriptionId: parseInt(transcriptionId) },
	});

	const modalDownloadTranscriptionPage = useShowModal({
		withClose: true,
		title: t('modals.downloadTranscriptionPage.title'),
		name: "downloadTranscriptionPage",
		componentPath: "./parts/ModalDownloadTranscriptionPage",
		componentProps: { transcriptionId: parseInt(transcriptionId) },
	});
	
	const modalMarkTranscriptionPageAsEmpty = useShowModal({
		title: t('modals.markTranscriptionPageAsEmpty.title'),
		name: "markTranscriptionPageAsEmpty",
		componentPath: "./parts/ModalMarkTranscriptionPageAsEmpty",
		componentProps: {
			pageId: parseInt(page.id),
			handleOnComplete: () => history.go(0),
		},
	});

	const modalLoadTextTranscriptionPage = useShowModal({
		withClose: false,
		title: t('modals.loadTextTranscriptionPage.title'),
		name: "loadTextTranscriptionPage",
		componentPath: "./parts/ModalLoadTextTranscriptionPage",
		componentProps: { transcriptionId: parseInt(transcriptionId), layers: layers },
	});

	useEffect(() => {
		let arr = [{ id: "download" }];
		if (CheckViewModePermission("canImportText", viewMode)) {
			arr.push({ id: "loadText" });
	
			arr.push({ id: "markAsEmpty" });
		}
		if (CheckViewModePermission("canRemovePages", viewMode) && creationMode === "web-form") {
			arr.push({ id: "deletePage" });
		}
		setTranscriptionOptions(arr);
	}, [page.percentOfTranscribed, viewMode, creationMode]);

	const onOption = (id, chId) => {

		switch (id) {
			case "loadText":
				dispatch(appActions.setModalLoadTextTranscriptionPage(page.id));
				modalLoadTextTranscriptionPage();
				break;
			case "markAsEmpty":
				if(page.empty) {
					setPageAsNotEmpty();
				} else {
					modalMarkTranscriptionPageAsEmpty();
				}
				break;
			case "download":
				dispatch(appActions.setModalDownloadTranscriptionPage(page.id));
				modalDownloadTranscriptionPage();
				break;
			case "deletePage":
				dispatch(appActions.setModalDeleteTranscriptionPage(page.id));
				modalDeleteTranscriptionPage();
				break;
			default:
				return false;
		}
		document.getElementById(chId).checked = false;
	};

	const imageRef = useRef(null);

	const handleImageOnError = () => {
		imageRef.current.className = styles["transcription__page--image-error"];
		imageRef.current.style.backgroundImage = `url(${imageOnError})`;
	};

	const handleMenuClick = (e) => {
		const all = document.getElementsByName('pageItemMenu');
		for (var i = 0; i < all.length; i++) {
			if (all[i] != e.target)
				all[i].checked = false;
		}
	};

	return (
		<div className={styles["transcription__page--box"]}>
			{isInTranscriptionDetails && transcriptionOptions.length > 0 && (
				<div className={styles["navigation"]}>
					<input
						type='checkbox'
						name='pageItemMenu'
						className={styles["navigation__checkbox"]}
						id={"navi-toggle" + page.id}
						onClick={handleMenuClick}
					/>
					<label className={styles["navigation__button"]} htmlFor={"navi-toggle" + page.id} >
						<span className={styles["navigation__icon"]} >&nbsp;</span>
					</label>
					<nav className={styles["navigation__nav"]}>
						<ul className={styles["navigation__list"]}>
							{transcriptionOptions.map(transcriptionOption => (
								<li
									key={transcriptionOption.id}
									className={styles["navigation__item"]}
								>
									<a href='#' onClick={() => onOption(transcriptionOption.id, "navi-toggle" + page.id)}>
										{t('transcriptions.dropdown.' + transcriptionOption.id)}
										{transcriptionOption.id === 'markAsEmpty' && page.empty && <i className='icon-accept'/>}
									</a>
								</li>
							))}
						</ul>
					</nav>
				</div>
			)}
			<Link
				to={{
					pathname: linkToTranscriptionView,
					state: {
						paginationPage: currentPage,
					},
				}}
			>

				<div
					className={styles["transcription__page--image"]}
					style={{ backgroundImage: `url(${page.thumbnailUrl})` }}
					ref={imageRef}
				>
					{page.percentOfTranscribed === 100 && (
						<div className={styles["transcription__page--ribbon"]}>{t('transcriptions.item.fullOCR')}</div>
					)}

					{page.thumbnailUrl &&
					<img src={page.thumbnailUrl} alt={`transcription-${page.id}`} title={`transcription-${page.id}`} role='presentation'
						onError={handleImageOnError}
					/>}
					<span className={styles["transcription__page--span"]}>{page.id}</span>
				</div>
			</Link>
			<div className={styles["transcription__page--block-bottom"]}>
				<Link
					to={{
						pathname: linkToTranscriptionView,
						state: {
							paginationPage: currentPage,
						},
					}}
					className={styles["transcription__page--number"]}
				>
					{t('transcriptions.item.pageNo')} {page.pageNo}
				</Link>

				{availablePageLayers.map((pageItem, index) => (
					<div key={pageItem.layer.id} className={styles["transcription__page--progress"]}>
						<span className={styles["transcription__page--progress-text"]}>
							<span className={styles["transcription__page--progress-text-layer"]}>
								{t('viewer.layers.container.layer')} {index + 1}
							</span>
							<span className={styles["transcription__page--progress-text-long"]}>
								{t('transcriptions.item.name')}
								{pageItem.layer.name}
							</span>
						</span>
						<span className={styles["transcription__page--progress-bar"]}>
							<div className={styles["transcription__page--progress-bar--wrapper"]}>
								<span
									className={
										page.percentOfTranscribed === 100
											? styles["transcription__page--progress-bar--active"]
											: styles["transcription__page--progress-bar--meter"]
									}
									style={{ width: `${pageItem.percentOfTranscribed}%` }}
								/>
							</div>
							<span className={styles["transcription__page--progress-value"]}>
								{pageItem.percentOfTranscribed}%
							</span>
						</span>
					</div>
				))}

				{matchesPerTranscription &&
					`${t('transcriptions.item.matchesInText')} ${matchesPerTranscription.hitCount}`}
				<div className={styles["transcription__page__magicBox"]}>
					<Link
						to={{
							pathname: linkToTranscriptionView,
							state: {
								paginationPage: currentPage,
							},
						}}
					>
						<Button variant='secondary'>{viewMode == "read-only" ? t('transcriptions.item.show') : t('transcriptions.item.work')}</Button>
					</Link>
				</div>
			</div>
		</div>
	);
};

TranscriptionsItemPage.propTypes = {
	page: PropTypes.object,
	percentOfTranscribed: PropTypes.number,
	viewMode: PropTypes.string,
	creationMode: PropTypes.string,
	isInTranscriptionDetails: PropTypes.bool,
	transcriptionId: PropTypes.number,
	currentPage: PropTypes.number,
	transcriptionProcessingStatusDisabledButtons: PropTypes.bool,
	matchesPerTranscription: PropTypes.object,
	layers: PropTypes.array,
};

export default React.memo(TranscriptionsItemPage);

import React, { useState, useEffect, useCallback } from "react";
import { useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { Modal, Form } from "react-bootstrap";
import PropTypes from "prop-types";
import FileSaver from "file-saver";

import api from "@/services/api";
import { alertActions } from "@/store/actions/alert.actions";
import Alert from "@Elements/Alert/Alert";
import CommonDropdown from "@Elements/Dropdown/CommonDropdown";

import Button from "@Elements/Button/Button";
import styles from "@Elements/Modal/parts/Styles/ModalDownloadResults.module.sass";

const ModalDownloadResults = ({ handleOnClose, transcriptionId, layers }) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	const [retryRequestURL, setRetryRequestURL] = useState(null);
	const [waitingForResponse, setWaitingForResponse] = useState(false);
	const [exportOption, setExportOption] = useState("all");
	const [shouldGenerateCover, setShouldGenerateCover] = useState(false);
	const [selectedExportIndex, setSelectedExportIndex] = useState(0);

	const firstId = arr => (arr && arr.length > 0 ? arr[0].id : -1);

	const [selectedLayerId, setSelectedLayerId] = useState(firstId(layers));
	const [selectedLayerIndex, setSelectedLayerIndex] = useState(0);

	const onSelectedExportHandler = index => {
		const exportOptionName = exportOptions[index];
		setExportOption(exportOptionName);
		setSelectedExportIndex(index);
	};

	const onSelectedLayerHandler = index => {
		setSelectedLayerIndex(index);
		setSelectedLayerId(layers[index].id);
	};

	const requestEbookIsReady = useCallback(() => {
		if (retryRequestURL) {
			api.get(retryRequestURL).then(res => {
				switch (res.data.status) {
					case "READY": {
						setRetryRequestURL(null);
						if (res.data.details) {
							window.open(res.data.details, "_blank");
							handleOnClose();
						}
						break;
					}

					case "FAILED": {
						setRetryRequestURL(null);

						dispatch(
							alertActions.setAlert({
								type: "danger",
								icon: "icon-circle-warning-empty",
								text: "form.messages.downloadFailed",
								close: true,
							}),
						);

						window.scrollTo(0, 0);

						break;
					}

					default:
						if (retryRequestURL) {
							setTimeout(() => requestEbookIsReady(), 3000);
						}
				}
			});
		}
	});

	useEffect(() => {
		if (retryRequestURL) {
			requestEbookIsReady();
		}
	}, [retryRequestURL]);

	useEffect(() => {
		return () => {
			setRetryRequestURL(null);
		};
	}, []);

	const downloadEbook = method => {
		setWaitingForResponse(true);
		const url = `/transcriptions/${transcriptionId}/eBooks/${selectedLayerId}?format=${method.toUpperCase()}&titlepage=${shouldGenerateCover}`;
		api.post(url).then(res => {
			res && setRetryRequestURL(res.headers.location.replace("/api", ""));
			setWaitingForResponse(false);
		}).catch(() => {
			dispatch(
				alertActions.setAlert({
					type: "danger",
					icon: "icon-circle-warning-empty",
					text: "form.messages.downloadFailed",
					close: true,
				}),
			);
			setWaitingForResponse(false);
		});
	};

	const exportOptions = ["all", "specified"];
	const exportLabels = exportOptions.map(layer =>t('modals.downloadResults.layerOptions.'+layer));
	const layerLabels = layers.map(layer => layer.name);
	const dropdownOptions = ["epub", "mobi", "pdf"];
	const options = [
		{
			code: "hocrZip",
			zip: true,
			method: "hocr",
		},
		{
			code: "teiZip",
			zip: true,
			method: "tei",
		},
		{
			code: "textZip",
			zip: true,
			method: "plain-text",
		},
	];

	const downloadDocument = item => {
		setWaitingForResponse(true);
		let url = `/transcriptions/${transcriptionId}/export/content?type=${item.method}`;
		if (exportOption !== "all" && selectedLayerId > 0) url += "&layerId=" + selectedLayerId;
		api.get(url, {
			responseType: "blob",
		}).then(response => {
			let fileName = response.headers["content-disposition"].split("filename=")[1];
			FileSaver.saveAs(response.data, fileName);
			setWaitingForResponse(false);
			handleOnClose();
		});
	};

	return (
		<>
			<Modal.Body>
				<Alert />
				{!!retryRequestURL || waitingForResponse ? (
					<div className='modal-spinner'>
						<div className='modal-spinner__loading' />
					</div>
				) : (
					<>
						{t('modals.downloadResults.text')}
						
						<CommonDropdown
							options={exportLabels}
							selectedOption={selectedExportIndex}
							setSelectedOption={onSelectedExportHandler}
							className='dropdown-new'
						/>
						
						{exportOption === "all" ? (
							<>
								{t('modals.downloadResults.selectFormat')}
								<div className={styles["modal-grid"]}>
									{options.map(item => (
										<Button
											key={item.code}
											className={styles["modal-btn"]}
											onClick={() => downloadDocument(item)}
											variant='download'
										>
											{t('modals.downloadResults.btOptions.'+item.code)}
										</Button>
									))}
								</div>
							</>
						) : (
							<>
								{t('modals.downloadResults.selectLayer')}
								<CommonDropdown
									options={layerLabels}
									selectedOption={selectedLayerIndex}
									setSelectedOption={onSelectedLayerHandler}
									className='dropdown-new'
								/>
								<Form.Check
									type='checkbox'
									label={t('modals.downloadResults.generateCoverPage')}
									checked={shouldGenerateCover}
									onChange={e => setShouldGenerateCover(e.currentTarget.checked)}
								/>
								{t('modals.downloadResults.selectFormat')}
								<div className={styles["modal-grid"]}>
									{dropdownOptions.map(option => (
										<Button
											key={option}
											onClick={() => downloadEbook(option)}
											className={styles["modal-btn"]}
											variant='download'
										>
											{t('modals.downloadResults.options.'+option)}
										</Button>
									))}
								</div>
							</>
						)}
					</>
				)}
			</Modal.Body>
		</>
	);
};

ModalDownloadResults.propTypes = {
	handleOnClose: PropTypes.func,
	transcriptionId: PropTypes.number,
	layers: PropTypes.array,
};

export default ModalDownloadResults;

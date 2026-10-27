import React, { useState, useEffect, useRef } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router';
import PropTypes from "prop-types";

import { useShowModal } from '@Elements/Modal/ModalHooks';
import { CheckViewModePermission } from "@/permissions/ViewModes";

import api from "@/services/api";
import { appActions } from '@/store/actions/app.actions';
import Button from "@Elements/Button/Button";

import styles from "@Pages/Transcription/TranscriptionPermissions/TranscriptionPermissions.module.sass";

const TranscriptionPermissionsGroups = ({ groups, getTranscriptionPermissionsData, viewMode }) => {

	const { t } = useTranslation();

	const dispatch = useDispatch();
	const modalConfirmInfo = useSelector((state) => state.app.modalConfirmInfo);
	const { transcriptionId } = useParams();
	const wrapperRef = useRef(null);

	const [ display, setDisplay ] = useState(false);
	const [ allowedGroups, setAllowedGropus ] = useState([]);
	const [ namesOfGroups, setNamesOfGroups ] = useState([]);
	const [ groupToAdd, setGroupToAdd ] = useState({});
	const [ inputValue, setInputValue ] = useState('');
	const [ groupId, setGroupId ] = useState('');

	useEffect(() => {
		setGroupNameArray();
		window.addEventListener("mousedown", handleClickOutside);
		return () => {
			window.removeEventListener("mousedown", handleClickOutside);
		};
	}, []);

	useEffect(() => {
		if (modalConfirmInfo === "GROUP_DELETE_CONFIRMED") {
			api.delete(`/transcriptions/${transcriptionId}/allowed-groups/${groupId}`)
				.then(() => {
					getTranscriptionPermissionsData();
				});
		}
		dispatch(appActions.setModalConfirmInfo(null));
	}, [ modalConfirmInfo ]);

	const modalDeleteGroup = useShowModal({
		title:t('modals.permissionsDeleteGroup.title'),
		name: 'deleteGroup',
		componentPath: './parts/ModalDeleteGroup',
	});

	const onAddGroupSubmit = () => {
		api.post(`/transcriptions/${transcriptionId}/allowed-groups`, { groupId: groupToAdd.id })
			.then(() => {
				getTranscriptionPermissionsData();
			});
		setInputValue('');
		setNamesOfGroups(namesGroups => [ ...namesGroups, groupToAdd.name ]);
	};

	const onWrite = (path) => {
		setInputValue(path);
		path.length > 0 ? (
			api.get(`/groups/my-groups/${path}`)
				.then(({ data }) => {
					setAllowedGropus(data);
					(data.length > 0) ? setDisplay(true) : setDisplay(false);
				})
		)
			: setDisplay(false);
	};

	const updateInput = (value) => {
		setDisplay(false);
		setGroupToAdd(value);
		setInputValue(value.name);
	};

	const onDelete = (id, name) => {
		setGroupId(id);
		modalDeleteGroup();
		setNamesOfGroups(namesGroups => namesGroups.filter(item => item !== name));
	};

	const setGroupNameArray = () => {
		if (groups) {
			groups.map((item) => (
				setNamesOfGroups(namesGroups => [ ...namesGroups, item.group.name ])
			));
		}
	};

	const handleClickOutside = event => {
		const { current: wrap } = wrapperRef;
		if (wrap && !wrap.contains(event.target)) {
			setDisplay(false);
		}
	};

	return (
		<div className={ styles["permissions__group--container"] }>
			<h3 className={ styles["permissions__additional-text"] }>{t('transcription.permissions.groups')}</h3>
			<div className={ styles["permissions__group"] }>
				{groups && (
					groups.map((item, index) => (
						<div key={ item.group.id }>
							{index + 1}. {item.group.name}
							<span className={ styles["permissions__group-creator"] }> ({t('auth.account.groupCreator')}: {item.group.creator.email})</span>
							{CheckViewModePermission("canRemoveGroups", viewMode) && 
							<Button
								variant='link'
								onClick={ () => {
									onDelete(item.group.id, item.group.name);
								} }
							>
								{t('transcription.permissions.deleteUser')}
							</Button>}
						</div>
					))
				)}
			</div>
			{CheckViewModePermission("canAddGroups", viewMode) && 
			<>
				<h3 className={ styles["permissions__additional-text"] }>{t('transcription.permissions.addGroupTitle')}</h3>
				<p className='text--small'>{t('transcription.permissions.addGroupName')} <span style={{color: "#005ca9"}}>*</span></p>
				<div className={ styles["permissions__add--container"] }>
					<form
						className={ styles["permissions__add--form"] }
					>
						<div
							className={ styles["permissions__add--wrapper"] }
							ref={ wrapperRef }
						>
							<input
								type='text'
								name='groupName'
								placeholder={t('transcription.permissions.groupPlaceholder') }
								className={ `form-control permissions__add--form-input 
									${styles["permissions__add--form-input"]}
								` }
								onChange={ (e) => onWrite(e.target.value) }
								onClick={ (e) => onWrite(e.target.value) }
								value={ inputValue }
								autoComplete='off'
							/>
							{(display && allowedGroups.length !== namesOfGroups.length) && (
								<div className={ styles["permissions__dropdown-autocomplete"] }>
									{allowedGroups
										.map((value) => {
											return (
												<>{!namesOfGroups.includes(value.name) &&
													<div
														onClick={ () => updateInput(value) }
														key={ value.id }
														className={ styles["permissions__dropdown-autocomplete--item"] }
													>
														{value.name} / {value.creator.email}
													</div>}
												</>
											);
										})}
								</div>
							)}
						</div>
						<div style={ {display: "flex", alignItems: "center"} }>
							<div style={ {color: "#606060", fontSize: "0.8rem"} }><span style={{color: "#005ca9"}}>*</span> {t('transcription.permissions.requiredFields') }</div>
							<Button
								variant='submit'
								className={ styles["permissions__add--form-submit"] }
								label={t('group.addButton') }
								disabled={ inputValue !== groupToAdd.name }
								onClick={ () => onAddGroupSubmit() }
							>
								{t('group.addButton')}
							</Button>
						</div>
					</form>
				</div>
			</>}
		</div>
	);
};

TranscriptionPermissionsGroups.propTypes = {
	groups: PropTypes.array,
	getTranscriptionPermissionsData: PropTypes.func,
	viewMode: PropTypes.string,
};

export default TranscriptionPermissionsGroups;

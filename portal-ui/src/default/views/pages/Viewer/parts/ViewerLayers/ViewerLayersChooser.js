import React, { useEffect, useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router';
import { Dropdown } from "react-bootstrap";
import PropTypes from "prop-types";

import { viewerActions } from '@/store/actions/viewer.actions';
import { CheckViewModePermission } from "@/permissions/ViewModes";

import styles from '@Pages/Viewer/parts/ViewerLayers/ViewerLayersChooser.module.sass';

const CustomToggle = React.forwardRef(({ children, onClick, isShown }, ref) => (
	<div
		className={isShown ? styles["layers__dropdown__toggle"] + " " + styles["layers__dropdown__toggle--shown"] : styles["layers__dropdown__toggle"] }
		ref={ref}
		onClick={(e) => {
			e.preventDefault();
			onClick(e);
		}}
	>
		<div className={styles["layers__dropdown__toggle--text"]}>
			{children}
		</div>
		<div className={styles["layers__dropdown__toggle--icon"]}>
			{!isShown && <i className='icon-chevron-down'/>}
			{isShown && <i className='icon-chevron-up'/>}
		</div>
	</div>
));
CustomToggle.displayName = "CustomToggle";
CustomToggle.propTypes = {
	children: PropTypes.string,
	onClick: PropTypes.func,
	isShown: PropTypes.bool,
};

const CustomMenu = React.forwardRef(({ children, style, className }, ref) => {
	return (
		<div
			ref={ref}
			style={style}
			className={className + " " + styles["layers__dropdown__menu"]}
		>
			<ul className={styles["layers__dropdown__menu--list"]}>
				{children}
			</ul>
		</div>
	);
});
CustomMenu.displayName = "CustomMenu";
CustomMenu.propTypes = {
	children: PropTypes.array,
	style: PropTypes.object,
	className: PropTypes.string,
};

const ViewerLayersChooser = () => {
	const { t } = useTranslation();

	const layersData = useSelector((state) => state.viewer.transcriptionLayersData);
	const selectedLayerId = useSelector(state => state.viewer.selectedLayer);
	const showTextStructure = useSelector((state) => state.viewer.showTextStructure && !state.viewer.annotationLayer);
	const dispatch = useDispatch();
	const { transcriptionId, pageId } = useParams();
	const viewMode = useSelector((state) => state.viewer.data.viewMode);
	const readOnly = viewMode && !CheckViewModePermission("hasAdvancedLayerViewing", viewMode);
	const [ isMenuShown, setMenuShown ] = useState(false);

	useEffect(() => {
		if(readOnly) {
			readOnlyLayerClick(selectedLayerId);
		}
	},[readOnly, selectedLayerId]);

	const getLayerById = (layerId) => {
		for(let layer of layersData.layers){
			if(layer.layer.id === layerId){
				return layer.layer;
			}
		}

		return {};
	};

	const changeVisibility = (layerId) => {
		for(let layer of layersData.layers){
			if(layer.layer.id === layerId){
				dispatch(viewerActions.selectTranscriptionLayerVisibility(transcriptionId, layerId, !layer.isShown));
			}
		}
	};

	const readOnlyLayerClick = (layerId) => {
		dispatch(viewerActions.selectTranscriptionLayer(transcriptionId, layerId, pageId));
		dispatch(viewerActions.setSingleVisibleLayer(layerId));
	};

	const changeSelectedTranscriptionLayer = (layerId) => {
		dispatch(viewerActions.selectTranscriptionLayer(transcriptionId, layerId, pageId));
		dispatch(viewerActions.selectTranscriptionLayerVisibility(transcriptionId, layerId, true));
	};
	
	return (
		<>
			<div
				className={styles['layers__container']}
			>
				<div className={styles["layers__container__title"]}>{t('viewer.layers.container.title') + ":" }</div>
				<Dropdown
					onToggle={(isShown) => { setMenuShown(isShown); }}
					show={isMenuShown}
				>
					<Dropdown.Toggle
						as={CustomToggle}
						isShown={isMenuShown}
					>
						{selectedLayerId ? "[" + layersData.idToNumber.get(selectedLayerId) + "] " + getLayerById(selectedLayerId).name : ""}
					</Dropdown.Toggle>

					<Dropdown.Menu
						as={CustomMenu}
					>
						{layersData.layers.map(item => {
							return (
								<li key={item.layer.id}>
									<input
										type='radio' 
										id={`layer-${item.layer.id}`} 
										value={item.layer.id} 
										name='layers'
										checked={parseInt(item.layer.id) === parseInt(selectedLayerId)} 
										onChange={(e) => changeSelectedTranscriptionLayer(e.target.value)}
										className={styles['layers__container__wrapper--input']}
									/>
									<span style={{cursor:"pointer"}} onClick={() => { changeSelectedTranscriptionLayer(item.layer.id); }}>[{layersData.idToNumber.get(item.layer.id)}]&nbsp;{item.layer.name}</span>
									
									{ showTextStructure ? 
										<span className={styles['layers__dropdown__menu--eye-disabled']}>
											{parseInt(item.layer.id) === parseInt(selectedLayerId) ? 
												<i className='icon-eye_show' onClick={()=>changeVisibility(item.layer.id)}/> : 
												<i className='icon-eye_hide' onClick={()=>changeVisibility(item.layer.id)}/>}
										</span>
										:
										<span className={parseInt(item.layer.id) === parseInt(selectedLayerId) ? styles['layers__dropdown__menu--eye-disabled'] : styles['layers__dropdown__menu--eye']}>
											{item.isShown ? 
												<i className='icon-eye_show' onClick={()=>changeVisibility(item.layer.id)}/> : 
												<i className='icon-eye_hide' onClick={()=>changeVisibility(item.layer.id)}/>}
										</span>}
								</li>
							);
						})}
					</Dropdown.Menu>
				</Dropdown>
			</div>
		</>
	);
};

export default ViewerLayersChooser;
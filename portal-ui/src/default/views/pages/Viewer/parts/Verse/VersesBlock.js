import React, { memo } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useTranslation } from 'react-i18next';
import { useParams } from "react-router";
import PropTypes from "prop-types";
import { Dropdown } from "react-bootstrap";

import { viewerActions } from '@/store/actions/viewer.actions';

import { CheckViewModePermission } from "@/permissions/ViewModes";

import Verse from "@Pages/Viewer/parts/Verse/Verse";

import divChapter from '@Assets/icons/div/chapter.svg';
import divPart from '@Assets/icons/div/part.svg';
import divPrologue from '@Assets/icons/div/prologue.svg';
import divSection from '@Assets/icons/div/section.svg';

import addAbove from '@Assets/icons/add/add_above.svg';
import addBelow from '@Assets/icons/add/add_below.svg';
import addMargin from '@Assets/icons/add/add_margin.svg';

const VersesBlock = ({ blockId, firstVerseNo, lastVerseNo, blockType, blocks, hierarchyDepth, consistencyErrors }) => {
	const { t } = useTranslation();

	const dispatch = useDispatch();

	const selectedLayer = useSelector(state => state.viewer.selectedLayer);
	const isMobile = useSelector((state) => state.viewer.isMobile);
	const viewMode = useSelector((state) => state.viewer.data.viewMode);

	const { transcriptionId, pageId } = useParams();

	const getConsistencyWarning = (consistencyErr) => {
		if(!consistencyErr || consistencyErr.length === 0){
			return <></>;
		}

		let warning = "";

		if (consistencyErr.includes("INVALID_PARENT")) {
			warning = t("viewer.tooltip.blocksErrors.invalidParent");
		} else if(consistencyErr.includes("ROOT_NOT_ALLOWED")) {
			warning = t("viewer.tooltip.blocksErrors.rootNotAllowed");
		} else if(consistencyErr.includes("VERSES_NOT_ALLOWED")) {
			warning = t("viewer.tooltip.blocksErrors.versesNotAllowed");
		} else if(consistencyErr.includes("ONE_VERSE_ALLOWED")) {
			warning = t("viewer.tooltip.blocksErrors.onlyOneVerse");
		}

		return (
			<>
				<i 
					style={{ fontSize: "1.1rem" }} 
					className='icon-circle-warning-empty verses-block__title__consistency-error'
					data-tooltip-id='custom-tooltip' data-tooltip-content={ warning }
				/>
			</>
		);
	};

	const getBlockHeader = (blockTypeId) => {
		if (blockTypeId === 'heading-1') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-header-1-end'/><div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.heading-1") }</div></>;
		} else if (blockTypeId === 'heading-2') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-header-2-end'/><div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.heading-2") }</div></>;
		} else if (blockTypeId === 'heading-3') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-header-3-end'/><div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.heading-3") }</div></>;
		} else if (blockTypeId === 'paragraph') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-paragraph'/><div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.paragraph") }</div></>;
		} else if (blockTypeId === 'table') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-table'/><div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.table") }</div></>;
		} else if (blockTypeId === 'table-row') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-table-verse-end'/><div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.table-row") }</div></>;
		} else if (blockTypeId === 'table-cell') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-table-cell-end'/><div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.table-cell") }</div></>;
		} else if (blockTypeId === 'annotation') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-subscript2'/><div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.annotation") }</div></>;
		} else if (blockTypeId === 'list') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-list'/><div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.list") }</div></>;
		} else if (blockTypeId === 'list-item') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-list-item'/><div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.list-item") }</div></>;
		} else if (blockTypeId === 'header') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-verse-header'/><div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.header") }</div></>;
		} else if (blockTypeId === 'footer') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-verse-footer'/><div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.footer") }</div></>;
		} else if (blockTypeId === 'page-no') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-page-number'/><div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.page-no") }</div></>;
		} else if (blockTypeId === 'div-prologue') {
			return <><img src={divPrologue}/>&nbsp; <div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.div-prologue") }</div></>;
		} else if (blockTypeId === 'div-part') {
			return <><img src={divPart}/>&nbsp; <div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.div-part") }</div></>;
		} else if (blockTypeId === 'div-chapter') {
			return <><img src={divChapter}/>&nbsp; <div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.div-chapter") }</div></>;
		} else if (blockTypeId === 'div-section') {
			return <><img src={divSection}/>&nbsp; <div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.div-section") }</div></>;
		} else if (blockTypeId === 'add') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-tei-add'/>&nbsp; <div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.add") }</div></>;
		} else if (blockTypeId === 'add-above') {
			return <><img src={addAbove}/>&nbsp; <div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.add-above") }</div></>;
		} else if (blockTypeId === 'add-below') {
			return <><img src={addBelow}/>&nbsp; <div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.add-below") }</div></>;
		} else if (blockTypeId === 'add-margin') {
			return <><img src={addMargin}/>&nbsp; <div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.add-margin") }</div></>;
		} else if (blockTypeId === 'del') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-usunięta-partia-tekstu-_del_'/>&nbsp; <div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.del") }</div></>;
		} else if (blockTypeId === 'subst') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-subst'/>&nbsp; <div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.subst") }</div></>;
		} else if (blockTypeId === 'image') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-image'/>&nbsp; <div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.image") }</div></>;
		} else if (blockTypeId === 'caption') {
			return <><i style={{ fontSize: "1.1rem" }} className='icon-image-description'/>&nbsp; <div className='verses-block__title__name'>{ t("viewer.tooltip.blocks.caption") }</div></>;
		}

		return <></>;
	};

	return (
		<ul
			className={`viewer__verses--list ${blocks && blocks.length > 5 ? "viewer__verses--list-overflow" : ""}`}
		>
			<li className='viewer__verses--item verses-block'>
				<div 
					style={{paddingLeft: ((hierarchyDepth * 20 + 10) + "px")}}
					className='verses-block__title' 
					onClick={() => { dispatch(viewerActions.setVerseActiveRange(firstVerseNo, lastVerseNo)); }}
				>
					{ getBlockHeader(blockType) }
					<div style={{flexGrow: "1"}}/>
					{ getConsistencyWarning(consistencyErrors) }
				</div>
				{!isMobile && CheckViewModePermission("canEditVerses", viewMode) && 
				<Dropdown 
					className='verses-block__toggle viewer__verses--item-dropdown'
				>
					<Dropdown.Toggle
						variant='verse-block-options'
						data-place='left'
						id='verse-block-options'
					>
						<i className='icon-more' />
					</Dropdown.Toggle>
					<Dropdown.Menu alignRight>
						<Dropdown.Item onClick={ () =>  dispatch(viewerActions.deleteBlock(transcriptionId, pageId, selectedLayer, blockId)) }>
							{t('viewer.block.delete')}
						</Dropdown.Item>
					</Dropdown.Menu>
				</Dropdown>}
			</li>
			{blocks && blocks.map(block => {
				if(block.blockId !== undefined) {
					return (
						<VersesBlock 
							firstVerseNo={block.firstVerseNo}
							lastVerseNo={block.lastVerseNo}
							key={block.blockId} 
							blockId={block.blockId} 
							blockType={block.type} 
							hierarchyDepth={hierarchyDepth + 1} 
							blocks={block.children}
							consistencyErrors={block.consistencyErrors}
						/>
					);
				}else{
					return (
						<Verse key={block.id} hierarchyDepth={hierarchyDepth + 1} verse={block} />
					);
				}
			})}
		</ul>
	);
};

VersesBlock.propTypes = {
	blockId: PropTypes.number,
	firstVerseNo: PropTypes.number, 
	lastVerseNo: PropTypes.number,
	hierarchyDepth: PropTypes.number,
	blockType: PropTypes.string,
	blocks: PropTypes.array,
	consistencyErrors: PropTypes.array,
};

export default memo(VersesBlock);

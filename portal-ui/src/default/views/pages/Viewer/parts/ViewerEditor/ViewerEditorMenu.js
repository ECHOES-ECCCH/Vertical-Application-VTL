import React, { memo, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useTranslation } from 'react-i18next';
import PropTypes from "prop-types";

import divChapter from '@Assets/icons/div/chapter.svg';
import divPart from '@Assets/icons/div/part.svg';
import divPrologue from '@Assets/icons/div/prologue.svg';
import divSection from '@Assets/icons/div/section.svg';

import addAbove from '@Assets/icons/add/add_above.svg';
import addBelow from '@Assets/icons/add/add_below.svg';
import addMargin from '@Assets/icons/add/add_margin.svg';

import { viewerActions } from "@/store/actions/viewer.actions";

import DropdownBox from "@Pages/Viewer/parts/ViewerEditor/parts/DropdownBox";
import FormattingDropdownButton from "@Pages/Viewer/parts/ViewerEditor/parts/FormattingDropdownButton";
import FormattingButton from "@Pages/Viewer/parts/ViewerEditor/parts/FormattingButton";

import Button from "@Elements/Button/Button";

const INLINE_STYLES = [
	{ label: "B", style: "BOLD", icon: "bold", tooltip: "viewer.tooltip.menu.style.BOLD" },
	{ label: "I", style: "ITALIC", icon: "italic", tooltip: "viewer.tooltip.menu.style.ITALIC" },
	{ label: "S", style: "STRIKETHROUGH", icon: "strike", tooltip: "viewer.tooltip.menu.style.STRIKETHROUGH" },
	{ label: "U", style: "UNDERLINE", icon: "underline", tooltip: "viewer.tooltip.menu.style.UNDERLINE" },
];

const StyleButton = ({ style, active, icon, tooltip = "" }) => {
	const dispatch = useDispatch();
	const { t } = useTranslation();

	const verseEdit = useSelector(state => state.viewer.editor.verseEdit);
	const wordEdit = useSelector(state => state.viewer.editor.wordEdit);

	const setStyle = (event, value) => {
		event.preventDefault();
		dispatch(viewerActions.setStyleActive(value));
	};

	return (
		<div
			data-tooltip-id='custom-tooltip' data-tooltip-content={t('viewer.tooltip.menu.style.'+style) }
			data-place='right'
		>
			<Button
				variant='viewer-button'
				className={ `viewer__button--menu ${active ? "viewer__button--active" : ""}` }
				onMouseDown={ event => setStyle(event, style) }
				disabled={ !verseEdit && !wordEdit.wordId }
				data-tooltip-id='custom-tooltip' data-tooltip-content={ tooltip }
			>
				<span className='d-none'>{icon}</span>
				<i className={ `icon-${icon}` } />
			</Button>
		</div>
	);
};

StyleButton.propTypes = {
	style: PropTypes.string,
	active: PropTypes.bool,
	icon: PropTypes.string,
	tooltip: PropTypes.string,
};

const ViewerEditorCategory = ({ icon, tooltip, disabled, children }) => {
	const [expand, setExpand] = useState(false);

	const onButtonClick = () => {
		setExpand(!expand);
	};
		
	return (
		<>
			<div
				data-tooltip-id='custom-tooltip' data-tooltip-content={ tooltip }
				data-place='right'
			>
				<Button
					variant='viewer-button-category'
					onClick={ () => onButtonClick() }
					disabled={ disabled }
				>
					{ expand && !disabled ? <i className='expand-category icon-arrow-down-full' /> 
						: <i className='expand-category icon-arrow-right-full' /> }
					<span className='d-none'>{icon}</span>
					<i className={ `icon-${icon}` } />
				</Button>
			</div>
			{expand && !disabled && (
				children
			)}
		</>
	);
};

ViewerEditorCategory.propTypes = {
	icon: PropTypes.string,
	tooltip: PropTypes.string,
	disabled: PropTypes.bool,
	children: PropTypes.array,
};

const ViewerEditorMenu = ({ anyVerseActive, anyWordActive, canBlockBeCreated }) => {
	const currentStyle = useSelector(state => state.viewer.editor.styleCurrent);
	const { t } = useTranslation();

	const showTextStructure = useSelector(state => state.viewer.showTextStructure && !state.viewer.annotationLayer);
	const annotationLayer = useSelector(state => state.viewer.annotationLayer);
	const verseEdit = useSelector(state => state.viewer.editor.verseEdit);
	const wordEdit = useSelector(state => state.viewer.editor.wordEdit);
	const verseActive = useSelector(state => state.viewer.editor.verseActive);
	const verseGroupActive = useSelector((state) => state.viewer.editor.verseGroupActive);
	const wordActive = useSelector(state => state.viewer.editor.wordActive);
	const wordGroupActive = useSelector((state) => state.viewer.editor.wordGroupActive);
	const verses = useSelector(state => state.viewer.data.verses);

	const viewMode = useSelector((state) => state.viewer.data.viewMode);
	const student = viewMode === 'student';
	
	const indexesDisabled = showTextStructure || (!verseActive && !wordActive?.wordId) || !!verseEdit || !!wordEdit.wordId || student;

	return (
		<ul className='viewer__menu--list'>
			<ViewerEditorCategory 
				disabled={ !verseEdit && !wordEdit.wordId }
				icon='text-formatting' 
				tooltip={t('viewer.tooltip.menu.categoryTextFormatting')}
			>
				{INLINE_STYLES.map(type => (
					<li key={ type.style }>
						<StyleButton
							key={ type.label }
							active={ currentStyle.size ? currentStyle.has(type.style) : false }
							label={ type.label }
							style={ type.style }
							icon={ type.icon }
							tooltip='' 
						/>
					</li>
				))}
			</ViewerEditorCategory>
			<ViewerEditorCategory
				disabled={
					!anyVerseActive ||
						!!verseEdit ||
						!canBlockBeCreated ||
						!showTextStructure ||
						student
				}
				icon='text-structure' 
				tooltip={t('viewer.tooltip.menu.categoryTextStructure')}
			>
				<li>
					<DropdownBox
						disabled={
							!anyVerseActive ||
								!!verseEdit ||
								!canBlockBeCreated ||
								!showTextStructure ||
								student
						}
						tooltip={t('viewer.tooltip.menu.titleH') }
						icon='header'
					>
						<FormattingDropdownButton
							icon='header-1-end'
							func='heading-1'
							label={t('viewer.tooltip.menu.titleH1') }
						/>
						<FormattingDropdownButton
							icon='header-2-end'
							func='heading-2'
							label={t('viewer.tooltip.menu.titleH2') }
						/>
						<FormattingDropdownButton
							icon='header-3-end'
							func='heading-3'
							label={t('viewer.tooltip.menu.titleH3') }
						/>
					</DropdownBox>
				</li>

				<li>
					<DropdownBox
						disabled={
							!anyVerseActive ||
								!!verseEdit ||
								!canBlockBeCreated ||
								!showTextStructure ||
								student
						}
						tooltip={t('viewer.tooltip.menu.list') }
						icon='list'
					>
						<FormattingDropdownButton
							icon='list-item-end'
							func='list-item'
							label={t('viewer.tooltip.menu.elementListItem') }
						/>
						<FormattingDropdownButton
							icon='list'
							func='list'
							label={t('viewer.tooltip.menu.elementList') }
						/>
					</DropdownBox>
				</li>

				<li>
					<DropdownBox
						disabled={
							!anyVerseActive ||
								!!verseEdit ||
								!canBlockBeCreated ||
								!showTextStructure ||
								student
						}
						tooltip={t('viewer.tooltip.menu.table') }
						icon='table'
					>
						<FormattingDropdownButton
							icon='table-cell-end'
							func='table-cell'
							label={t('viewer.tooltip.menu.elementTableCell') }
						/>
						<FormattingDropdownButton
							icon='table-verse-end'
							func='table-row'
							label={t('viewer.tooltip.menu.elementTableRow') }
						/>
						<FormattingDropdownButton
							icon='table'
							func='table'
							label={t('viewer.tooltip.menu.elementTable') }
						/>
					</DropdownBox>
				</li>

				<li>
					<FormattingButton
						disabled={
							!anyVerseActive ||
								!!verseEdit ||
								!canBlockBeCreated ||
								!showTextStructure ||
								student
						}
						icon='subscript2'
						func='annotation'
						tooltip={t('viewer.tooltip.menu.subscript') }
						data-tooltip-id='custom-tooltip' data-tooltip-content={t('viewer.tooltip.menu.subscript') }
					/>
				</li>

				<li>
					<DropdownBox
						disabled={
							!anyVerseActive ||
								!!verseEdit ||
								!canBlockBeCreated ||
								!showTextStructure ||
								student
						}
						tooltip={t('viewer.tooltip.menu.pageDescription') }
						icon='verses-desc'
					>
						<FormattingDropdownButton
							icon='verse-header'
							func='header'
							label={t('viewer.tooltip.menu.verseHeader') }
						/>
						<FormattingDropdownButton
							icon='page-number'
							func='page-no'
							label={t('viewer.tooltip.menu.pageNumber') }
						/>
						<FormattingDropdownButton
							icon='verse-footer'
							func='footer'
							label={t('viewer.tooltip.menu.verseFooter') }
						/>
					</DropdownBox>
				</li>
				<li>
					<DropdownBox
						disabled={
							!anyVerseActive ||
								!!verseEdit ||
								!canBlockBeCreated ||
								!showTextStructure ||
								student
						}
						tooltip={t('viewer.tooltip.menu.image') }
						icon='image'
					>
						<FormattingDropdownButton
							icon='image'
							func='image'
							label={t('viewer.tooltip.menu.image') }
						/>
						<FormattingDropdownButton
							icon='image-description'
							func='caption'
							label={t('viewer.tooltip.menu.imageCaption') }
						/>
					</DropdownBox>
				</li>
			</ViewerEditorCategory>
			<ViewerEditorCategory
				disabled={
					(!anyVerseActive ||
						!!verseEdit ||
						!canBlockBeCreated ||
						!showTextStructure
					) && (
						!anyWordActive ||
						!!wordEdit.wordId
					) || student
				}
				icon='tei' 
				tooltip={t('viewer.tooltip.menu.categoryTeiStructure')}
			>
				<li>
					<DropdownBox
						disabled={
							!anyVerseActive ||
								!!verseEdit ||
								!canBlockBeCreated ||
								!showTextStructure ||
								student
						}
						tooltip={t('viewer.tooltip.menu.div') }
						icon='div'
					>
						<FormattingDropdownButton
							iconImg={divPrologue}
							func='div-prologue'
							width='70px'
							label={t('viewer.tooltip.menu.div-prologue') }
						/>
						<FormattingDropdownButton
							iconImg={divPart}
							func='div-part'
							width='70px'
							label={t('viewer.tooltip.menu.div-part') }
						/>
						<FormattingDropdownButton
							iconImg={divChapter}
							func='div-chapter'
							width='70px'
							label={t('viewer.tooltip.menu.div-chapter') }
						/>
						<FormattingDropdownButton
							iconImg={divSection}
							func='div-section'
							width='70px'
							label={t('viewer.tooltip.menu.div-section') }
						/>
					</DropdownBox>
				</li>
				<li>
					<FormattingButton
						disabled={
							!anyVerseActive ||
								!!verseEdit ||
								!canBlockBeCreated ||
								!showTextStructure ||
								student
						}
						icon='paragraph'
						func='paragraph'
						tooltip={t('viewer.tooltip.menu.paragraph')}
						data-tooltip-id='custom-tooltip' data-tooltip-content={t('viewer.tooltip.menu.paragraph')}	
					/>
				</li>
				<li>
					<FormattingButton
						disabled={
							(!anyVerseActive ||
								!!verseEdit ||
								!canBlockBeCreated ||
								!showTextStructure
							) && (
								!anyWordActive ||
								!!wordEdit.wordId
							) || student
						}
						icon='usunięta-partia-tekstu-_del_'
						func={!anyWordActive ? 'del' : 'word-del'}
						tooltip={t('viewer.tooltip.menu.del')}
						data-tooltip-id='custom-tooltip' data-tooltip-content={t('viewer.tooltip.menu.del')}	
					/>
				</li>
				<li>
					<DropdownBox
						disabled={
							(!anyVerseActive ||
								!!verseEdit ||
								!canBlockBeCreated ||
								!showTextStructure
							) && (
								!anyWordActive ||
								!!wordEdit.wordId
							) || student
						}
						tooltip={t('viewer.tooltip.menu.add') }
						icon='tei-add'
					>
						<FormattingDropdownButton
							icon='tei-add'
							func={!anyWordActive ? 'add' : 'word-add'}
							label={t('viewer.tooltip.menu.add-def') }
						/>
						<FormattingDropdownButton
							iconImg={addAbove}
							func={!anyWordActive ? 'add-above' : 'word-add-above'}
							width='70px'
							label={t('viewer.tooltip.menu.add-above') }
						/>
						<FormattingDropdownButton
							iconImg={addBelow}
							func={!anyWordActive ? 'add-below' : 'word-add-below'}
							width='70px'
							label={t('viewer.tooltip.menu.add-below') }
						/>
						<FormattingDropdownButton
							iconImg={addMargin}
							func={!anyWordActive ? 'add-margin' : 'word-add-margin'}
							width='70px'
							label={t('viewer.tooltip.menu.add-margin') }
						/>
					</DropdownBox>
				</li>
				<li>
					<FormattingButton
						disabled={
							(!anyVerseActive ||
								!!verseEdit ||
								!canBlockBeCreated ||
								!showTextStructure
							) && (
								!anyWordActive ||
								!!wordEdit.wordId
							) || student
						}
						icon='subst'
						func={!anyWordActive ? 'subst' : 'word-subst'}
						tooltip={t('viewer.tooltip.menu.subst')}
						data-tooltip-id='custom-tooltip' data-tooltip-content={t('viewer.tooltip.menu.subst')}	
					/>
				</li>
				<li>
					<FormattingButton
						disabled={
							!anyWordActive ||
							!!wordEdit.wordId ||
							student
						}
						icon='tei-gap'
						func='gap'
						tooltip={t('viewer.tooltip.menu.gap')}
						data-tooltip-id='custom-tooltip' data-tooltip-content={t('viewer.tooltip.menu.gap')}	
					/>
				</li>
			</ViewerEditorCategory>
			<ViewerEditorCategory
				disabled={indexesDisabled}
				icon='instapaper' 
				tooltip={showTextStructure ? t('viewer.tooltip.menu.categoryIndexesTextStructureError') : t('viewer.tooltip.menu.categoryIndexes')}
			>
				<li>
					<FormattingButton
						icon='index-place'
						func='verse-set-as-index'
						indexType='place'
						tooltip={t('viewer.tooltip.menu.setAsPlace')}
						data-tooltip-id='custom-tooltip' data-tooltip-content={t('viewer.tooltip.menu.setAsPlace')}
						disabled={indexesDisabled}
					/>
				</li>
				<li>
					<FormattingButton
						icon='index-term'
						func='verse-set-as-index'
						indexType='term'
						tooltip={t('viewer.tooltip.menu.setAsTerm')}
						data-tooltip-id='custom-tooltip' data-tooltip-content={t('viewer.tooltip.menu.setAsTerm')}
						disabled={indexesDisabled}
					/>
				</li>
				<li>
					<FormattingButton
						icon='index-person'
						func='verse-set-as-index'
						indexType='person'
						tooltip={t('viewer.tooltip.menu.setAsPerson')}
						data-tooltip-id='custom-tooltip' data-tooltip-content={t('viewer.tooltip.menu.setAsPerson')}	
						disabled={indexesDisabled}
					/>
				</li>
			</ViewerEditorCategory>
			<ViewerEditorCategory 
				icon='verse-management' 
				tooltip={t('viewer.tooltip.menu.categoryVerseEdition')}
				disabled={student}
			>
				<li>
					<FormattingButton
						disabled={
							!anyVerseActive ||
								!!verseEdit ||
								annotationLayer ||
								student
						}
						icon='verse-auto-transcription'
						func='recognize-content'
						tooltip={t('viewer.tooltip.menu.verseAutoTranscription') }
					/>
				</li>

				<li>
					<FormattingButton
						visible
						disabled={!verseActive || !!verseEdit || student}
						icon='verse-comment'
						func='verse-comment'
						tooltip={t('viewer.tooltip.menu.addComment') }
					/>
				</li>

				<li>
					<DropdownBox
						tooltip={t('viewer.tooltip.menu.deleting') }
						disabled={ !verses.length || student }
						icon='verse-delete'
					>
						<FormattingDropdownButton
							icon='verse-delete'
							func='verse-delete'
							disabled={ (!verseActive && verseGroupActive.firstVerseNo === null) || !!verseEdit || student }
							label={t('viewer.tooltip.menu.verseDelete') }
						/>
						<FormattingDropdownButton
							icon='verse-delete'
							func='word-delete'
							disabled={ (!wordActive.wordId && wordGroupActive.firstWordNo === null) || wordEdit.wordId || student }
							label={t('viewer.tooltip.menu.wordDelete') }
						/>
						<FormattingDropdownButton
							icon='verse-delete'
							func='verses-delete'
							disabled={ student }
							label={t('viewer.tooltip.menu.versesDelete') }
						/>
					</DropdownBox>
				</li>

				<li>
					<FormattingButton
						disabled={
							!anyVerseActive ||
								!!verseEdit ||
								student
						}
						icon='verse-clear'
						func='clear-verses'
						tooltip={t('viewer.tooltip.menu.verseClear') }
					/>
				</li>

				<li>
					<FormattingButton
						disabled={!anyVerseActive || !!verseEdit || student}
						icon='verse-up'
						func='move-up'
						tooltip={t('viewer.tooltip.menu.verseUp') }
					/>
				</li>

				<li>
					<FormattingButton
						disabled={!anyVerseActive || !!verseEdit || student}
						icon='verse-down'
						func='move-down'
						tooltip={t('viewer.tooltip.menu.verseDown') }
					/>
				</li>

				<li>
					<FormattingButton
						disabled={!anyVerseActive || !!verseEdit || student}
						icon='move-verse-10-up'
						func='move-up-10'
						tooltip={t('viewer.tooltip.menu.verseUp10') }
					/>
				</li>

				<li>
					<FormattingButton
						disabled={!anyVerseActive || !!verseEdit || student}
						icon='move-verse-10-down'
						func='move-down-10'
						tooltip={t('viewer.tooltip.menu.verseDown10') }
					/>
				</li>
			</ViewerEditorCategory>
		</ul>
	);
};

ViewerEditorMenu.propTypes = {
	anyVerseActive: PropTypes.bool,
	anyWordActive: PropTypes.bool,
	canBlockBeCreated: PropTypes.bool,
};

export default memo(ViewerEditorMenu);

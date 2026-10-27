import React, { memo } from "react";
import PropTypes from "prop-types";
import { useParams } from "react-router";
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Dropdown } from "react-bootstrap";

import { viewerActions } from '@/store/actions/viewer.actions';
import { CheckViewModePermission } from "@/permissions/ViewModes";

const VerseStatus = ({ verse }) => {
	const dispatch = useDispatch();
	const { t } = useTranslation();

	const isMobile = useSelector((state) => state.viewer.isMobile);
	const viewMode = useSelector((state) => state.viewer.data.viewMode);
	const selectedLayer = useSelector(state => state.viewer.selectedLayer);

	const { transcriptionId, pageId } = useParams();

	const changeStatus = (newStatus) => {
		dispatch(viewerActions.submitVersesStatus([verse.id], newStatus, selectedLayer));
	};

	return (
		<Dropdown className='verse__comments'>
			<Dropdown.Toggle
				variant='verse-status-options'
				data-place='left'
				className='verse__status'
			>
				<div className={`verse__checkbox${CheckViewModePermission("canCheckVerses", viewMode) && !isMobile ? ' verse__checkbox--active' : ''}` }>
					{verse.status === 'transcribed' &&
						<div className='verse__checkbox--accept'>
							<i className='icon-accept' />
						</div>}

					{verse.status === 'verified' &&
						<div className='verse__checkbox--accept-double'>
							<i className='icon-accept' />
							<i className='icon-accept' />
						</div>}

					{verse.status === 'incorrect' &&
						<div className='verse__checkbox--decline'>
							<i className='icon-close' />
						</div>}
				</div>
			</Dropdown.Toggle>
			<Dropdown.Menu className='verse__status__dropdown-menu'>
				<Dropdown.Item 
					onClick={() => { changeStatus("none"); }}
					className='verse__status__dropdown-menu__item'
				>
					<div className='verse__status'>
						<div className='verse__checkbox'/>
					</div>
					{t('viewer.status.inEdit')}
				</Dropdown.Item>
				<Dropdown.Item 
					onClick={() => { changeStatus("transcribed"); }}
					className='verse__status__dropdown-menu__item'
				>
					<div className='verse__status'>
						<div className='verse__checkbox'>
							<div className='verse__checkbox--accept'>
								<i className='icon-accept' />
							</div>
						</div>
					</div>
					{t('viewer.status.verification')}
				</Dropdown.Item>
				{ CheckViewModePermission("canAcceptAndRejectVerses", viewMode) && 
				<Dropdown.Item 
					onClick={() => { changeStatus("verified"); }}
					className='verse__status__dropdown-menu__item'
				>
					<div className='verse__status'>
						<div className='verse__checkbox'>
							<div className='verse__checkbox--accept-double'>
								<i className='icon-accept' />
								<i className='icon-accept' />
							</div>
						</div>
					</div>
					{t('viewer.status.accepted')}
				</Dropdown.Item>}
				{ CheckViewModePermission("canAcceptAndRejectVerses", viewMode) &&
				<Dropdown.Item
					onClick={() => { changeStatus("incorrect"); }}
					className='verse__status__dropdown-menu__item'
				>
					<div className='verse__status'>
						<div className='verse__checkbox'>
							<div className='verse__checkbox--decline'>
								<i className='icon-close' />
							</div>
						</div>
					</div>
					{t('viewer.status.rejected')}
				</Dropdown.Item>}
			</Dropdown.Menu>
		</Dropdown>
	);
};

VerseStatus.propTypes = {
	verse: PropTypes.object,
};

export default memo(VerseStatus);
import React, { useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import PropTypes from 'prop-types';

import Button from '@Elements/Button/Button';

const TranscriptionSearch = ({ searchString, setSearchString }) => {
	const { t } = useTranslation();

	const { register, handleSubmit, setValue, formState } = useForm({ mode: 'onChange' });

	useEffect(() => {
		setValue("searchText", searchString);
	}, [searchString]);

	const clearField = useCallback(() => {
		setSearchString('');
	}, [setSearchString]);

	return (
		<form
			onSubmit={handleSubmit((tx) => setSearchString(tx.searchText))}
			noValidate
		>
			<label className='input-group'>

				<input
					{...register('searchText')}
					type='text'
					placeholder={t('transcriptions.search.placeholder')}
					className='form-control'
				/>
			</label>
			<div className='input-group-append'>
				{(searchString && searchString.length) > 0 && (
					<Button
						onClick={clearField}
						className='btn-third'
					>
						<i className='icon-close' />
					</Button>
				)}
				<Button
					type='submit'
					disabled={!formState.isDirty}
				>
					{t('transcriptions.search.searchBt')}
				</Button>
			</div>

		</form>
	);
};

TranscriptionSearch.propTypes = {
	setSearchString: PropTypes.func,
	searchString: PropTypes.string,
};

export default TranscriptionSearch;
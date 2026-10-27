import React, { useEffect, useState } from "react";
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

import api from "@/services/api";

import Button from '@Elements/Button/Button';

const ModalAvailableImportSources = ({ handleOnClose, ModalSpinner }) => {
	const { t } = useTranslation();

	const [ availableDomains, setAvailableDomains ] = useState([]);
	const [ availableDomainsLoading, setAvailableDomainsLoading ] = useState(true);
	const [ searchDomains, setSearchDomains ] = useState('');
	const [ filteredDomains, setFilteredDomains ] = useState(availableDomains);
	const importsDomains = useSelector((state) => state.app.importsDomains);

	useEffect(() => {
		setAvailableDomainsLoading(true);
		api.get(`/domains?file-type=${importsDomains}`)
			.then(response => {
				setAvailableDomainsLoading(false);
				setAvailableDomains(response.data);
				setFilteredDomains(response.data);
			});
	}, []);

	return (
		<>
			{availableDomainsLoading ? (
				<ModalSpinner />
			) : (
				<>
					<Modal.Body>
						<p dangerouslySetInnerHTML={ { __html:t('modals.importAvailableSources.desc.'+importsDomains) } } />

						<input
							onChange={ e => {
								const filteredDomainsData = availableDomains.filter(domain => {
									return domain.toLowerCase().includes(e.target.value.toLowerCase());
								});

								setSearchDomains(e.target.value);
								setFilteredDomains(filteredDomainsData);
							} }
							type='text'
							value={ searchDomains }
							className='modal__resources--input'
							placeholder={t('modals.importAvailableSources.search') }
						/>
						<ul className='modal__resources--list'>
							{filteredDomains &&
							filteredDomains.map((item, i) =>
								<li
									key={ item + parseInt(i) }
									className='modal__resources--list-item'
								>
									{item}
								</li>,
							)}
						</ul>
					</Modal.Body>
					<Modal.Footer>
						<Button
							variant='primary'
							onClick={ () => handleOnClose() }
						>
							{t('modals.importAvailableSources.close')}
						</Button>
					</Modal.Footer>
				</>
			)}
		</>
	);
};

ModalAvailableImportSources.propTypes = {
	handleOnClose: PropTypes.func,
	ModalSpinner: PropTypes.func,
};

export default ModalAvailableImportSources;
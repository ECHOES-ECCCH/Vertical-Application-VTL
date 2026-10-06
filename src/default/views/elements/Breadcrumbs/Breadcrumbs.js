import React from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import styles from "@Elements/Breadcrumbs/Breadcrumbs.module.sass";
import url from "@/router/urls";

const Breadcrumbs = ({ breadcrumbsList }) => {
	const { t } = useTranslation();

	const getBreadcrumbsLink = (item) => {
		if (item.label === 'home') {
			return (
				<li 
					key={ item.id }
					className={ styles['breadcrumbs__list-item'] }
				>
					<Link
						to={ url.home }
						className={ styles['breadcrumbs__list-item--home'] }
					>
						<span>{t('breadcrumbs.home')}</span>
					</Link>
				</li>
			);
		}

		return (
			<li 
				key={ item.id } 
				className={ `${styles['breadcrumbs__list-item']} ${'icon-slash'}` }
			>
				<span className={ styles['breadcrumbs__list-item--scroll'] }>
					{item.link ? (
						<Link
							to={ item.link }
							className={ styles['breadcrumbs__list-item--link'] }
						>
							{item.label}
						</Link>
					): (
						<span
							className={ styles['breadcrumbs__list-item--label'] }
						>
							{item.label}
						</span>
					)}
				</span>
			</li>
		);
	};

	return (
		<div className={ styles['breadcrumbs'] }>
			<ul className={ styles['breadcrumbs__list'] }>
				{breadcrumbsList.map((breadcrumbsListItem) =>
					breadcrumbsListItem
						? getBreadcrumbsLink(breadcrumbsListItem)
						: false,
				)}
			</ul>
		</div>
	);
};

Breadcrumbs.propTypes = {
	breadcrumbsList: PropTypes.arrayOf(
		PropTypes.shape({
			label: PropTypes.any.isRequired,
			link: PropTypes.string,
		}),
	),
};

export default Breadcrumbs;

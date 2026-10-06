import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import PropTypes from 'prop-types';
import { useDebounce } from 'use-debounce';

import PaginationsItemsPerPage from "@Components/Pagination/PaginationsItemsPerPage";

/* Instrukcja użycia:
Wiemy, że dany blok będzie wymagał modułu paginacji, otaczamy go tym komponentem:
<Pagination>
	<ExampleList />
</Pagination>
w ten sposób przekazany zostaje pierwszy parametr - children
W nadrzędnym komponencie musimy tylko zdefiniować stan dla obecnego numeru strony:
const [currentPage, setCurrentPage] = setState(1);
Api zawssze podaje nam też ilość stron, a zatem to przypisujemy jako trzeci parametr

onGoToPage to referencja do funkcji, która jest odpowiedzialna za zmianę strony
(na przykład użytkownik w module paginacja kliknął na 3 stronę, czyli w komponencie powyżej musimy zrobić przekierowanie na odpowiednią stronę, coś w stylu:
history.push('examplelist/3')
	)
Opcjonalnie metoda ta dostaje nową ilość rekordów na stronę.
*/

const Pagination = ({ children, currentPage, pagesNum, onGoToPage, perPage, title, showPerPage = false, leftTopChildren, className }) => {
	const { t } = useTranslation();

	const [ pagesArr, setPagesArr ] = useState([]);
	const [ inputPageNum, setInputPageNum ] = useState(0);
	const [ debouncedPageNum ] = useDebounce(inputPageNum, 500);
	const paginationTop = useRef();
	const [ count, setCount ] = useState(perPage);

	const setCountAndReload = (newCount) => {
		setCount(newCount);
		onGoToPage(1, newCount);
	};

	useEffect(() => {
		setCount(perPage);
	}, [perPage]);

	useEffect(() => {
		const db = parseInt(debouncedPageNum);
		if (db !== currentPage && db > 0 && db <= pagesNum) {
			gotoPage(db);
		}
	}, [ debouncedPageNum ]);

	const gotoPage = (pageNum) => {
		paginationTop.current.scrollIntoView({
			behavior: 'smooth',
			block: 'start',
		});
		onGoToPage(pageNum);
	};

	const prevButton =  (
		<button
			className={ `${(currentPage > 1) ? "pagination__btn" : "pagination__btn--disabled"}
					${"pagination__btn--prev"}` }
			onClick={ () => gotoPage(currentPage - 1) }
		>{t('pagination.previous')}
		</button>
	);
	const nextButton = (
		<button
			className={ `${(currentPage < pagesNum) ? "pagination__btn" : "pagination__btn--disabled"}
					${"pagination__btn--next"}` }
			onClick={ () => gotoPage(currentPage + 1) }
		>{t('pagination.next')}
		</button>
	);

	const fPage = (pNum) => (
		<li
			className='pagination-bottom__item'
			onClick={ () => {
				gotoPage(pNum);
			} }
		>{pNum}
		</li>
	);

	useEffect(() => {
		setPagesArr([ ...Array(pagesNum).keys() ].filter((item, index) => {
			let surroundingPageNum = 2;
			if ((currentPage === 1) || (pagesNum >= 5 && currentPage === pagesNum)) {
				surroundingPageNum = 4;
			} else if ((currentPage === 2) || (pagesNum === 4 && currentPage === 4) || (pagesNum > 4 && currentPage === pagesNum - 1)) {
				surroundingPageNum = 3;
			}
			if (index >= (currentPage - surroundingPageNum - 1) && index <= (currentPage + surroundingPageNum - 1)) {
				return item + 1;
			}
			return null;
		}));
		if (currentPage) {
			setInputPageNum(currentPage);
		}
	}, [ currentPage, pagesNum /*, count */ ]);

	return (
		<>
			<div className={"pagination-top pagination-top--container " + className}>
				<div className='pagination-top__items'>
					{leftTopChildren}
					<div
						ref={ paginationTop }
						className='pagination-top__anchor'
					/>
					
					{showPerPage &&
					<PaginationsItemsPerPage
						count={count}
						setCount={setCountAndReload}
						title={title}
					/>}
				</div>
				
				<div className='pagination-top--left'>
					{(currentPage > 1) && prevButton}
					{(pagesNum > 1) ? (
						<label>
							<span className='d-none'>{inputPageNum}</span>
							<input
								className='pagination__input'
								type='number'
								min='1'
								value={ inputPageNum }
								onChange={ ev => setInputPageNum(ev.target.value) }
								max={ pagesNum }
							/>
						</label>
					) : currentPage}
					<div>{t('pagination.of')}</div>
					{fPage(pagesNum)}
					{(currentPage < pagesNum) && nextButton}
				</div>
			</div>
			
			{children}
			
			<div className={"pagination-bottom " + className}>
				{prevButton}
				{(pagesNum > 5 && currentPage >= 4) && (fPage(1))}
				{(pagesNum > 5 && currentPage >= 5) && (
					<li className='pagination-bottom__item--disabled'>...</li>
				)}

				<ul className='pagination-bottom__list'>
					{pagesArr.map((page, index) => (
						<li
							className={ (page + 1 === currentPage) ? "pagination-bottom__item--disabled" : "pagination-bottom__item" }
							key={ "page" + index.toString() }
							onClick={ () => gotoPage(page + 1) }
						> {page + 1}
						</li>),
					)}
					<li className='pagination-bottom__item--disabled'> {t('pagination.of')} </li>
					{fPage(pagesNum)}
				</ul>
				{nextButton}
			</div>
				
		</>
	);
};

Pagination.propTypes = {
	children: PropTypes.oneOfType([ PropTypes.string, PropTypes.element ]),
	currentPage: PropTypes.number,
	pagesNum: PropTypes.number,
	onGoToPage: PropTypes.func,
	perPage: PropTypes.number,
	title: PropTypes.string,
	showPerPage: PropTypes.bool,
	leftTopChildren: PropTypes.element,
	className: PropTypes.string,
};

export default Pagination;

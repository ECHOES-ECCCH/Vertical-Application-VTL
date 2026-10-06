import React, {useEffect} from "react";
import { useSelector, useDispatch } from 'react-redux';
import { Dropdown } from 'react-bootstrap';

import { appActions } from '@/store/actions/app.actions';

import styles from '@Templates/header/AppHeader/HeaderTopLang.module.sass';

const HeaderTopLang = () => {
	const appState = useSelector((state) => state.app);
	const langs = appState.langs.sort((a, b) => a.name.localeCompare(b.name));
	let currentLang = langs.filter(item => item.id === appState.currentLang)[0];
	const dispatch = useDispatch();

	useEffect(() => {
	});

	const setLanguage = (lang) => {
		dispatch(appActions.setLang(lang));
	};

	return (
		<Dropdown className={ styles["lang-dropdown"] } alignRight>

			<Dropdown.Toggle
				id='dropdown-custom-1'
				className={ styles["lang-dropdown__button"] }
				variant={ styles["lang-dropdown__button"] }
			>{currentLang.name}
			</Dropdown.Toggle>

			<Dropdown.Menu className={ styles["lang-dropdown__menu"] }>
				{langs.map(lang =>
					<Dropdown.Item
						key={ lang.id }
						onClick={ () => setLanguage(lang) }
						className={ ` 
							${styles["lang-dropdown__item"]}
							${ lang.id !== appState.currentLang ? styles["lang-dropdown__item--not-active"] : ''} 
						` }
					>
						{ lang.id === appState.currentLang ? <i className='icon-accept' /> : null}
						{lang.name}
					</Dropdown.Item>,
				)}
			</Dropdown.Menu>
		</Dropdown>
	);
};

export default HeaderTopLang;

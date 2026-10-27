import React from 'react';
import { useTranslation } from 'react-i18next';

import styles from "@Pages/Home/parts/Financed/Financing.module.sass";

const Financing = () => {
	const { t } = useTranslation();

	return (
		<div className={ styles['financing'] }>
			<div className={` ${styles['financing__container']} container`}>
				<div className={ styles['financing__content']}>
					<p className={ styles['financing__text--content']}>
						{t('financed.text')} <a href='https://www.echoes-eccch.eu/community/'>https://www.echoes-eccch.eu/community/</a>
					</p>
				</div>
				<div className={ styles['financing__footer']}>
					<div className={styles["financing__img_1"]} />
					<div className={styles["financing__img_2"]} />
				</div>
                
			</div>
		</div>
	);
};

export default Financing;
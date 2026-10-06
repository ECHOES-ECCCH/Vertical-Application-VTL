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
						{t('financed.text')} <a href='https://lab.dariah.pl/projekt/o-projekcie/'>https://lab.dariah.pl/projekt/o-projekcie/</a>
					</p>
				</div>
				<div className={ styles['financing__footer']}>
					<div className={styles["financing__img_1"]} />
					<div className={styles["financing__img_2"]} />
					<div className={styles["financing__img_3"]} />
				</div>
                
			</div>
		</div>
	);
};

export default Financing;
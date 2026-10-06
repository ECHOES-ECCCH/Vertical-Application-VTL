-- Script to adapt database from WLT from version around 2021 to 2023

ALTER TABLE ocr_training_lang_stats ALTER COLUMN lang TYPE varchar(63);
ALTER TABLE ocr_training_lang_stats DROP COLUMN trainable;

ALTER TABLE ocr_training_examples ALTER COLUMN lang TYPE varchar(63);
ALTER TABLE ocr_training_examples DROP CONSTRAINT ocr_training_examples_iiif_img_url_key;

ALTER TABLE pending_ocr_corrections ALTER COLUMN lang TYPE varchar(63);
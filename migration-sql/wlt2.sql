-- Script to adapt database from WLT from version around 2021 to 2023
-- 1. Run the new WLT, make sure ddl-auto is set to update so that it creates most missing tables
-- and columns and prints some errors to logs for columns that cannot be automatically created
-- 2. Shut down WLT and run this script that creates the rest of the columns and makes other necessary changes
-- 3. Run WLT again, should start without errors.

CREATE TEXT SEARCH DICTIONARY polish_ispell (
  Template = ispell,
  DictFile = polish,
  AffFile = polish,
  StopWords = polish
);

CREATE TEXT SEARCH CONFIGURATION polish(parser = default);

ALTER TEXT SEARCH CONFIGURATION polish
  ALTER MAPPING FOR asciiword, asciihword, hword_asciipart, word, hword, hword_part
  WITH polish_ispell;

alter table transcriptions add column percent_of_transcribed int4;
update transcriptions set percent_of_transcribed = 0;
alter table transcriptions alter column percent_of_transcribed set not null;

alter table transcriptions add column used_space_quota int8;
update transcriptions set used_space_quota = 0;
alter table transcriptions alter column used_space_quota set not null;

update transcriptions set quota = 400000000 where quota is null;

insert into transcription_infos(id) (select id from transcriptions);
SELECT setval('transcription_infos_id_seq', (select max(id) + 1 from transcription_infos));
alter table transcriptions add column transcription_info_id int8;
update transcriptions set transcription_info_id = id;
alter table transcriptions alter column transcription_info_id set not null;

alter table verse_contents add column id int8;
update verse_contents set id = nextval('verse_contents_id_seq');
alter table verse_contents alter column id set not null;
alter table verse_contents drop constraint verse_contents_pkey;
alter table verse_contents add primary key (id);
alter table verse_contents alter column old_content drop not null;

alter table verse_contents add column plain_content varchar(4096);
update verse_contents set plain_content = content;
alter table verse_contents alter column plain_content set not null;
delete from verse_contents where verse_id not in (select id from verses);

alter table verses add column annotation boolean;
update verses set annotation = false;
alter table verses alter column annotation set not null;

alter table verses add column bold boolean;
update verses set bold = false;
alter table verses alter column bold set not null;

alter table verses add column italic boolean;
update verses set italic = false;
alter table verses alter column italic set not null;

alter table verses add column marginalia boolean;
update verses set marginalia = false;
alter table verses alter column marginalia set not null;

alter table verses add column notated_music boolean;
update verses set notated_music = false;
alter table verses alter column notated_music set not null;

alter table verses add column strikethrough boolean;
update verses set strikethrough = false;
alter table verses alter column strikethrough set not null;

alter table verses add column underline boolean;
update verses set underline = false;
alter table verses alter column underline set not null;

alter table users alter column quota type int8;
update users set quota = quota * 1000000;

alter table users alter column used_space_quota type int8;
update users set used_space_quota = used_space_quota * 1000000;

insert into layers (select nextval('layers_id_seq'), '', 'Domyślna', null, id from transcriptions);

insert into page_layers (select
  (select id from layers l where l.transcription_id = p.transcription_id),
  id,
  (select date_created from transcriptions where id = transcription_id),
  last_modification_date,
  last_modifier,
  percent_of_transcribed,
  percent_of_verified
from pages p);

update verses v set
page_layer_layer_id = (select l.id from layers l, pages p where l.transcription_id = p.transcription_id and v.page_id = p.id),
page_layer_page_id = (select p.id from pages p where v.page_id = p.id),
current_verse_content_id = (select max(id) from verse_contents where verse_id = v.id),
initial_verse_content_id = (select min(id) from verse_contents where verse_id = v.id);

insert into user_roles values ('contributor', 'contributor', 1, 'uczestnik');
update transcription_users set user_role_code = 'contributor' where user_role_code = 'transcriber';
update user_groups set user_role_code = 'contributor' where user_role_code = 'transcriber';
delete from user_roles where code = 'transcriber';

-- clean potential inconsistencies in verse numbering (duplicated verse nubmers in the same page/layer)
UPDATE verses
SET verse_no = subquery.row_num
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY page_id, page_layer_layer_id ORDER BY verse_no, id) AS row_num
    FROM verses
    where page_layer_layer_id in (
    	select page_layer_layer_id from verses group by page_id , page_layer_layer_id , verse_no having count(*) > 1)
) AS subquery
WHERE verses.id = subquery.id;

-- fix potential inconsistency: some transcriptions may not have their creator assigned as their admin
insert into transcription_users
(select id, creator_id, 'admin' from transcriptions t where not exists
  (select * from transcription_users tm where tm.transcription_id = t.id and tm.user_id = t.creator_id));

-- mark existing languages as available for transcription metadata
update languages set for_transcription_metadata = true;

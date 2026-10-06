--VERSE CONTENT BEGIN
CREATE INDEX IF NOT EXISTS vc_search_index ON verses USING gin(search_vector);

DROP TRIGGER IF EXISTS vc_search_vector_update ON verses;

CREATE TRIGGER vc_search_vector_update BEFORE INSERT OR UPDATE
ON verses FOR EACH ROW EXECUTE PROCEDURE
tsvector_update_trigger(search_vector, 'public.polish', plain_content);
--VERSE CONTENT END

--INDEX BEGIN
CREATE INDEX IF NOT EXISTS ix_search_index ON indexes USING gin(search_vector);

DROP TRIGGER IF EXISTS ix_search_vector_update ON indexes;

CREATE TRIGGER ix_search_vector_update BEFORE INSERT OR UPDATE
ON indexes FOR EACH ROW EXECUTE PROCEDURE
tsvector_update_trigger(search_vector, 'public.polish', name, description);
--INDEX END

--TRANSCRIPTION INFO BEGIN
CREATE INDEX IF NOT EXISTS ti_search_index ON transcription_infos USING gin(search_vector);

DROP TRIGGER IF EXISTS ti_search_vector_update ON transcription_infos;

CREATE TRIGGER ti_search_vector_update BEFORE INSERT OR UPDATE
ON transcription_infos FOR EACH ROW EXECUTE PROCEDURE
tsvector_update_trigger(search_vector, 'public.polish', release_date, description_plain, document_types, collection_name, contact_name, guidelines_plain);
--TRANSCRIPTION INFO END

--ANALYTIC INFO BEGIN
CREATE INDEX IF NOT EXISTS ai_search_index ON analytic_infos USING gin(search_vector);

DROP TRIGGER IF EXISTS ai_search_vector_update ON analytic_infos;

CREATE TRIGGER ai_search_vector_update BEFORE INSERT OR UPDATE
ON analytic_infos FOR EACH ROW EXECUTE PROCEDURE
tsvector_update_trigger(search_vector, 'public.polish', title);
--ANALYTIC INFO END

--MONOGRAFIC INFO BEGIN
CREATE INDEX IF NOT EXISTS mi_search_index ON monografic_infos USING gin(search_vector);

DROP TRIGGER IF EXISTS mi_search_vector_update ON monografic_infos;

CREATE TRIGGER mi_search_vector_update BEFORE INSERT OR UPDATE
ON monografic_infos FOR EACH ROW EXECUTE PROCEDURE
tsvector_update_trigger(search_vector, 'public.polish', title);
--MONOGRAFIC INFO END

--PROFILE DESC BEGIN
CREATE INDEX IF NOT EXISTS pd_search_index ON profile_descs USING gin(search_vector);

DROP TRIGGER IF EXISTS pd_search_vector_update ON profile_descs;

CREATE TRIGGER pd_search_vector_update BEFORE INSERT OR UPDATE
ON profile_descs FOR EACH ROW EXECUTE PROCEDURE
tsvector_update_trigger(search_vector, 'public.polish', summary, keywords_string);
--PROFILE DESC END
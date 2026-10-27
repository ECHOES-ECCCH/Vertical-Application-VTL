package pl.psnc.vtl.portal.repositories.transcriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.transcriptions.Language;

import java.util.List;

@Repository
public interface LanguageRepository extends JpaRepository<Language, String> {

    Language findByCode(String code);

    List<Language> findAllByForTranscriptionMetadataIsTrueOrderByOrd();
}

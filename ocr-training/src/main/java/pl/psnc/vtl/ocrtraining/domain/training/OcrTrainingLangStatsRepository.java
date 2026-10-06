package pl.psnc.vtl.ocrtraining.domain.training;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface OcrTrainingLangStatsRepository extends JpaRepository<OcrTrainingLangStats, Long> {

    Optional<OcrTrainingLangStats> findFirstByNewExamplesGreaterThanEqualOrderByNewExamplesDesc(long minNewExamples);

    Optional<OcrTrainingLangStats> findByLang(String lang);
}

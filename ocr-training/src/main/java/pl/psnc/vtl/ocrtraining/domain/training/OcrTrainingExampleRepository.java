package pl.psnc.vtl.ocrtraining.domain.training;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


interface OcrTrainingExampleRepository extends JpaRepository<OcrTrainingExample, Long> {

    List<OcrTrainingExample> findByLang(String lang, Pageable pageable);

    Optional<OcrTrainingExample> findByIiifImgUrlAndLang(String iiifImgUrl, String lang);
}

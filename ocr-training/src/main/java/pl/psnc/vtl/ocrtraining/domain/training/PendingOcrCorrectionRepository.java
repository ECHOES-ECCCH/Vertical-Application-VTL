package pl.psnc.vtl.ocrtraining.domain.training;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;


public interface PendingOcrCorrectionRepository extends JpaRepository<PendingOcrCorrection, Long> {

    Optional<PendingOcrCorrection> findByIiifImageUrl(String iiifImgUrl);
    boolean existsByIiifImageUrl(String iiifImgUrl);

    Stream<PendingOcrCorrection> findByLastRetryBeforeOrLastRetryIsNullOrderByQueueTime(LocalDateTime lastRetryBefore);
}

package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.pages.PageStatistics;
import pl.psnc.vtl.portal.dto.pages.PaginatedPagesStatistics;
import pl.psnc.vtl.portal.dto.transcriptions.statistics.LayerStatisticsDto;
import pl.psnc.vtl.portal.dto.transcriptions.statistics.TranscriptionStatistics;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.verses.projections.VersesStatsForLayer;
import pl.psnc.vtl.portal.model.verses.projections.VersesStatsForPage;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TranscriptionStatisticsService {
    private final TranscriptionPermissionService transcriptionPermissionService;

    private final VerseRepository verseRepository;

    private final TranscriptionRepository transcriptionRepository;


    public TranscriptionStatistics getTranscriptionStatistics(long transcriptionId, Pageable pageable) {

        checkExistenceAndRights(transcriptionId);

        TranscriptionStatistics stats = new TranscriptionStatistics();

        Transcription transcription = transcriptionRepository.getById(transcriptionId);
        stats.setTitleLabel(transcription.getTitleLabel());
        stats.setAuthorsLabel(transcription.getAuthorsLabel());
        stats.setHidden(transcription.isHidden());

        stats = updateWithLayersStats(stats, transcriptionId);

        stats = updateWithPagesStats(stats, transcriptionId, pageable,
                stats.getLayersStatistics().stream().map(LayerStatisticsDto::getLayerId).toArray(Long[]::new));

        return stats;
    }

    public Optional<Date> getMinLastUpdatedPublicTranscription() {
        Optional<LocalDateTime> minLastUpdatedDate = transcriptionRepository.getMinLastUpdatedDate(false);
        return minLastUpdatedDate.map(localDateTime -> Date.from(localDateTime.toInstant(ZoneOffset.UTC)));
    }

    private void checkExistenceAndRights(long transcriptionId) {
        if (!transcriptionRepository.existsById(transcriptionId)) {
            throw new EntityNotFoundException("Transcription with id: " + transcriptionId + " not found");
        }
        transcriptionPermissionService.checkVerifyAccess(transcriptionId);
    }

    private TranscriptionStatistics updateWithLayersStats(TranscriptionStatistics stats, long transcriptionId) {
        List<VersesStatsForLayer> lStats = verseRepository.getLayersStatistics(transcriptionId);

        stats.setLayersStatistics(lStats.stream().filter(s -> Objects.nonNull(s.getLayerId()))
                .map(LayerStatisticsDto::of).collect(Collectors.toList()));
        return stats;
    }

    private TranscriptionStatistics updateWithPagesStats(TranscriptionStatistics stats, long transcriptionId,
            Pageable pageable, Long... layersIds) {

        List<PaginatedPagesStatistics> pps = new ArrayList<>();

        for (Long layerId : layersIds) {
            org.springframework.data.domain.Page<VersesStatsForPage> pagesStats = verseRepository.getPagesStatisticsForLayer(
                    transcriptionId, layerId, pageable);
            pps.add(new PaginatedPagesStatistics(pagesStats.getTotalElements(), pagesStats.getContent().size(),
                    pageable.getPageNumber(), pageable.getPageSize(), pagesStats.getTotalPages(), layerId,
                    pagesStats.getContent().stream().map(PageStatistics::of).collect(Collectors.toList())));
        }

        stats.setPaginatedPagesStatistics(pps);

        return stats;
    }
}

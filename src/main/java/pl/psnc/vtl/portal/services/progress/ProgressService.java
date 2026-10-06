package pl.psnc.vtl.portal.services.progress;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.verses.VerseStatus;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository.VerseCount;

import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class ProgressService {

    private final VerseRepository verseRepository;
    private final PageRepository pageRepository;
    private final TranscriptionRepository transcriptionRepository;

    public Transcription computeProgress(Transcription transcription) {
        if (transcription.getViewMode() == ViewMode.VERIFY || transcription.getViewMode() == ViewMode.ADMIN) {
            transcription.setPercentOfVerified(
                    pageRepository.averagePercentOfVerifiedByTranscriptionId(transcription.getId()));
        } else {
            transcription.setPercentOfVerified(null);
        }
        return transcription;
    }

    public void updatePageProgress(long pageId) {
        Page page = pageRepository.getById(pageId);
        List<VerseCount> countsList = verseRepository.countByStatusAndLayer(pageId);
        long total = sum(countsList, null, null);
        if (total > 0) {
            long transcribed = sum(countsList, VerseStatus.TRANSCRIBED, null);
            long verified = sum(countsList, VerseStatus.VERIFIED, null);
            page.setPercentOfTranscribed((int) ((transcribed + verified) * 100 / total));
            page.setPercentOfVerified((int) (verified * 100 / total));
        } else {
            page.setPercentOfTranscribed(page.isEmpty() ? 100 : 0);
            page.setPercentOfVerified(page.isEmpty() ? 100 : 0);
        }

        for (PageLayer pageLayer : page.getPageLayers()) {
            long layerTotal = sum(countsList, null, pageLayer.getId());
            if (layerTotal > 0) {
                long layerTranscribed = sum(countsList, VerseStatus.TRANSCRIBED, pageLayer.getId());
                long layerVerified = sum(countsList, VerseStatus.VERIFIED, pageLayer.getId());
                pageLayer.setPercentOfTranscribed((int) ((layerTranscribed + layerVerified) * 100 / layerTotal));
                pageLayer.setPercentOfVerified((int) (layerVerified * 100 / layerTotal));
            } else {
                pageLayer.setPercentOfTranscribed(page.isEmpty() ? 100 : 0);
                pageLayer.setPercentOfVerified(page.isEmpty() ? 100 : 0);
            }
        }

        updateTranscriptionProgress(page.getTranscription().getId());
    }

    public void updateTranscriptionProgress(long transcriptionId) {
        Transcription transcription = transcriptionRepository.getById(transcriptionId);

        Integer progress = pageRepository.averagePercentOfTranscribedByTranscriptionId(transcriptionId);
        transcription.setPercentOfTranscribed(progress == null ? 100 : progress);
        transcriptionRepository.save(transcription);
    }

    private long sum(List<VerseCount> countsList, VerseStatus status, PageLayer.Id pageLayerId) {
        Stream<VerseCount> stream = countsList.stream();
        if (status != null)
            stream = stream.filter(vc -> vc.getStatus() == status);
        if (pageLayerId != null)
            stream = stream.filter(vc -> pageLayerId.equals(vc.getPageLayerId()));
        return stream.mapToLong(VerseCount::getCount).sum();
    }

    public void resetPageProgress(long pageId) {
        Page page = pageRepository.getById(pageId);
        page.setPercentOfTranscribed(page.isEmpty() ? 100 : 0);
        page.setPercentOfVerified(page.isEmpty() ? 100 : 0);

        for (PageLayer pageLayer : page.getPageLayers()) {
            pageLayer.setPercentOfTranscribed(page.isEmpty() ? 100 : 0);
            pageLayer.setPercentOfVerified(page.isEmpty() ? 100 : 0);
        }

        updateTranscriptionProgress(page.getTranscription().getId());
    }
}

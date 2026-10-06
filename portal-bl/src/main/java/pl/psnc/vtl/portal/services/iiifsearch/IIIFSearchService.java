package pl.psnc.vtl.portal.services.iiifsearch;

import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.openannotation.Annotation;
import de.digitalcollections.iiif.model.search.SearchHit;
import de.digitalcollections.iiif.model.search.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.iiifpresentation.IIIFPresentation2Service;
import pl.psnc.vtl.portal.services.iiifserver.IIIFUriResolver;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionService;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IIIFSearchService {

    private final TranscriptionService transcriptionService;

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final VerseRepository verseRepository;

    private final IIIFUriResolver iiifUriResolver;

    private final IIIFPresentation2Service iiifPresentation2Service;

    @SneakyThrows
    public String getSearchResults(long transcriptionId, String q, Long layerId) {
        String origin = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        transcriptionService.requireTranscription(transcriptionId);
        transcriptionPermissionService.checkReadAccess(transcriptionId);

        List<Verse> verses;
        if (layerId != null) {
            verses = verseRepository.findAllByTranscriptionIdAndLayerIdAndQ(transcriptionId, layerId, q);
        } else {
            verses = verseRepository.findAllByTranscriptionIdAndQ(transcriptionId, q);
        }

        String resultId =
                URI.create(origin + "/api/iiif/transcriptions/" + transcriptionId + "/search?q=" + q).toString();
        SearchResult searchResult = new SearchResult(resultId);
        for (Verse verse : verses) {
            String canvasId = iiifUriResolver.getComponentUrl(origin, transcriptionId, "canvas", verse.getPage().getId());
            Annotation annotation = iiifPresentation2Service.createAnnotation(transcriptionId, verse, canvasId);

            SearchHit searchHit = new SearchHit();
            searchHit.addAnnotation(new Annotation(annotation.getIdentifier().toString()));
            searchResult.addHit(searchHit);


            searchResult.addResource(annotation);
        }

        return new IiifObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(searchResult);
    }
}

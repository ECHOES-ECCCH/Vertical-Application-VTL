package pl.psnc.vtl.portal.utils.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.psnc.vtl.portal.dto.transcriptions.PaginatedTranscriptions;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionSearchResult;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Class storing search parameters and search response
 */
@Data
@AllArgsConstructor
public class OpenSearchConverterData {

    private final long count;
    private final List<Transcription> documents;
    private final int page;
    private final int perPage;
    private final String query;

    public OpenSearchConverterData(PaginatedTranscriptions<TranscriptionSearchResult> response, int page, int perPage,
            String query) {
        count = response.getCount();
        documents = response.getTranscriptions().stream().map(TranscriptionSearchResult::getTranscription)
                .collect(Collectors.toList());
        this.page = page;
        this.perPage = perPage;
        this.query = query;
    }

}

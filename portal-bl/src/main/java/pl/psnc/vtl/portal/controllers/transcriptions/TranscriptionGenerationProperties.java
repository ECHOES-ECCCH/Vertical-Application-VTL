package pl.psnc.vtl.portal.controllers.transcriptions;

import lombok.Builder;
import lombok.Getter;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;

@Builder
@Getter
public class TranscriptionGenerationProperties {

    private long transcriptionId;
    private TranscriptionPageContentType contentType;
    @Builder.Default
    private Long layerId = null;

}

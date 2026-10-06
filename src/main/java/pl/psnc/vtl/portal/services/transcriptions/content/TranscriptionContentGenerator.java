package pl.psnc.vtl.portal.services.transcriptions.content;

import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

public interface TranscriptionContentGenerator {
    boolean supports(TranscriptionPageContentType transcriptionPageContentType);

    String generate(Transcription transcription, Layer layer);
}

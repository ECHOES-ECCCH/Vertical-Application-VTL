package pl.psnc.vtl.portal.services.pages;

import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.pages.Page;

import java.util.Optional;

public interface ImportPageRecognizer {

    boolean supports(TranscriptionPageContentType transcriptionPageContentType);

    Optional<Page> recognizePage(long transcriptionId, String hocrContent, String fileName);

}

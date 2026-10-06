package pl.psnc.vtl.portal.services.pages.content;

import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;

import java.io.IOException;
import java.util.List;


public interface TranscriptionPageContentGenerator {

    boolean supports(TranscriptionPageContentType transcriptionPageContentType);

    List<ImmutablePair<Layer, String>> generate(Page page, List<Layer> layer) throws IOException;

}

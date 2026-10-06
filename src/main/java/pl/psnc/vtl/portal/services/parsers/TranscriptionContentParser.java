package pl.psnc.vtl.portal.services.parsers;

import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.verses.Verse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Note: implementations must implement at least one version of
 * {@code parseVerses} method.
 */
public interface TranscriptionContentParser {

    /**
     * @return list of verses divided per page in input
     */
    default List<List<Verse>> parseVerses(InputStream inputStream) throws IOException {
        return parseVerses(inputStream, null);
    }

    /**
     * @param transcriptionPages
     * @return list of verses divided per page in input
     */
    default List<List<Verse>> parseVerses(InputStream inputStream, List<Page> transcriptionPages)
            throws IOException {
        return parseVerses(inputStream);
    }

    boolean support(TranscriptionPageContentType pageContentType);

    default List<List<Verse>> parseVerses(String input) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
            return parseVerses(in);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected IOException on in-memory stream", e);
        }
    }
}

package pl.psnc.vtl.portal.dto.transcriptions;

/**
 * Search type object.
 */
public enum SearchType {
    /**
     * Search in all metadata and verse content.
     */
    ALL,
    /**
     * Search only in metadata.
     */
    METADATA,
    /**
     * Search only in verse content.
     */
    CONTENT,
    /**
     * Search only in transcription title and owner email.
     */
    TITLE_EMAIL
}

package pl.psnc.vtl.portal.dto.transcriptions;

/**
 * Search scope object.
 */
public enum SearchScope {
    /**
     * Search in all transcriptions.
     */
    ALL,
    /**
     * Search in transcription that current user is creator of.
     */
    CREATOR,
    /**
     * Search transcriptions that current user is member of.
     */
    MEMBER,
    /**
     * Search transcriptions that current user is creator or member of.
     */
    CREATOR_OR_MEMBER,
}

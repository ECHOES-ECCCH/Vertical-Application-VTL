package pl.psnc.vtl.portal.model.transcriptions;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status of the transcription that was imported from dLibra.
 */
public enum TranscriptionExportStatus {

    /**
     * Transcription wasn't imported from dLibra.
     */
    UNDEFINED,
    /**
     * Transcription was imported from dLibra, and could have changes.
     */
    NEW,
    /**
     * Transcription is marked as ready to export back to dLibra.
     */
    READY,
    /**
     * Transcription export was accepted and request will be sent to dLibra.
     */
    ACCEPTED,
    /**
     * Transcription export was rejected.
     */
    REJECTED,
    /**
     * Transcription export request was sent.
     */
    SENT,
    /**
     * Error occurred after sending export request.
     */
    ERROR;


    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }
}

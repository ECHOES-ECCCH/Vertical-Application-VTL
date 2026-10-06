package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Transcription info core object.
 */
@Data
@NoArgsConstructor
public class TranscriptionInfoCore {

    /**
     * Source object release date.
     */
    private String releaseDate;

    /**
     * Source object description.
     */
    private String description;

    /**
     * Source object document types.
     */
    private String documentTypes;

    /**
     * Source object document sources.
     */
    private String documentSrc;

    /**
     * Source object image source.
     */
    private String imgSrc;

    /**
     * Source object collection name.
     */
    private String collectionName;

    /**
     * Transcription creator contact information.
     */
    private String contactName;
    private String contactSurname;
    private String contactEmail;

    /**
     * Guidelines for project members.
     */
    private String guidelines;

}

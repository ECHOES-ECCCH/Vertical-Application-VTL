package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionExportStatus;

import java.time.LocalDateTime;

/**
 * Transcription export dto object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionExportDto {

    /**
     * Transcription id.
     */
    private long id;

    /**
     * Transcription last modification date.
     */
    private LocalDateTime lastModificationDate;

    /**
     * Transcription title.
     */
    private String title;

    /**
     * Transcription creator email.
     */
    private String creator;

    /**
     * Transcription export status.
     */
    private TranscriptionExportStatus status;

}

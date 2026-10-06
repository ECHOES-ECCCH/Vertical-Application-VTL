package pl.psnc.vtl.portal.dto.transcriptions.list;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Transcription info for dace dto object.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranscriptionInfoForDaceDto extends TranscriptionInfoDto {

    /**
     * Transcription title.
     */
    private String title;

    /**
     * Transcription creation date.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private OffsetDateTime dateCreated;

    /**
     * Transcription publication date.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private OffsetDateTime datePublished;

    /**
     * Transcription creator.
     */
    private String creator;

    @Builder(builderMethodName = "infoDaceDtoBuilder")
    public TranscriptionInfoForDaceDto(long id, String title, String creator, LocalDateTime dateCreated, LocalDateTime datePublished) {
        super(id, null);
        this.title = title;
        this.datePublished = datePublished.atOffset(ZoneOffset.UTC);
        this.dateCreated = dateCreated.atOffset(ZoneOffset.UTC);
        this.creator = creator;
    }
}

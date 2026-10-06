package pl.psnc.vtl.portal.dto.transcriptions.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Transcriptions for dace dto object.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TranscriptionsForDaceDto {

    /**
     * List of transcription info for dace dto.
     */
    private List<TranscriptionInfoForDaceDto> projects;
}

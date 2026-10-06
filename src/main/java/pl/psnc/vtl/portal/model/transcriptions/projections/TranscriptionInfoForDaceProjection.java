package pl.psnc.vtl.portal.model.transcriptions.projections;

import pl.psnc.vtl.portal.dto.transcriptions.list.*;

import java.time.LocalDateTime;

public interface TranscriptionInfoForDaceProjection extends TranscriptionInfoProjection<TranscriptionInfoForDaceDto> {
    LocalDateTime getDateCreated();
    LocalDateTime getDatePublished();
    String getCreator();
    @Override
    default TranscriptionInfoForDaceDto toTranscriptionInfoDto() {
        return TranscriptionInfoForDaceDto.infoDaceDtoBuilder().id(getId()).title(getAnalyticInfoTitle())
                .creator(getCreator()).datePublished(getDatePublished()).dateCreated(getDateCreated()).build();
    }
}

package pl.psnc.vtl.portal.model.transcriptions.projections;

import pl.psnc.vtl.portal.dto.transcriptions.list.TranscriptionInfoDto;

public interface TranscriptionInfoProjection<U extends TranscriptionInfoDto> {
    long getId();
    String getAnalyticInfoTitle();
    U toTranscriptionInfoDto();
}

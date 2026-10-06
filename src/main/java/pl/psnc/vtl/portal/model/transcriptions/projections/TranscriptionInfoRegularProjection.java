package pl.psnc.vtl.portal.model.transcriptions.projections;

import pl.psnc.vtl.portal.dto.transcriptions.list.AnalyticInfoDto;
import pl.psnc.vtl.portal.dto.transcriptions.list.TranscriptionInfoRegularDto;
import pl.psnc.vtl.portal.dto.transcriptions.list.UserInfoDto;

public interface TranscriptionInfoRegularProjection extends TranscriptionInfoProjection<TranscriptionInfoRegularDto> {
    long getOwnerId();
    String getOwnerEmail();
    Long getQuota();
    Long getUserSpaceQuota();

    @Override
    default TranscriptionInfoRegularDto toTranscriptionInfoDto() {
        UserInfoDto uli = UserInfoDto.builder().id(getOwnerId()).email(getOwnerEmail()).build();
        AnalyticInfoDto ali = AnalyticInfoDto.builder().title(getAnalyticInfoTitle()).build();
        return TranscriptionInfoRegularDto.infoRegularDtoBuilder().id(getId()).analyticInfo(ali).creator(uli)
                .quota(getQuota()).userSpaceQuota(getUserSpaceQuota()).build();
    }
}

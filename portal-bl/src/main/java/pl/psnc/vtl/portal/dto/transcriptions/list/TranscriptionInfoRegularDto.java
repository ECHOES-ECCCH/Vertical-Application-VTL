package pl.psnc.vtl.portal.dto.transcriptions.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Transcription info regular dto object.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TranscriptionInfoRegularDto extends TranscriptionInfoDto {

    /**
     * Transcription creator info.
     */
    private UserInfoDto creator;

    /**
     * Transcription max space quota.
     */
    private Integer quota;

    /**
     * Transcription used up quota.
     */
    private Integer userSpaceQuota;

    @Builder(builderMethodName = "infoRegularDtoBuilder")
    public TranscriptionInfoRegularDto(long id, AnalyticInfoDto analyticInfo, UserInfoDto creator, Long quota, Long userSpaceQuota) {
        super(id, analyticInfo);
        this.creator = creator;
        this.quota = quota != null ? (int) (quota / 1000000) : 0;
        this.userSpaceQuota = userSpaceQuota != null ? (int) (userSpaceQuota / 1000000) : 0;
    }
}

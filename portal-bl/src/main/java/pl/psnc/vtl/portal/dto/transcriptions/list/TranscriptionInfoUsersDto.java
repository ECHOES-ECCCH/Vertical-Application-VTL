package pl.psnc.vtl.portal.dto.transcriptions.list;

import lombok.*;

import java.util.List;

/**
 * Transcription info users dto object.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TranscriptionInfoUsersDto extends TranscriptionInfoDto {

    /**
     * Transcription owner email.
     */
    private String ownerEmail;

    /**
     * List of transcription members.
     */
    private List<UserInfoDto> users;

    @Builder(builderMethodName = "infoUsersDtoBuilder")
    public TranscriptionInfoUsersDto(long id, AnalyticInfoDto analyticInfo, String ownerEmail,
            List<UserInfoDto> users) {
        super(id, analyticInfo);
        this.users = users;
        this.ownerEmail = ownerEmail;
    }
}

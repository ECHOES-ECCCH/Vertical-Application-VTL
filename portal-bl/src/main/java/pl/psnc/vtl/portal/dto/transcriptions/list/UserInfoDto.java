package pl.psnc.vtl.portal.dto.transcriptions.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User info dto object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDto {

    /**
     * User id.
     */
    private long id;

    /**
     * User email.
     */
    private String email;
}

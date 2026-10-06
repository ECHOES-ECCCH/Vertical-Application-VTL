package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Actor core object.
 */
@Data
@NoArgsConstructor
public class ActorCore {

    /**
     * Actor name.
     */
    private String name;

    /**
     * Actor role id.
     */
    private ActorRoleId role;
}

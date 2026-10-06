package pl.psnc.vtl.portal.dto.users;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.users.User;

/**
 * User group core object.
 */
@Data
@NoArgsConstructor
public class UserGroupCore {

    /**
     * User reference.
     */
    private User user;

    /**
     * Role in group.
     */
    private UserRoleId role;

}

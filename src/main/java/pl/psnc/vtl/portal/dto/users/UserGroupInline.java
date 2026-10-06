package pl.psnc.vtl.portal.dto.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.users.UserRole;

/**
 * User group in line object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroupInline {

    /**
     * User reference.
     */
    private User user;

    /**
     * User role reference.
     */
    private UserRole role;

}

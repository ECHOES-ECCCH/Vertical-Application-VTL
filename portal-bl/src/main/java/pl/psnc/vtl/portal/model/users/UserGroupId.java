package pl.psnc.vtl.portal.model.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * UserGroupId object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupId implements Serializable {

    /**
     * User
     */
    private Long user;

    /**
     * Group
     */
    private Long group;

}

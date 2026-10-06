package pl.psnc.vtl.portal.dto.users;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.users.Group;
import pl.psnc.vtl.portal.model.users.User;

import java.util.List;

/**
 * User profile object.
 */
@Data
@NoArgsConstructor
public class UserProfile {

    private UserRegularDto user;

    /**
     * List of groups that given user created.
     */
    private List<Group> myGroups;

    /**
     * List of groups that given user is member of.
     */
    private List<Group> otherGroups;

}

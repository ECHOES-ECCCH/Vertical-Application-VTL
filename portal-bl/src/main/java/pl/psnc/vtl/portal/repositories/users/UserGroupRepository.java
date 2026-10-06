package pl.psnc.vtl.portal.repositories.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.users.UserGroup;
import pl.psnc.vtl.portal.model.users.UserGroupId;

import java.util.List;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, UserGroupId> {

    List<UserGroup> findByUserId(Long userId);

    List<UserGroup> findByGroupId(Long groupId);

    List<UserGroup> findByGroupIdIn(List<Long> groupIds);

    List<UserGroup> findByGroupIdOrderByCreationDateAsc(Long groupId);

    boolean existsByGroupIdAndUserEmail(Long groupId, String email);

    void deleteByGroupId(Long groupId);

}

package pl.psnc.vtl.portal.repositories.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.users.Group;
import pl.psnc.vtl.portal.model.users.User;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    boolean existsByCreatorEmailAndName(String email, String name);

    List<Group> findByCreator(User user);

}

package pl.psnc.vtl.portal.repositories.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.users.UserRole;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {

}

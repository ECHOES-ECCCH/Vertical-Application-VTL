package pl.psnc.vtl.portal.repositories.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.users.User;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Set<User> findAllUsersBySecurityGroupContaining(SecurityGroup securityGroup);

    Page<User> findAllByEmailContainsIgnoreCase(String q, Pageable pageable);

}

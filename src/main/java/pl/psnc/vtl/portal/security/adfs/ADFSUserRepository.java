package pl.psnc.vtl.portal.security.adfs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ADFSUserRepository extends JpaRepository<ADFSUser, String> {

}

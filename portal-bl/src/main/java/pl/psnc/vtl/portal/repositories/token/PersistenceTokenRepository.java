package pl.psnc.vtl.portal.repositories.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.psnc.vtl.portal.model.registration.PersistenceToken;
import pl.psnc.vtl.portal.model.registration.TokenType;
import pl.psnc.vtl.portal.model.users.User;

@Repository
public interface PersistenceTokenRepository extends JpaRepository<PersistenceToken, String> {

    void deleteAllByUserAndTokenType(User user, TokenType type);
}

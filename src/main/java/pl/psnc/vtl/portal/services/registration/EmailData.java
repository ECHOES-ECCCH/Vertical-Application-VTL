package pl.psnc.vtl.portal.services.registration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.psnc.vtl.portal.model.registration.PersistenceToken;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.User;

@Getter
@RequiredArgsConstructor
public class EmailData {
    private final User user;
    private final PersistenceToken persistenceToken;
    private final String clientRedirectURL;
    private final StaticFileLanguage lang;
}

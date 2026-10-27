package pl.psnc.vtl.portal.services.registration;

import lombok.Getter;
import pl.psnc.vtl.portal.model.registration.PersistenceToken;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.User;

@Getter
public class FederationEmailData extends EmailData {
	private final String client;

	public FederationEmailData(User user, PersistenceToken persistenceToken, String clientRedirectURL, StaticFileLanguage lang, String client) {
		super(user, persistenceToken, clientRedirectURL, lang);
		this.client = client;
	}
}

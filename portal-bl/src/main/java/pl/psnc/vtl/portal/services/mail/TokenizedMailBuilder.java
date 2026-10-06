package pl.psnc.vtl.portal.services.mail;

import pl.psnc.vtl.portal.services.registration.EmailData;

public interface TokenizedMailBuilder {

    String makeEmailWithTokenBody(EmailData emailData);

}

package pl.psnc.vtl.portal.services.registration;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.psnc.vtl.portal.model.registration.PersistenceToken;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.statics.StaticFilesRepository;
import pl.psnc.vtl.portal.services.mail.TokenizedMailBuilder;

import static org.junit.Assert.*;

public class TokenizedMailBuilderTest {

    private TokenizedMailBuilder tokenizedMailBuilder;

    private String TOKEN = "myUglyTokenValue//.'][123123][p[]ZXCVżźęę@@32213ł";

    private EmailData emailData;


    @Before
    public void init(){
        StaticFilesRepository repository = new StaticFilesRepository();
        ReflectionTestUtils.setField(repository, "filesLocation", "./vtl-config/");
        tokenizedMailBuilder = new RegistrationMailBuilder(repository);

        PersistenceToken token = new PersistenceToken();
        token.setUser(new User());
        token.setToken(TOKEN);
        emailData = new EmailData(new User(), token, "url", StaticFileLanguage.EN);
    }

    @Ignore("fileLocation cannot be constant in tests because tests are run on different environment, in docker for instance there is no ./vtl-config dir")
    @Test
    public void shouldContainToken() {
        String mailBody = tokenizedMailBuilder.makeEmailWithTokenBody(emailData);
        assertNotNull(mailBody);
        assertTrue(mailBody.contains(TOKEN));
    }

    @Ignore("fileLocation cannot be constant in tests because tests are run on different environment, in docker for instance there is no ./vtl-config dir")
    @Test
    public void shouldNotContainWrongToken() {
        String mailBody = tokenizedMailBuilder.makeEmailWithTokenBody(emailData);
        assertNotNull(mailBody);
        assertFalse(mailBody.contains(TOKEN+"-"));
    }
}
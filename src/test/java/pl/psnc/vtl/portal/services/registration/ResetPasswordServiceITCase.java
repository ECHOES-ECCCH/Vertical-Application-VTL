package pl.psnc.vtl.portal.services.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.dto.registration.ResetPasswordCore;
import pl.psnc.vtl.portal.dto.registration.ResetPasswordRequest;
import pl.psnc.vtl.portal.model.registration.PersistenceToken;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.repositories.token.PersistenceTokenRepository;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@Transactional
public class ResetPasswordServiceITCase {


    ObjectMapper objectMapper = new ObjectMapper();
    private static final String EMAIL = "email@wp.pl";
    private static final String PASSWORD = "myPassword";

    @Autowired
    ResetPasswordService service;

    @Autowired
    PersistenceTokenRepository persistenceTokenRepository;
    @Test
    public void ShouldDeleteAllTokensAfterOneWasUsed(){
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setEmail(EMAIL);
        resetPasswordRequest.setLang(StaticFileLanguage.EN);
        resetPasswordRequest.setClientRedirectUrl("redirectUr");

        service.sendEmailToResetPassword(resetPasswordRequest);
        service.sendEmailToResetPassword(resetPasswordRequest);
        service.sendEmailToResetPassword(resetPasswordRequest);

        List<PersistenceToken> all = persistenceTokenRepository.findAll();
        Assert.assertEquals(3, all.size());

        ResetPasswordCore resetPasswordCore = new ResetPasswordCore();
        resetPasswordCore.setNewPassword("newPassword11@$");
        resetPasswordCore.setRepeatedNewPassword("newPassword11@$");
        resetPasswordCore.setToken(all.get(0).getToken());
        
        service.resetPassword(resetPasswordCore);

        Assert.assertTrue(persistenceTokenRepository.findAll().isEmpty());

    }


}
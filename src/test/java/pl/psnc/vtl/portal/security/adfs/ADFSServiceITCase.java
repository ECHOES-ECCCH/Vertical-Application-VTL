package pl.psnc.vtl.portal.security.adfs;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;

import javax.mail.internet.MimeMessage;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@Transactional
public class ADFSServiceITCase {

    @Autowired
    ADFSService adfsService;
    @Autowired
    ADFSUserRepository adfsUserRepository;

    @Test
    public void shouldRegisterAdfsUser(){
        String uuid = "uuid";
        String email = "email";
        ADFSIdentifier identifier = new ADFSIdentifier(uuid, email);
        adfsService.getOrCreateUser(identifier, StaticFileLanguage.EN);
        ADFSUser adfsUser = adfsUserRepository.getOne(uuid);
        Assert.assertEquals(uuid, adfsUser.uuid);
        Assert.assertEquals(email, adfsUser.email);
        Assert.assertNotNull(adfsUser.user);
        Assert.assertEquals(email, adfsUser.user.getEmail());
    }

    @Test
    public void shouldSendEmailWhenEmailChanged(){
        String uuid = "uuid";
        String email = "email";
        //CHANGE IT TO EMAIL
        String changedEmail = "typeYourMailHere";
        ADFSIdentifier identifier = new ADFSIdentifier(uuid, email);
        ADFSIdentifier anotherIdentifier = new ADFSIdentifier(uuid, changedEmail);
        adfsService.getOrCreateUser(identifier, StaticFileLanguage.EN);
        adfsService.getOrCreateUser(anotherIdentifier, StaticFileLanguage.EN);
        ADFSUser adfsUser = adfsUserRepository.getOne(uuid);
        Assert.assertEquals(uuid, adfsUser.uuid);
        Assert.assertEquals(changedEmail, adfsUser.email);
        Assert.assertNotNull(adfsUser.user);
        Assert.assertEquals(changedEmail, adfsUser.user.getEmail());
    }


}
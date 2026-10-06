package pl.psnc.vtl.portal.security.adfs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.QuotaConfiguration;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.services.registration.RegistrationService;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ADFSService {

    private static final String ADFS_CLIENT = "Active Directory";

    private final ADFSUserRepository adfsUserRepository;
    private final RegistrationService registrationService;
    private final UserRepository userRepository;
    private final ADFSMailService adfsMailService;
    private final QuotaConfiguration quotaConfiguration;

    public User getOrCreateUser(ADFSIdentifier identifier, StaticFileLanguage language) {
        ADFSUser adfsUser = adfsUserRepository.findById(identifier.uuid)
                .orElseGet(() -> registerAdfsUser(identifier, language  ));
        sendEmailIfEmailChanged(adfsUser, identifier);
        return adfsUser.user;
    }

    private void sendEmailIfEmailChanged(ADFSUser adfsUser, ADFSIdentifier identifier) {
        User user = adfsUser.user;
        if (identifier.email.equals(user.getEmail())) return;
        log.debug("Sending ADFS email, email identifier email: " + identifier.email + ", user email: " + user.getEmail());
        adfsUser.setEmail(identifier.email);
        adfsUserRepository.save(adfsUser);
        user.setEmail(identifier.email);
        userRepository.save(user);
        adfsMailService.sendMailChangedEmail(adfsUser.email);
    }

    private ADFSUser registerAdfsUser(ADFSIdentifier identifier, StaticFileLanguage language) {
        User user = userRepository.findByEmail(identifier.email)
                .orElseGet(() -> registrationService.registerFederationUser(identifier.email, null, ADFS_CLIENT,
                        language,
                        SecurityGroup.USER, quotaConfiguration.getSpace() * 1000000L, null,true
                ));
        ADFSUser adfsUser = new ADFSUser(identifier.uuid, identifier.email, user);
        return adfsUserRepository.save(adfsUser);
    }
}

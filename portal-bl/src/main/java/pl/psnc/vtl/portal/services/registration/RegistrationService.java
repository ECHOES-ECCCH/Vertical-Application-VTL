package pl.psnc.vtl.portal.services.registration;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.registration.UserRegisterCore;
import pl.psnc.vtl.portal.model.registration.PersistenceToken;
import pl.psnc.vtl.portal.model.registration.TokenType;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.QuotaConfiguration;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.security.SecurityConfiguration;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PersistenceTokenProvider persistanceTokenProvider;
    private final RegistrationEmailService registrationEmailService;
    private final FederationRegistrationEmailService federationRegistrationEmailService;
    private final UserRegistrationRequestValidator userRegistrationRequestValidator;
    private final QuotaConfiguration quotaConfiguration;
    private final SecurityConfiguration securityConfiguration;


    public User registerUser(UserRegisterCore userRegistrationRequest)
            throws InvalidUserDataException {
        validateRequest(userRegistrationRequest);
        User user = createUserFrom(userRegistrationRequest);
        PersistenceToken registrationToken = persistanceTokenProvider.createToken(user, TokenType.REGISTRATION_TOKEN);
        EmailData emailData = new EmailData(user, registrationToken, userRegistrationRequest.getClientRedirectURL(),
                userRegistrationRequest.getLang());
        registrationEmailService.sendEmail(emailData);
        return user;
    }

    private User createUserFrom(UserRegisterCore newUser) {
        User user = User.builder()
                .password(passwordEncoder.encode(newUser.getPassword()))
                .email(newUser.getEmail())
                .acceptedRegulations(LocalDateTime.now())
                .enabled(false)
                .quota(quotaConfiguration.getSpace() * 1000000L)
                .usedSpaceQuota(0L)
                .hasPassword(true)
                .securityGroup(SecurityGroup.USER)
                .build();
        return userRepository.save(user);
    }

    private void validateRequest(UserRegisterCore newUser) {
        userRegistrationRequestValidator.validate(newUser);
    }


    public void activateAccount(String tokenValue) {
        User user = persistanceTokenProvider.getUserbyToken(tokenValue);
        user.setEnabled(true);
        userRepository.save(user);
        persistanceTokenProvider.deleteTokenByValue(tokenValue);
    }

    public User getOrCreateUser(String email, String name, String client, StaticFileLanguage lang, SecurityGroup role,
                                int quota, LocalDateTime tsAcceptedTerms, boolean hasAccess) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> registerFederationUser(email, name, client, lang, role, quota, tsAcceptedTerms,
                        hasAccess));
    }

    public User syncOrCreateUser(String email, String name, String client, StaticFileLanguage lang, SecurityGroup role,
                                 long quota, LocalDateTime tsAcceptedTerms, boolean hasAccess) {
        Optional<User> u = userRepository.findByEmail(email);
        return u.map(user -> syncUser(user, name, role, quota, hasAccess)).orElseGet(
                () -> registerFederationUser(email, name, client, lang, role, quota, tsAcceptedTerms, hasAccess));
    }

    public User registerFederationUser(String email, String name, String client, StaticFileLanguage lang,
                                       SecurityGroup role, long quota, LocalDateTime tsAcceptedTerms,
                                       boolean hasAccess) {
        User user = createUser(email, name, role, quota, tsAcceptedTerms, hasAccess);
        if (securityConfiguration.getRegistrationEmail())
            this.sendRegistrationInfo(user, client, lang);
        return user;
    }

    private void sendRegistrationInfo(User user, String client, StaticFileLanguage lang) {
        FederationEmailData emailData = new FederationEmailData(user, null, null, lang, client);
        federationRegistrationEmailService.sendEmail(emailData);
    }

    private User createUser(String email, String name,
                            SecurityGroup role, long quota, LocalDateTime tsAcceptedTerms, boolean hasAccess) {

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(UUID.randomUUID().toString());
        user.setHasPassword(false);
        //TODO: auto accept terms?
        user.setAcceptedRegulations(Objects.requireNonNullElseGet(tsAcceptedTerms, LocalDateTime::now));
        //lock user
        user.setEnabled(hasAccess);
//        user.setEnabled(true);
        user.setSecurityGroup(role);
        user.setQuota(quota);
        user.setUsedSpaceQuota(0L);
        return userRepository.saveAndFlush(user);
    }

    private User syncUser(User user, String name,
                          SecurityGroup role, long quota, boolean hasAccess) {
        user.setName(name);
        user.setEnabled(hasAccess);
        user.setSecurityGroup(role);
        //user.setQuota(quota);
        return userRepository.saveAndFlush(user);
    }

}

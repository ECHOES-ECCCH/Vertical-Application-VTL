package pl.psnc.vtl.portal.services.users;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.dto.users.UserGroupInline;
import pl.psnc.vtl.portal.dto.users.UserPasswordCore;
import pl.psnc.vtl.portal.dto.users.UserProfile;
import pl.psnc.vtl.portal.dto.users.UserRegularDto;
import pl.psnc.vtl.portal.model.users.*;
import pl.psnc.vtl.portal.repositories.users.UserGroupRepository;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserGroupRepository userGroupRepository;

    private final PasswordEncoder passwordEncoder;
    private final UserPasswordCoreValidator userPasswordCoreValidator;

    private final LoggedInUserHolder loggedInUserHolder;

    @Transactional(readOnly = true)
    public UserProfile getMyProfile() {
        UserProfile userProfile = new UserProfile();
        User loggedInUser = loggedInUserHolder.getLoggedInUser();
        User user = userRepository.findById(loggedInUser.getId()).orElseThrow(EntityNotFoundException::new);
        userProfile.setUser(new UserRegularDto(user));

        List<UserGroup> allGroups = userGroupRepository.findByUserId(user.getId());
        List<Group> otherGroups = new ArrayList<>();
        List<Group> myGroups = new ArrayList<>();

        for (UserGroup userGroup : allGroups) {
            if (userGroup.getRole().getCode().equals("admin")) {
                myGroups.add(userGroup.getGroup());
            } else {
                otherGroups.add(userGroup.getGroup());
            }
        }
        myGroups.forEach(this::hideMembersQuota);
        otherGroups.forEach(this::hideAllQuotas);

        userProfile.setOtherGroups(otherGroups);
        userProfile.setMyGroups(myGroups);
        return userProfile;
    }


    private void hideAllQuotas(Group group) {
            hideMembersQuota(group);
            group.getCreator().setQuota(null);
            group.getCreator().setUsedSpaceQuota(null);
    }


    private void hideMembersQuota(Group group) {
        if (group.getMembers() != null) {
            for (UserGroupInline member : group.getMembers()) {
                member.getUser().setQuota(null);
                member.getUser().setUsedSpaceQuota(null);
            }
        }
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + User.class + " with email " + email));
    }

    @Transactional
    public void updateMyPassword(UserPasswordCore userPasswordCore) {
        userPasswordCoreValidator.validate(userPasswordCore);
        User user = loggedInUserHolder.getLoggedInUser();
        user = userRepository.findById(user.getId()).orElseThrow(EntityNotFoundException::new);
        String encodedPassword = passwordEncoder.encode(userPasswordCore.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    @Transactional
    public void updateUsedSpaceQuota(User user, long quota) {
        user = userRepository.findById(user.getId()).orElseThrow(EntityNotFoundException::new);
        user.setUsedSpaceQuota(user.getUsedSpaceQuota() + quota);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public void validateUsedSpaceQuota(User user) {
        user = userRepository.findById(user.getId()).orElseThrow(EntityNotFoundException::new);
        if (user.getQuota() < user.getUsedSpaceQuota()) {
            throw new QuotaException("User Quota exceeded error", "quota.error.user.exceeded");
        }
    }

    @Transactional(readOnly = true)
    public String[] getAllSuperadminUsersMails() {
        return userRepository.findAllUsersBySecurityGroupContaining(SecurityGroup.SUPER_ADMIN).stream()
                .map(User::getEmail).toArray(String[]::new);
    }
}

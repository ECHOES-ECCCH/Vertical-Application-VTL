package pl.psnc.vtl.portal.services.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.conf.logging.LogAction;
import pl.psnc.vtl.portal.dto.users.PaginatedUsers;
import pl.psnc.vtl.portal.dto.users.UserRegularDto;
import pl.psnc.vtl.portal.model.users.QuotaException;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.users.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;

    public static final String USER_EMAIL_COLUMN_NAME = "email";

    @Value("${vtl.pagination.users.default-perpage}")
    private Integer usersDefaultPerpage;

    @Value("${vtl.pagination.users.maximal-perpage}")
    private Integer usersMaxPerpage;

    public User disableUser(long id) {
        User user = loadUser(id);
        user.setEnabled(false);

        LogAction.setMDCAction(LogAction.MODIFY_DATA);
        log.info("Disabled User with id - {}.", user.getId());
        LogAction.clearMDCAction();

        return userRepository.save(user);
    }


    public User enableUser(long id) {
        User user = loadUser(id);
        user.setEnabled(true);

        LogAction.setMDCAction(LogAction.MODIFY_DATA);
        log.info("Enabled User with id - {}.", user.getId());
        LogAction.clearMDCAction();

        return userRepository.save(user);
    }

    public User trustUser(long userId) {
        User user = loadUser(userId);
        user.setTrustedContributor(true);

        LogAction.setMDCAction(LogAction.MODIFY_DATA);
        log.info("Added User with id - {} to trusted contributors list.", user.getId());
        LogAction.clearMDCAction();

        return userRepository.save(user);
    }

    public User untrustUser(long userId) {
        User user = loadUser(userId);
        user.setTrustedContributor(false);

        LogAction.setMDCAction(LogAction.MODIFY_DATA);
        log.info("Removed User with id - {} from trusted contributors list.", user.getId());
        LogAction.clearMDCAction();

        return userRepository.save(user);
    }

    private User loadUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Unable to find %s with id %d", User.class, userId)));
    }


    public PaginatedUsers searchUsers(Integer page, Integer perpage, Sort.Direction orderDir, String q) {

        if (Objects.isNull(perpage)) {
            perpage = usersDefaultPerpage;
        }

        // numbering of pages in the api starts with 1, but database paging mechanism starts with 0, hence we need to adjust request
        Pageable pageable = PageRequest.of(page - 1, Math.min(perpage, usersMaxPerpage), Sort.by(orderDir,
                USER_EMAIL_COLUMN_NAME));

        Page<User> usersPage = userRepository.findAllByEmailContainsIgnoreCase(q, pageable);
        List<UserRegularDto> userList = usersPage.getContent().stream().map(UserRegularDto::new).collect(Collectors.toList());

        return PaginatedUsers.builder()
                .users(userList)
                .count(usersPage.getContent().size())
                .hits(usersPage.getTotalElements())
                .page(page)
                .perPage(perpage)
                .pages(usersPage.getTotalPages())
                .build();
    }

    public void updateUserRole(SecurityGroup role, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id: " + id + " not found"));
        user.setSecurityGroup(role);

        LogAction.setMDCAction(LogAction.MODIFY_DATA);
        log.info("Updated role for User with id - {}. New role - {}", user.getId(), role.name());
        LogAction.clearMDCAction();

        userRepository.save(user);
    }


    public void updateUserQuota(long userId, Integer quota) {
        if (quota <= 0) {
            throw new QuotaException("Quota must be greater than 0","quota.error.invalid.parameter");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id: " + userId + " not found"));
        user.setQuota(quota * 1000000L);

        LogAction.setMDCAction(LogAction.MODIFY_DATA);
        log.info("Updated quota for User with id - {}. New quota - {}", user.getId(), quota);
        LogAction.clearMDCAction();

        userRepository.save(user);
    }
}

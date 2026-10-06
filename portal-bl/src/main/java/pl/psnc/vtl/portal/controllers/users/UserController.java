package pl.psnc.vtl.portal.controllers.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.users.UserPasswordCore;
import pl.psnc.vtl.portal.dto.users.UserProfile;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.security.SecurityConfiguration;
import pl.psnc.vtl.portal.services.users.UserRemovalService;
import pl.psnc.vtl.portal.services.users.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    private final UserRemovalService userRemovalService;

    private final SecurityConfiguration securityConfiguration;

    @Operation(summary = "Retrieve user's profile")
    @GetMapping(path = "/my-profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserProfile> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }


    @Operation(summary = "Updates the user's password based on a pair of an old and new password.")
    @PutMapping(path = "/my-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateMyPassword(@RequestBody UserPasswordCore userPasswordCore) {
        userService.updateMyPassword(userPasswordCore);
    }


    @Operation(summary = "Retrieve user by its email address")
    @GetMapping(path = "/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getUserByEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @Operation(summary = "Removes currently logged-in user")
    @DeleteMapping(path = "/current", produces = MediaType.APPLICATION_JSON_VALUE)
    public void removeCurrentUser(
            @RequestParam(required = false, defaultValue = "false") boolean removePublished,
            HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        userRemovalService.removeLoggedInUser(removePublished);
        request.logout();
        String redirectUri = securityConfiguration.getPostLogoutRedirectUri(request, auth);
        if (redirectUri != null) {
            response.setHeader("Location", redirectUri);
        }
    }

}

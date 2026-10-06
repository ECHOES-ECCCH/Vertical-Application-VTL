package pl.psnc.vtl.portal.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.dto.users.PaginatedUsers;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.services.admin.AdminService;

import javax.validation.constraints.Min;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Operations reserved for Admin user")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @Operation(summary = "Change user account availability based on the flag provided")
    @PutMapping("/users/{id}")
    public ResponseEntity<User> changeUserAccountAvailability(
            @PathVariable("id") long userId,
            @RequestParam(value = "enabled") boolean enabled) {
        return ResponseEntity.ok(enabled ? adminService.enableUser(userId) : adminService.disableUser(userId));
    }

    @Operation(summary = "Mark user as trusted contributor")
    @PutMapping(path = "/users/{id}/trust", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> changeUserTrust(@PathVariable("id") long userId,
                                                @RequestParam(name = "trust", defaultValue = "true") boolean trust) {
        User user = trust ? adminService.trustUser(userId) : adminService.untrustUser(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Retrieves list of users based on given params. Users are sorted by e-mail adress.")
    @GetMapping(path = "/users")
    public ResponseEntity<PaginatedUsers> searchUsers(
            @Parameter(description = "Query applied to the user's email") @RequestParam(required = false, defaultValue = "") String q,
            @Parameter(description = "Number of the page to be retrieved") @RequestParam(required = false, defaultValue = "1") @Min(1) Integer page,
            @Parameter(description = "Number of users to be retrieved on a given page") @RequestParam(required = false) @Min(1) Integer perpage,
            @Parameter(description = "Sort direction of the results") @RequestParam(required = false, defaultValue = "asc", name="order-dir") @Min(1) Sort.Direction orderDir) {
        return ResponseEntity.ok(adminService.searchUsers(page, perpage, orderDir, q));
    }


    @Operation(summary = "Update user's role")
    @PutMapping(path = "/users/{userId}/role")
    public void updateUserRole(@PathVariable long userId,
            @RequestParam(value = "role") SecurityGroup securityGroup) {

        adminService.updateUserRole(securityGroup, userId);
    }

    @Operation(summary = "Update user quota")
    @PutMapping(path = "/users/{userId}/quota")
    public ResponseEntity<Void> updateUserQuota(@PathVariable long userId, @RequestParam(value = "quota") Integer quota) {
        adminService.updateUserQuota(userId, quota);
        return ResponseEntity.ok().build();
    }

}
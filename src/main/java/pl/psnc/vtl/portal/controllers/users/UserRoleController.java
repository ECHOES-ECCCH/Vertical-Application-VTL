package pl.psnc.vtl.portal.controllers.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.model.users.UserRole;
import pl.psnc.vtl.portal.services.users.UserRoleService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @Operation(summary = "Retrieve user roles")
    @GetMapping(path = "/user-roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserRole>> getUserRoles() {
        return ResponseEntity.ok(userRoleService.getUserRoles());
    }

}

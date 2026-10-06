package pl.psnc.vtl.portal.controllers.transcriptions;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.model.transcriptions.ActorRole;
import pl.psnc.vtl.portal.services.transcriptions.ActorRoleService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ActorRoleController {

    private final ActorRoleService actorRoleService;


    @GetMapping(path = "/actor-roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ActorRole>> getActorRoles() {
        return ResponseEntity.ok(actorRoleService.getActorRoles());
    }

}

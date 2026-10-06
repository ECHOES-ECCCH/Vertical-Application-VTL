package pl.psnc.vtl.portal.controllers.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.services.users.UsersCommunicationMail;
import pl.psnc.vtl.portal.services.users.UsersCommunicationService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "UserCommunication", description = "establishing communication between users")
class UserCommunicationController {

    private final UsersCommunicationService usersCommunicationService;

    @Operation(summary = "send and email to another user")
    @PostMapping(path = "/communication/email", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> sendEmail(@RequestBody UsersCommunicationMail communicationMail, @RequestHeader("language") StaticFileLanguage language) {
        usersCommunicationService.sendEmail(communicationMail, language);
        return ResponseEntity.accepted().build();
    }
}

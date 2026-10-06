package pl.psnc.vtl.portal.controllers.domains;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.model.imports.FileType;
import pl.psnc.vtl.portal.services.domains.DomainService;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name= "Import transcription")
@RequiredArgsConstructor
public class DomainController {

    private final DomainService domainService;


    @Operation(summary = "method return list of domains that are supported during import of transcription."
            + "Objects from domains not listed in here will be rejected."
            + "List might be limited by providing a substring to be matched or file-type.")
    @GetMapping("/domains")
    public ResponseEntity<List<String>> getDomains(
            @RequestParam(value = "substring", required = false) String substring,
            @RequestParam(value = "file-type", required = false) FileType fileType)
            throws ConfigurationException {
        return ResponseEntity.ok(domainService.getDomains(substring, fileType));
    }
}

package pl.psnc.vtl.portal.controllers.transcriptions;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.model.transcriptions.BiblType;
import pl.psnc.vtl.portal.services.transcriptions.BiblTypeService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BiblTypeController {

    private final BiblTypeService biblTypeService;


    @GetMapping(path = "/bibl-types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BiblType>> getBiblTypes() {
        return ResponseEntity.ok(biblTypeService.getBiblTypes());
    }

}

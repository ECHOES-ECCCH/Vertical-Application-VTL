package pl.psnc.vtl.portal.controllers.properties;


import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.services.properties.PropertyService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping(path = "/info/survey-url", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> getSurveyURL() {
        return ResponseEntity.ok(propertyService.getSurveyURL());
    }

    @GetMapping(path = "/info/symbols", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<String>> getSymbols() {
        return ResponseEntity.ok(propertyService.getSymbols());
    }

}

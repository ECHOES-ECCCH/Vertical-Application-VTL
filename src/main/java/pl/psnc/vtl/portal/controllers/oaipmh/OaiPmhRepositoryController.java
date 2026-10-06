package pl.psnc.vtl.portal.controllers.oaipmh;

import com.lyncode.xml.exceptions.XmlWriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dspace.xoai.dataprovider.exceptions.OAIException;
import org.dspace.xoai.dataprovider.parameters.OAIRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.vtl.portal.services.oaipmh.OAIService;

import javax.xml.stream.XMLStreamException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/oai-pmh")
@RequiredArgsConstructor
public class OaiPmhRepositoryController {

    private final OAIService oaiService;

    @RequestMapping(
            value = "/oai-pmh-repository.xml",
            produces = MediaType.APPLICATION_XML_VALUE)
    public String handle(@RequestParam Map<String, String> allRequestParams) throws OAIException,
            XMLStreamException, XmlWriteException {
        Map<String, List<String>> listedMap = new HashMap<>();
        allRequestParams.forEach((key, value) -> listedMap.put(key, Collections.singletonList(value)));
        OAIRequest oaiRequest = new OAIRequest(listedMap);
        return oaiService.handle(oaiRequest);
    }
}

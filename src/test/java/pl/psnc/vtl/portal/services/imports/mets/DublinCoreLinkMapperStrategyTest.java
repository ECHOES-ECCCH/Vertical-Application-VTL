package pl.psnc.vtl.portal.services.imports.mets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.psnc.vtl.portal.ResourceFileContentLoader;
import pl.psnc.vtl.portal.services.imports.LoadedFile;
import pl.psnc.vtl.portal.services.imports.PublicationLinks;
import pl.psnc.vtl.portal.services.imports.Link;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;


public class DublinCoreLinkMapperStrategyTest {

    DublinCoreLinkMapperStrategy mapper = new DublinCoreLinkMapperStrategy();
    private final String ANALYTIC_DC = "METSLinks.xml";

    ObjectMapper objectMapper = new ObjectMapper();
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(4354);

    @Before
    public void init()
            throws Exception {
        objectMapper.registerModule( new JavaTimeModule());
    }

    @Test
    public void shouldMapAnalyticDublinCore(){
        String xmlString = ResourceFileContentLoader.loadFileContentAsString(ANALYTIC_DC);
        LoadedFile loadedFile = new LoadedFile(null, xmlString);
        PublicationLinks res = mapper.getTranscriptionLinks(loadedFile);
        List<String> links = res.getLinks().stream()
                .map(Link::getUrl)
                .collect(Collectors.toList());
        assertEquals(3, links.size());
        String link1 = "http://156.17.58.32/Content/77/Test3.jpg";
        String link2 = "http://156.17.58.32/Content/77/Test2.jpg";
        String link3 = "http://156.17.58.32/Content/77/Test1.jpg";
        assertEquals(link3, links.get(0));
        assertEquals(link2, links.get(1));
        assertEquals(link1, links.get(2));
    }
}
package pl.psnc.vtl.portal.services.imports.tei;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.vtl.portal.controllers.ErrorResponseMessage;
import pl.psnc.vtl.portal.controllers.RequestFieldsValidationException;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.TextFormatErrorMessages;

import static org.junit.Assert.*;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class TeiMapperStrategyITCase {

    TeiMapperStrategy mapper = new TeiMapperStrategy();

    private final String TEI_TEST_FILE1 = "test/tei/Man.5061.xml";
    private final String TEI_TEST_FILE2 = "test/tei/recipe_0_tei.xml";
    private final String TEI_TEST_FILE3 = "test/tei/Bo.17.xml";
    private final String TEI_TEST_FILE4 = "test/tei/Bo.18.xml";
    private final String TEI_TEST_FILE5 = "test/tei/Ch.504.Bo.17.xml";
    private final String TEI_TEST_FILE6 = "test/tei/Gn.254.xml";
    private final String TEI_TEST_FILE7 = "test/tei/Man.3.xml";
    private final String TEI_TEST_FILE8 = "test/tei/Man.4421.xml";
    private final String TEI_TEST_FILE9 = "test/tei/No.3.Jo.20.xml";
    private final String TEI_TEST_FILE10 = "test/tei/Pn.35.xml";
    private final String TEI_TEST_FILE11 = "test/tei/Pn.1648.xml";

    @SneakyThrows
    private String loadXMLFile(String fileName){
        return new String(getClass().getClassLoader().getResourceAsStream(fileName).readAllBytes());
    }

    @Test
    public void shouldMapTeiDocumentWithMonographInfo() throws Exception {
        String xmlString = loadXMLFile(TEI_TEST_FILE1);
        TranscriptionCore core = mapper.mapToImportTemplate(xmlString);
        log.info(core.toString());
        assertEquals(core.getMonographicInfo().getTitle(), "Modlitewnik");
        assertEquals(core.getMonographicInfo().getActors().size(), 2);
        assertEquals(core.getMonographicInfo().getActors().get(0).getRole().getCode(), "publisher" );
        assertEquals(core.getMonographicInfo().getActors().get(1).getRole().getCode(), "copyist" );
        assertEquals(core.getMonographicInfo().getActors().get(1).getName(), "Jeszua Josef Mordkowicz" );
        assertEquals(core.getMonographicInfo().getPublicationDate(), "Przed 1851");
        assertEquals(core.getMonographicInfo().getPublicationPlace(), "Halicz");

        //shouldn't preserve teiHeader and teiBody in first stage of import process
        assertNull(core.getTeiHeader());
        assertNull(core.getTeiBody());
        assertNotNull(core.getProfileDesc().getSummary());
    }

    @Test
    public void shouldMapTeiDocumentWithComplexPersonNames() throws Exception {
        String xmlString = loadXMLFile(TEI_TEST_FILE2);
        TranscriptionCore core = mapper.mapToImportTemplate(xmlString);
        log.info(core.toString());
        assertEquals(core.getMonographicInfo().getTitle(), "Königssuppe");
        assertEquals(core.getMonographicInfo().getActors().size(), 16);
    }

    @Test
    public void shouldMapAllTestTeiDocuments() throws Exception {
        String xmlString = loadXMLFile(TEI_TEST_FILE1);
        TranscriptionCore core = mapper.mapToImportTemplate(xmlString);
        log.info(core.toString());
        assertEquals(core.getMonographicInfo().getTitle(), "Modlitewnik");

        xmlString = loadXMLFile(TEI_TEST_FILE2);
        core = mapper.mapToImportTemplate(xmlString);
        log.info(core.toString());
        assertEquals(core.getMonographicInfo().getTitle(), "Königssuppe");
        assertEquals(core.getMonographicInfo().getActors().size(), 16);

        xmlString = loadXMLFile(TEI_TEST_FILE3);
        core = mapper.mapToImportTemplate(xmlString);
        assertEquals(core.getMonographicInfo().getTitle(), "Karaimi");
        assertEquals(core.getMonographicInfo().getPublicationPlace(), "Pieniężno");
        assertNotNull(core.getMonographicInfo().getPublisher());
        assertEquals(Integer.valueOf(104), core.getMonographicInfo().getPages());
        log.info(core.toString());

        xmlString = loadXMLFile(TEI_TEST_FILE4);
        core = mapper.mapToImportTemplate(xmlString);
        assertEquals(core.getMonographicInfo().getTitle(), "Karaimizm");
        assertEquals(core.getMonographicInfo().getPublicationDate(), "2005");
        assertEquals(core.getMonographicInfo().getPublicationPlace(), "Wrocław");
        assertEquals(core.getMonographicInfo().getIsbn(), "83-920068-3-6");
        assertEquals(core.getProfileDesc().getKeywords().size(), 1);
        assertEquals(core.getProfileDesc().getKeywords().get(0), "Historia");
        assertNotNull(core.getExternalId());
        log.info(core.toString());

        xmlString = loadXMLFile(TEI_TEST_FILE5);
        core = mapper.mapToImportTemplate(xmlString);
        assertEquals(core.getMonographicInfo().getTitle(), "Karaimi");
        assertEquals(core.getMonographicInfo().getPageRange(), "16-19");
        log.info(core.toString());

        xmlString = loadXMLFile(TEI_TEST_FILE6);
        core = mapper.mapToImportTemplate(xmlString);
        assertNotNull(core.getMonographicInfo().getTitle());
        log.info(core.toString());

        xmlString = loadXMLFile(TEI_TEST_FILE7);
        core = mapper.mapToImportTemplate(xmlString);
        assertEquals(core.getMonographicInfo().getTitle(), "Zbiór modlitw");
        log.info(core.toString());

        xmlString = loadXMLFile(TEI_TEST_FILE8);
        core = mapper.mapToImportTemplate(xmlString);
        assertEquals(core.getMonographicInfo().getPublicationPlace(), "Halicz");
        log.info(core.toString());

        xmlString = loadXMLFile(TEI_TEST_FILE9);
        core = mapper.mapToImportTemplate(xmlString);
        assertEquals(core.getMonographicInfo().getTitle(), "Awazymyz");
        log.info(core.toString());

        xmlString = loadXMLFile(TEI_TEST_FILE10);
        core = mapper.mapToImportTemplate(xmlString);
        assertNotNull(core.getMonographicInfo().getTitle());
        log.info(core.toString());

        xmlString = loadXMLFile(TEI_TEST_FILE11);
        core = mapper.mapToImportTemplate(xmlString);
        assertNotNull(core.getMonographicInfo().getTitle());
        assertEquals(core.getMonographicInfo().getPageRange(), "11-11");
        assertEquals(core.getMonographicInfo().getIssueNo(), Integer.valueOf(1648));
        log.info(core.toString());
    }

    @Test
    public void shouldThrowExceptionWhenMalformedXML(){
        String xmlString = "malformed";
        try{
            mapper.mapToImportTemplate(xmlString);
            fail();
        } catch (RequestFieldsValidationException e) {
            ErrorResponseMessage messages = e.getErrorResponseMessage();
            assertTrue(messages.getMessages().containsKey("resourceUrl"));
            assertEquals(TextFormatErrorMessages.MALFORMED.getErrorMessage(), messages.getMessages().get("resourceUrl"));
        }
    }
}

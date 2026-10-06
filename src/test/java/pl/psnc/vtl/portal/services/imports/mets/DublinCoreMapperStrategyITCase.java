package pl.psnc.vtl.portal.services.imports.mets;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.vtl.portal.ResourceFileContentLoader;
import pl.psnc.vtl.portal.controllers.ErrorResponseMessage;
import pl.psnc.vtl.portal.controllers.RequestFieldsValidationException;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.TextFormatErrorMessages;

import static org.junit.Assert.*;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class DublinCoreMapperStrategyITCase {

    DublinCoreMapperStrategy mapper = new DublinCoreMapperStrategy();
    private final String ANALYTIC_DC = "AnalyticDublinCore.xml";
    private final String NON_ANALYTIC_DC = "NotAnalyticDublinCore.xml";
    private final String MULTI_DC_TYPE = "MultiDcTypeDublinCore.xml";


    @Test
    public void shouldMapAnalyticDublinCore(){
        String xmlString = ResourceFileContentLoader.loadFileContentAsString(ANALYTIC_DC);
        TranscriptionCore core = mapper.mapToImportTemplate(xmlString);
        assertTrue(core.isAnalytic());
        assertEquals("title", core.getAnalyticInfo().getTitle());
        assertEquals(2, core.getAnalyticInfo().getActors().size());
        assertEquals("relationTitle", core.getMonographicInfo().getTitle());
        assertEquals( "publicationDate", core.getMonographicInfo().getPublicationDate());
        assertEquals("summary", core.getProfileDesc().getSummary());
        core.getMonographicInfo().setTitle("relationTitle");
        assertEquals(2, core.getProfileDesc().getKeywords().size() );
        assertEquals("iSbN text",core.getMonographicInfo().getIsbn());
        assertEquals("pol", core.getAnalyticInfo().getLanguage().getCode());
        core.getMonographicInfo().setPublisher("publisher");
    }

    @Test
    public void shouldMapNonAnalyticDublinCore(){
        String xmlString = ResourceFileContentLoader.loadFileContentAsString(NON_ANALYTIC_DC);
        TranscriptionCore core = mapper.mapToImportTemplate(xmlString);
        assertFalse(core.isAnalytic());
        assertEquals("title", core.getMonographicInfo().getTitle());
        assertEquals(2, core.getMonographicInfo().getActors().size());
        assertEquals( "publicationDate", core.getMonographicInfo().getPublicationDate());
        String complexSummary = "some" + System.lineSeparator() + "mary";
        assertEquals(complexSummary, core.getProfileDesc().getSummary());
        core.getMonographicInfo().setTitle("relationTitle");
        assertEquals(2, core.getProfileDesc().getKeywords().size() );
        assertEquals("iSbN text",core.getMonographicInfo().getIsbn());
        assertEquals("pol", core.getMonographicInfo().getLanguage().getCode());
        core.getMonographicInfo().setPublisher("publisher");
    }

    @Test
    public void shouldMapAnalyticDublinCoreWithMultipleDcType(){
        String xmlString = ResourceFileContentLoader.loadFileContentAsString(MULTI_DC_TYPE);
        TranscriptionCore core = mapper.mapToImportTemplate(xmlString);
        assertTrue(core.isAnalytic());
        assertEquals("title", core.getAnalyticInfo().getTitle());
        assertEquals(2, core.getAnalyticInfo().getActors().size());
        assertEquals("relationTitle", core.getMonographicInfo().getTitle());
        assertEquals( "publicationDate", core.getMonographicInfo().getPublicationDate());
        assertEquals("summary", core.getProfileDesc().getSummary());
        core.getMonographicInfo().setTitle("relationTitle");
        assertEquals(2, core.getProfileDesc().getKeywords().size() );
        assertEquals("EiSbN text",core.getMonographicInfo().getEisbn());
        assertEquals("pol", core.getAnalyticInfo().getLanguage().getCode());
        core.getMonographicInfo().setPublisher("publisher");
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
            Assert.assertEquals(TextFormatErrorMessages.MALFORMED.getErrorMessage(), messages.getMessages().get("resourceUrl"));
        }
    }

}
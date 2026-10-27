package pl.psnc.vtl.portal.controllers.statics;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.psnc.vtl.portal.controllers.PortalExceptionHandler;
import pl.psnc.vtl.portal.dto.statics.StaticFile;
import pl.psnc.vtl.portal.repositories.statics.StaticFileNotFoundException;
import pl.psnc.vtl.portal.services.statics.StaticFilesService;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class StaticFilesControllerTest {

    MockMvc mockMvc;

    @Mock
    StaticFilesService service;

    @InjectMocks
    StaticFilesController controller;


    @Before
    public void init() {
        FormattingConversionService formatting = new FormattingConversionService();
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).setConversionService(formatting)
                .setControllerAdvice(new StaticFilesHandler(), new PortalExceptionHandler()).build();
    }


    @Test
    public void SuccessfullGetHowToEngFileTest()
            throws Exception {
        StaticFile fileContent = new StaticFile("en", "ąćęłńóśźż", "äöüß");
        doReturn(fileContent).when(service).loadFile("regulations");
        mockMvc.perform(get("/api/static-files/{fileName}", "regulations"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pl", is("ąćęłńóśźż"))).andExpect(jsonPath("$.en", is("en")))
                .andExpect(status().isOk());
    }


    @Test
    public void FIleNotFoundExceptionTest()
            throws Exception {
        StaticFile fileContent = new StaticFile("en", "ąćęłńóśźż", "äöüß");
        String fileName = "name";
        doThrow(new StaticFileNotFoundException(fileName)).when(service).loadFile(any());
        mockMvc.perform(get("/api/static-files/{fileName}", "regulations"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }


    @Test
    public void ContollerAdviceExceptionTest()
            throws Exception {
        StaticFile fileContent = new StaticFile("en", "ąćęłńóśźż", "äöüß");
        doThrow(new RuntimeException()).when(service).loadFile(any());
        mockMvc.perform(get("/api/static-files/{fileName}", "regulations"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }
}
package pl.psnc.vtl.portal.services.statics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pl.psnc.vtl.portal.dto.statics.StaticFile;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.repositories.statics.StaticFilesRepository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class StaticFilesServiceTest {

    @Mock
    StaticFilesRepository repository;

    @InjectMocks
    StaticFilesService service;


    @Test
    public void loadFileContent() {
        String engFileContent = "en";
        String plFileContent = "pl";
        String fullStringPathEn = "contact" + "-" + StaticFileLanguage.EN + ".md";
        String fullStringPathPl = "contact" + "-" + StaticFileLanguage.PL + ".md";
        doReturn(engFileContent).when(repository).loadFileContent(fullStringPathEn);
        doReturn(plFileContent).when(repository).loadFileContent(fullStringPathPl);
        StaticFile staticFile = service.loadFile("contact");
        assertEquals(engFileContent, staticFile.getEn());
        assertEquals(plFileContent, staticFile.getPl());
    }
}
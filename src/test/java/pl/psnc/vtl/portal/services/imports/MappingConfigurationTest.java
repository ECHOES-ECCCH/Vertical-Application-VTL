package pl.psnc.vtl.portal.services.imports;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pl.psnc.vtl.portal.model.transcriptions.Language;
import pl.psnc.vtl.portal.repositories.properties.TranscriptionPropertiesRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.LanguageRepository;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class MappingConfigurationTest {

    @Mock
    TranscriptionPropertiesRepository mappingPropertyRepository;

    @Mock
    LanguageRepository languageRepository;

    @InjectMocks
    MappingConfiguration mappingConfiguration;


    @Before
    public void setUp(){
        Language eng = new Language("eng", 2, null, null, true);
        Language pol = new Language("pol", 1, null, null, true);
        doReturn(List.of(eng, pol)).when(languageRepository).findAll();
        doReturn(List.of("polski", "pl", "polish")).when(mappingPropertyRepository).getProperties("language-codes.pol");
        doReturn(List.of("angielski", "en", "english", "eng")).when(mappingPropertyRepository)
                .getProperties("language-codes.eng");
    }

    @Test
    public void getCodeForPolish() {
        assertEquals("pol", mappingConfiguration.getLanguageCode("polski"));
        assertEquals("pol", mappingConfiguration.getLanguageCode("pl"));
        assertEquals("pol", mappingConfiguration.getLanguageCode("polish"));
        assertEquals("pol", mappingConfiguration.getLanguageCode("polish"));
    }

    @Test
    public void getCodeForEnglish(){
        assertEquals("eng", mappingConfiguration.getLanguageCode("angielski"));
        assertEquals("eng", mappingConfiguration.getLanguageCode("en"));
        assertEquals("eng", mappingConfiguration.getLanguageCode("english"));
        assertEquals("eng", mappingConfiguration.getLanguageCode("eng"));
    }
}
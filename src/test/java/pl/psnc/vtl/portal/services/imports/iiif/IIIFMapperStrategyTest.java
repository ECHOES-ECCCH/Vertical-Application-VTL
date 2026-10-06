package pl.psnc.vtl.portal.services.imports.iiif;

import org.junit.Test;
import pl.psnc.vtl.portal.ResourceFileContentLoader;
import pl.psnc.vtl.portal.controllers.RequestFieldsValidationException;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;

import static org.junit.Assert.assertEquals;

public class IIIFMapperStrategyTest {

    IIIFMapperStrategy mapper = new IIIFMapperStrategy();
    private String MANIFEST = "Manifest.json";
    private String MANIFEST_WITH_ARRAYS = "ManifestWithArrays.json";
    private String TEI_FILE = "teiWithComplexStructure.xml";

    @Test
    public void shouldMapToTranscriptionCore() {

        String iiifString = ResourceFileContentLoader.loadFileContentAsString(MANIFEST);
        TranscriptionCore core = mapper.mapToImportTemplate(iiifString);
        assertEquals("Papillons", core.getAnalyticInfo().getTitle());
        assertEquals("http://localhost:3000/manifest.json", core.getExternalId());
        assertEquals("Four patterns inspired by butterflies.", core.getProfileDesc().getSummary());
    }


    @Test
    public void shouldMapToTranscriptionCoreWhenManifestContainsArrays() {

        String iiifString = ResourceFileContentLoader.loadFileContentAsString(MANIFEST_WITH_ARRAYS);
        TranscriptionCore core = mapper.mapToImportTemplate(iiifString);
        assertEquals("Papillons", core.getAnalyticInfo().getTitle());
        assertEquals("http://localhost:3000/manifest.json", core.getExternalId());
        assertEquals("Der nach der Bibel wichtigste Text", core.getProfileDesc().getSummary());
    }

    @Test(expected = RequestFieldsValidationException.class)
    public void shouldThrowExceptionWhenIncorrectFileProvided() {
        String teiString = ResourceFileContentLoader.loadFileContentAsString(TEI_FILE);
        mapper.mapToImportTemplate(teiString);
    }
}

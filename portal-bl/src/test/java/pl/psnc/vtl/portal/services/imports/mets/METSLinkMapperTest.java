package pl.psnc.vtl.portal.services.imports.mets;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import pl.psnc.vtl.portal.ResourceFileContentLoader;
import pl.psnc.vtl.portal.controllers.RequestFieldsValidationException;
import pl.psnc.vtl.portal.services.imports.LoadedFile;
import pl.psnc.vtl.portal.services.imports.PublicationLinks;
import pl.psnc.vtl.portal.services.imports.Link;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class METSLinkMapperTest {

    private final String METS_MULTIFORMAT_PDF = "metsMultiformatMainPDF.xml";
    private final String METS_MULTIFORMAT_TIF = "metsMultiformatMainTiff.xml";
    private final String METS_UNSUPPORTED_TYPES = "metsMultiformatUnsupportedTypes.xml";


    private final DublinCoreLinkMapperStrategy mapper = new DublinCoreLinkMapperStrategy();

    @Test
    public void shouldReturnPDFLinks() {

        String fileContent = ResourceFileContentLoader.loadFileContentAsString(METS_MULTIFORMAT_PDF);
        List<String> expectedLinks = List.of("https://bibliotekacyfrowa.eu/pdf1.pdf", "https://bibliotekacyfrowa.eu/pdf2.pdf", "https://bibliotekacyfrowa.eu/pdf3.pdf");
        LoadedFile dto = new LoadedFile("url", fileContent);
        PublicationLinks res = mapper.getTranscriptionLinks(dto);
        List<String> links = res.getLinks().stream()
                .map(Link::getUrl)
                .collect(Collectors.toList());
        Assert.assertEquals(expectedLinks.size(), links.size());
        Assert.assertEquals(expectedLinks, links);
    }

    @Test
    public void shouldReturnTifLinks() {
        String fileContent = ResourceFileContentLoader.loadFileContentAsString(METS_MULTIFORMAT_TIF);
        List<String> expectedLinks = List.of("https://bibliotekacyfrowa.eu/tif1.tif", "https://bibliotekacyfrowa.eu/tif2.tif");
        LoadedFile dto = new LoadedFile("url", fileContent);
        PublicationLinks res = mapper.getTranscriptionLinks(dto);
        List<String> links = res.getLinks().stream()
                .map(Link::getUrl)
                .collect(Collectors.toList());;
        Assert.assertEquals(expectedLinks.size(), links.size());
        Assert.assertEquals(expectedLinks, links);
    }

    @Test(expected = RequestFieldsValidationException.class)
    public void shouldThrowExceptionWhenNoSupportedTypeFound() {
        String fileContent = ResourceFileContentLoader.loadFileContentAsString(METS_UNSUPPORTED_TYPES);
        LoadedFile dto = new LoadedFile("url", fileContent);
        mapper.getTranscriptionLinks(dto);
    }

}


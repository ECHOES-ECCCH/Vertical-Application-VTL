package pl.psnc.vtl.portal.services.imports.tei;

import org.junit.Test;
import pl.psnc.vtl.portal.ResourceFileContentLoader;
import pl.psnc.vtl.portal.services.imports.LoadedFile;
import pl.psnc.vtl.portal.services.imports.PublicationLinks;
import pl.psnc.vtl.portal.services.imports.Link;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;


public class TeiLinkMapperStrategyTest {

    private final String TEI_TEST_FILE10 = "test/tei/Pn.35.xml";
    private final String TEI_TEST_FILE11 = "test/tei/Pn.1648.xml";

    TeiLinkMapperStrategy mapper = new TeiLinkMapperStrategy();

    @Test
    public void shouldMapTestCase1(){
        String xmlString = ResourceFileContentLoader.loadFileContentAsString(TEI_TEST_FILE10);
        LoadedFile loadedFile = new LoadedFile("http://test.manus.repository.pl", xmlString);
        PublicationLinks res = mapper.getTranscriptionLinks(loadedFile);
        List<String> links = res.getLinks().stream()
                .map(Link::getUrl)
                .collect(Collectors.toList());

        assertEquals(2, links.size());
        assertEquals("http://test.manus.repository.pl/image/Pn.35/35_1388_25v_PoZ1_4.tif", links.get(0));
        assertEquals("http://test.manus.repository.pl/image/Pn.35/35_1388_26_PoZ1_4.tif", links.get(1));
    }


    @Test
    public void shouldMapTestCase2(){
        String xmlString = ResourceFileContentLoader.loadFileContentAsString(TEI_TEST_FILE11);
        LoadedFile loadedFile = new LoadedFile("http://test.manus.repository.pl", xmlString);
        PublicationLinks res = mapper.getTranscriptionLinks(loadedFile);
        List<String> links = res.getLinks().stream()
                .map(Link::getUrl)
                .collect(Collectors.toList());

        assertEquals(1, links.size());
        assertEquals("http://test.manus.repository.pl/image/Pn.1648/1648_1444_11_PoZGr1_55.tif", links.get(0));
    }


}
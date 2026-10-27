package pl.psnc.vtl.portal.repositories.iiiftools;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import pl.psnc.vtl.portal.services.eBook.Ebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "vtl.iiif-server-url=http://iiif-server/")
public class IIIFToolsRepositoryITCase {

    @Autowired
    private IIIFToolsRepository repository;

    //if we do it this way we have to add "api/iiif/" in every request
    @Value("${vtl.iiif-server-url: http://localhost:5555/}")
    private String iiifServerUrl;

    @Value("${vtl.iiif-server-pct}")
    private float iiifServerPct;

    //direct call to iiifTools
    @Ignore
    @Test
    public void shouldReturnMap(){
        Long transcriptionId = 666L;
        String pdf = "http://www.africau.edu/images/default/sample.pdf";
        String tif = "https://homepages.cae.wisc.edu/~ece533/images/cameraman.tif";
        String jpg = "https://en.wikipedia.org/wiki/File:Example-serious.jpg";
        String xml = "https://jar-download.com/explore-java-source-code.php?a=mysql-connector-java&g=mysql&v=8.0.11&downloadable=1";
        List<String> links = List.of(tif,jpg, pdf, xml);
        Map<String, String> map = repository.saveTranscriptionFiles(transcriptionId, links, null).getUrlsToFilenames();
        log.info("     " + map.toString());
        assertTrue(map.containsKey(jpg));
        assertTrue(map.containsKey(tif));
        assertTrue(map.containsKey(pdf));
    }

    //direct call to iiifTools
    @Ignore
    @Test
    public void shouldScreamReturnEmptyMapWhenGoneWrong(){
        Long transcriptionId = 666L;
        String jpg2 = "http://156.17.58.32/Content/77/Test3.jpg";
        String jpg = "http://www.geonames.org/export/JSON-webservices.html";
        List<String> links = List.of(jpg2,jpg);
        Map<String, String> map = repository.saveTranscriptionFiles(transcriptionId, links, null).getUrlsToFilenames();
        assertTrue(map.isEmpty());
    }

    @Test
    public void shouldGeneratePdf()
            throws IOException {
        long transcriptionId = 1L;
        String hash = "someHash";

        String filePath = "src/test/resources/hocrWithComplexStructure.html";
        File originalFile = new File(filePath);
        String pageFileName = "Page0000.html";
        HashMap<String, String> mappingsJson = new HashMap<>();
        mappingsJson.put("Page0000.html", iiifServerUrl + "iipsrv/iipsrv.fcgi/?IIIF=t1/page249_Page0000.tif/full/pct:"
                                        + iiifServerPct + "/0/default.jpg");

        List<MultipartFile> files = new ArrayList<>();
        try (FileInputStream input = new FileInputStream(originalFile)) {
            MultipartFile multipartFile = new MockMultipartFile("file",
                    pageFileName, "text/plain", IOUtils.toByteArray(input));
            files.add(multipartFile);
        }
        catch (Exception e){
            log.error("Cannot create test HOCR file for multipart request.");
        }

        repository.saveEbook(transcriptionId, hash, files, "SomeTitle", "SomeAuthor",
                Ebook.PDF, mappingsJson, null);

    }

}
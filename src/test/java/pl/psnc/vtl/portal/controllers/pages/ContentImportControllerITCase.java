package pl.psnc.vtl.portal.controllers.pages;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.ResourceFileContentLoader;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@Transactional
public class ContentImportControllerITCase {


    @Autowired
    private VerseRepository verseRepository;

    @Autowired
    private PageRepository pageRepository;

    private static final String EMAIL = "email@wp.pl";
    private static final String PASSWORD = "myPassword";

    @Autowired
    private MockMvc mvc;
    private String TOKEN;

    private final static String HOCRS_ZIP = "multiHOCR.zip";
    private final static String ZIP = "zipWithUnrecognizablePageHOCR.zip";

    private final static String TEI_TEST_DIR = "test/tei/";
    private final static String TEI_ZIP = "teiPages.zip";
    private final static String TEI_TRANSCRIPTION2 = "teiTranscription2.xml";
    private final static String BAD_TEI_ZIP = "teiBadPage.zip";
    private final static String HOCR_WITH_MULTIPLE_PAGES = "hocrWithMultipePages.html";

    private MockMultipartFile file;
    private MockMultipartFile unrecognizableZip;
    private MockMultipartFile unsupportableFileType;
    private MockMultipartFile fileFineTeiZip;
    private MockMultipartFile fileBadTeiZip;
    private MockMultipartFile fileTranscription2TeiXml;
    private MockMultipartFile fileTranscriptionHocrHtml;


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
        byte[] bytes = ResourceFileContentLoader.class.getClassLoader().getResourceAsStream(HOCRS_ZIP).readAllBytes();
        file = new MockMultipartFile("file", HOCRS_ZIP, null, bytes);

        byte[] bytes2 = ResourceFileContentLoader.class.getClassLoader().getResourceAsStream(ZIP).readAllBytes();
        unrecognizableZip = new MockMultipartFile("file", ZIP, null, bytes2);

        byte[] bytes3 = ResourceFileContentLoader.class.getClassLoader().getResourceAsStream(ZIP).readAllBytes();
        unsupportableFileType = new MockMultipartFile("file", "blabla.tar", null, bytes2);

        byte[] bytesTeiPagesZip = ResourceFileContentLoader.class.getClassLoader().getResourceAsStream(TEI_TEST_DIR+ TEI_ZIP).readAllBytes();
        fileFineTeiZip = new MockMultipartFile("file", TEI_ZIP, null, bytesTeiPagesZip);

        byte[] bytesTeiBadPageZip = ResourceFileContentLoader.class.getClassLoader().getResourceAsStream(TEI_TEST_DIR + BAD_TEI_ZIP).readAllBytes();
        fileBadTeiZip = new MockMultipartFile("file", BAD_TEI_ZIP, null, bytesTeiBadPageZip);

        byte[] bytesTeiTranscription2 = ResourceFileContentLoader.class.getClassLoader().getResourceAsStream(TEI_TEST_DIR+ TEI_TRANSCRIPTION2).readAllBytes();
        fileTranscription2TeiXml = new MockMultipartFile("file", TEI_ZIP, null, bytesTeiTranscription2);

        byte[] bytesHocrTranscription = ResourceFileContentLoader.class.getClassLoader().getResourceAsStream(HOCR_WITH_MULTIPLE_PAGES).readAllBytes();
        fileTranscriptionHocrHtml = new MockMultipartFile("file", HOCR_WITH_MULTIPLE_PAGES, null, bytesHocrTranscription);

    }

    @Test
    @SneakyThrows
    public void shouldImportPagesFromZip(){
        long transcriptionId = 1L;

        mvc.perform(multipart("/api/transcription/{transcriptionId}/content", transcriptionId)
                .file(file)
                .header("Authorization", TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk());


        var page1 = new Page();
        page1.setId(1L);
        var page2 = new Page();
        page2.setId(2L);


        var page1Verses = verseRepository.findByPage(page1);
        var page2Verses = verseRepository.findByPage(page2);

        String verse1Content = "ala ma kota";
        String verse2Content = "ala żźęłóń kot";


        Assert.assertEquals(verse1Content, page1Verses.get(0).getContent());
        Assert.assertEquals(verse2Content, page2Verses.get(0).getContent());

    }

    @Test
    @SneakyThrows
    public void shouldInformAboutUnrecognizablePage(){
        long transcriptionId = 1L;
        mvc.perform(multipart("/api/transcription/{transcriptionId}/content", transcriptionId)
                .file(unrecognizableZip)
                .header("Authorization", TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code", is("import.error.unrecognizablePage")))
                .andExpect(jsonPath("params[0]", is("unrecognizablePagehOCR.html")));
    }

    @Test
    public void shouldInformAboutUnsupportableFileType() throws Exception {
        long transcriptionId = 1L;
        mvc.perform(multipart("/api/transcription/{transcriptionId}/content", transcriptionId)
                .file(unsupportableFileType)
                .header("Authorization", TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code", is("import.error.wrongFileProvided")));
    }


    @Test
    @SneakyThrows
    public void shouldImportPagesFromTeiZip(){
        long transcriptionId = 1L;

        String verse1Content = "Ala ma kota1";
        String verse2Content = "113874II/5a-b";
        String verse3Content = "249";
        String verse4Content = "Pisarz 14, ks. III r. 1427; pisarz 15, ks. IV r 1429 i pisarz 16, ks. IV r. 1430";

        mvc.perform(multipart("/api/transcription/{transcriptionId}/content", transcriptionId)
                .file(fileFineTeiZip)
                .header("Authorization", TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk());


        var page1 = new Page();
        page1.setId(1L);
        var page2 = new Page();
        page2.setId(2L);
        var page3 = new Page();
        page3.setId(3L);
        var page4 = new Page();
        page4.setId(4L);

        var page1Verses = verseRepository.findByPage(page1);
        var page2Verses = verseRepository.findByPage(page2);
        var page3Verses = verseRepository.findByPage(page3);
        var page4Verses = verseRepository.findByPage(page4);


        Assert.assertEquals(verse1Content, page1Verses.get(0).getContent());
        Assert.assertEquals(verse2Content, page2Verses.get(0).getContent());
        Assert.assertEquals(verse3Content, page3Verses.get(0).getContent());
        Assert.assertEquals(verse4Content, page4Verses.get(0).getContent());
    }

    @Test
    @SneakyThrows
    public void shouldImportPagesFromTeiXml(){
        long transcriptionId = 2L;

        String verse1Content = "Ala ma kota6";
        String verse2Content = "Ala ma kota9";

        mvc.perform(multipart("/api/transcription/{transcriptionId}/content", transcriptionId)
                .file(fileTranscription2TeiXml)
                .header("Authorization", TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk());


        var pages = pageRepository.findByTranscriptionId(transcriptionId);

        var page1Verses = verseRepository.findByPage(pages.get(0));
        var page2Verses = verseRepository.findByPage(pages.get(1));

        Assert.assertEquals(verse1Content, page1Verses.get(0).getContent());
        Assert.assertEquals(verse2Content, page2Verses.get(0).getContent());
    }

    @Test
    public void shouldImportPagesFromHocrHtml() throws Exception {
        long transcriptionId = 2L;

        mvc.perform(multipart("/api/transcription/{transcriptionId}/content", transcriptionId)
                .file(fileTranscriptionHocrHtml)
                .header("Authorization", TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk());


        var pages = pageRepository.findByTranscriptionIdOrderByPageNo(transcriptionId, Page.class);

        var page1Verses = verseRepository.findByPage(pages.get(0));
        var page2Verses = verseRepository.findByPage(pages.get(1));

        Assert.assertThat(page1Verses.size(), is(24));
        Assert.assertThat(page2Verses.size(), is(2));
        StringBuilder firstPageContent = new StringBuilder();
        page1Verses.forEach(verse-> firstPageContent.append(verse.getContent()));

        Assert.assertThat(firstPageContent.toString(), containsString("249"));
        Assert.assertThat(firstPageContent.toString(), containsString("Tako my bog pomofz"));
        Assert.assertThat(firstPageContent.toString(), containsString("jez sie w pr"));
        Assert.assertThat(firstPageContent.toString(), containsString("wstawit Miroslaw:"));
        Assert.assertThat(firstPageContent.toString(), containsString("jego stara"));
        Assert.assertThat(firstPageContent.toString(), containsString("część 2"));

        StringBuilder secondPageContent = new StringBuilder();
        page2Verses.forEach(verse-> secondPageContent.append(verse.getContent()));

        Assert.assertThat(secondPageContent.toString(), containsString("245"));
        Assert.assertThat(secondPageContent.toString(), not(containsString("249")));
        Assert.assertThat(secondPageContent.toString(), containsString("Pisarz"));
        Assert.assertThat(secondPageContent.toString(), containsString("III"));
        Assert.assertThat(secondPageContent.toString(), containsString("1427"));

    }

    @Test
    @SneakyThrows
    public void shouldInformAboutUnrecognizableTeiPage(){
        long transcriptionId = 1L;
        mvc.perform(multipart("/api/transcription/{transcriptionId}/content", transcriptionId)
                .file(fileBadTeiZip)
                .header("Authorization", TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code", is("import.error.unrecognizablePage")))
                .andExpect(jsonPath("params[0]", is("teiBadPage.xml")));
    }
}
package pl.psnc.vtl.portal.controllers.pages;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.ResourceFileContentLoader;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.verses.OcrMode;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Transactional
public class PageControllerITCase {

    private static final String EMAIL = "email@wp.pl";
    private static final String PASSWORD = "myPassword";
    ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mvc;
    private String TOKEN;

    @Autowired
    private VerseRepository verseRepository;

    @Autowired
    private PageRepository pageRepository;


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
    }


    @Test
    public void shouldReturnPageForAnonymous()
            throws Exception {
        Long transcriptionId = 1L;
        Long pageId = 1L;

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, pageId)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("path", is("page0004r.tif"))).andExpect(jsonPath("pageNo", is(1)))
                .andExpect(jsonPath("boundingBox.upperLeftX", is(0)))
                .andExpect(jsonPath("boundingBox.upperLeftY", is(0)))
                .andExpect(jsonPath("boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("viewMode", is("read-only")));
    }


    @Test
    public void shouldReturnPageForAdmin()
            throws Exception {
        Long transcriptionId = 1L;
        Long pageId = 1L;

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, pageId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("path", is("page0004r.tif"))).andExpect(jsonPath("pageNo", is(1)))
                .andExpect(jsonPath("boundingBox.upperLeftX", is(0)))
                .andExpect(jsonPath("boundingBox.upperLeftY", is(0)))
                .andExpect(jsonPath("boundingBox.lowerRightX", is(2595)))
                .andExpect(jsonPath("boundingBox.lowerRightY", is(3759)))
                .andExpect(jsonPath("viewMode", is("admin")));
    }


    @Test
    public void shouldntReturnNonExistentPage()
            throws Exception {
        Long transcriptionId = 1L;
        Long pageId = 999L;

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, pageId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }


    @Ignore
    @Test
    public void shouldRecognizePage()
            throws Exception {
        //given
        Long transcriptionId = 2L;
        Long pageId = 5L;
        List<Verse> givenVerses = initVerses();

        //when
        MvcResult mvcResult = mvc.perform(put("/api/transcriptions/{transcriptionId}/pages/{pageId}/recognize", transcriptionId, pageId)
                .param("lang", "pol")
                .param("ocrMode", String.valueOf(OcrMode.OVERRIDE))
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Verse> versesFromController = objectMapper.readValue(contentAsString, new TypeReference<List<Verse>>() {

        });

        //then
        assertThat(versesFromController.size(), is(givenVerses.size()));
        for (int i = 0; i < givenVerses.size(); i++) {
            assertThat(versesFromController.get(i).getContent(), is(givenVerses.get(i).getContent()));
            assertThat(versesFromController.get(i).getBoundingBox(), is(givenVerses.get(i).getBoundingBox()));
        }
        assertThat(versesFromController.get(0).getContent(), not(givenVerses.get(10).getContent()));
        assertThat(versesFromController.get(0).getBoundingBox(), not(givenVerses.get(10).getBoundingBox()));
    }


    @Ignore
    @Test
    public void shouldRecognizePageVersesWithoutContent()
            throws Exception {
        //given
        Long transcriptionId = 2L;
        Long pageId = 5L;

        //when
        MvcResult mvcResult = mvc.perform(put("/api/transcriptions/{transcriptionId}/pages/{pageId}/recognize", transcriptionId, pageId)
                .param("lang", "eng")
                .param("ocrMode", String.valueOf(OcrMode.UPDATE))
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Verse> versesFromController = objectMapper.readValue(contentAsString, new TypeReference<List<Verse>>() {});
        assertThat(versesFromController.get(4).getContent(), containsString("J. E."));
        assertThat(versesFromController.get(5).getContent(), containsString("powodu"));
        assertThat(versesFromController.get(6).getContent(), containsString("BOGU"));
        assertThat(versesFromController.get(7).getContent(), containsString("Boga Wszechmocnego"));
    }


    @Test
    public void shouldThrowExceptionForIncorrectPageFormat() throws Exception {
        Long pageId = 12L;

        mvc.perform(
                get("/api/pages/{pageId}/content", pageId)
                        .param("type", "pageFormatThatDoesNotExist")
                        .header("Authorization", TOKEN))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void shouldReturnPageContentAsPlainText() throws Exception {

        Long pageId = 1L;

        MvcResult result = mvc.perform(
                get("/api/pages/{pageId}/content", pageId)
                        .param("type", "plain_text")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("Ala ma kota1"));
        assertThat(content, containsString("Ala ma kota2"));
        assertThat(content, containsString("Ala ma kota3"));
        assertThat(content, containsString("Ala ma kota4"));
        assertThat(content, containsString("Ala ma kota5"));
    }


    @Test
    public void shouldReturnPageContentAsPlainText_1() throws Exception {
        Long pageId = 2L;

        MvcResult result = mvc.perform(
                get("/api/pages/{pageId}/content", pageId)
                        .param("type", "plain_text")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content, containsString("Redaktor wydawnictwa Anna Kosmulska"));
        assertThat(content, containsString("Redaktor naczelny Alicja Januszczak"));
        assertThat(content, containsString("© Copyright by Zakład Narodowy im. Ossoliskich - Wydawnictwo, Wrocław 1981."));
        assertThat(content, containsString("ISBN 83-04-00300-7 całość ISBN 83-04-00489-5"));
        assertThat(content, containsString("Zakład Narodowy im. Ossolińskich - Wydawnicwto Wrocław 1981."));
        assertThat(content, containsString("Wrocławska Drukarnia Naukowa. Zam. 2064/80 - B3 - cena zł 130. "));
    }


    @Test
    public void shouldReturnPageContentAsPlainText_2() throws Exception {

        Long pageId = 3L;

        MvcResult result = mvc.perform(
                get("/api/pages/{pageId}/content", pageId)
                        .param("type", "plain_text")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();

        assertThat(content, containsString("Thomaislai tests Merczinkowsky: [[Ofzep de Regowo advocatus]] {primus Derslaus de Oczweka}, secundus Nicolaus de Oczwÿke, tercius Thurinskÿ Derslaus, quartus Mirzantha Caczkowsky, quintus Sbiluth Kaczkowskÿ"));
        assertThat(content, containsString("318. (89v) - Paxima rotha Miroslaii testibus contra Paxino Stanislaum:"));
        assertThat(content, containsString("318a. (89v) - (IV 37 r. ca 1430 –  Item &lt;...&gt; nobilis N&lt;...&gt;sky debet i&lt;urare&gt; Nobilis et S&lt;...&gt;de Creszcz ad proximus termonos &lt;particulares&gt; tali rotha:"));
        assertThat(content, containsString("319. (90) – Testes. Miroslaw de Paxino erga Taxinszky (!) Stanoslaun Mathiam. Testes: Mathias Zoleczsky, secundus Paulus Zoleczszky, tercius"));
    }


    @Test
    public void shouldReturnPageContentAsHOCR_1() throws Exception {

        long pageId = 4L;

        MvcResult result = mvc.perform(
                get("/api/pages/{pageId}/content", pageId)
                        .param("type", "hocr")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();

        assertThat(content, containsString("<span class=\"ocr_line\" id=\"line_4_6\" title=\"bbox 1000 700 1800 800\">Tako mi pomoży Bog i święty</span>"));
        assertThat(content, containsString("<span class=\"ocr_line\" id=\"line_4_7\" title=\"bbox 1000 810 1800 910\">krzyż, jako to świadczymy, jako</span>"));
        assertThat(content, containsString("<h2 class=\"ocr_section\" id=\"sec_4_56\" title=\"bbox 150 1700 2000 1800\">"));
        assertThat(content, containsString("<table class=\"ocr_table\" id=\"table_4_45\" title=\"bbox 200 700 1800 1370\">"));
    }


    @Test
    public void shouldReturnPageContentAsTei() throws Exception {

        Long pageId = 3L;

        MvcResult result = mvc.perform(
                get("/api/pages/{pageId}/content", pageId)
                        .param("type", "tei")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();

        assertThat(content, containsString("<surface id=\"page_3\" ulx=\"0\" uly=\"0\" lrx=\"2595\" lry=\"3759\">"));
        assertThat(content, containsString("<line ulx=\"50\" uly=\"1180\" lrx=\"421\" lry=\"1280\">tali rotha:</line>"));
        assertThat(content, containsString("<zone ulx=\"50\" uly=\"1420\" lrx=\"2400\" lry=\"1520\" type=\"p\">"));
    }


    @Test
    public void shouldNotAddUnrecognizedTextLayerToThePage()
            throws Exception {
        //given
        Long pageId = 1L;

        //when
        mvc.perform(
                MockMvcRequestBuilders
                        .multipart("/api/pages/{pageId}/content",pageId)
                        .file("file", "fake_file_without_meanningfull_content".getBytes()).characterEncoding("UTF-8")
                        .header("Authorization", TOKEN))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("code", is("import.error.wrongFileProvided")));

        //then
        Optional<Page> page = pageRepository.findById(pageId);
        List<Verse> verses = verseRepository.findByPageOrderByVerseNoAsc(page.get());

        assertThat(verses.size(), is(0));
    }


    @Test
    public void shouldAddTextLayerToThePage()
            throws Exception {
        //given
        Long pageId = 1L;

        byte[] ba = getClass().getClassLoader().getResourceAsStream("hocrWithParagraphsOnly.html").readAllBytes();
        MockMultipartFile multipartFile = new MockMultipartFile("file", "hocrWithParagraphsOnly.html", "text/html", ba);

        //when
        mvc.perform(
                MockMvcRequestBuilders
                        .multipart("/api/pages/{pageId}/content", pageId)
                        .file(multipartFile)
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk());

        //then
        Optional<Page> page = pageRepository.findById(pageId);
        List<Verse> verses = verseRepository.findByPageOrderByVerseNoAsc(page.get());

        assertThat(verses.size(), is(24));

        for (int i = 0; i < verses.size(); i++) {
            assertThat(verses.get(i).getVerseNo(), is(i + 1));
        }

        assertThat(verses.get(0).getContent(), containsString("249 Pisarz 14, ks. III r. 1427"));
        assertThat(verses.get(5).getContent(), containsString("Zapiska lacinska w rkp. po nastepnej rocie, odnoszqcej sie do inne) sprawy."));
        assertThat(verses.get(10).getContent(), containsString("wo witawil miroflaw maczka | fta-"));
        assertThat(verses.get(17).getContent(), containsString("wstawit Miroslaw: Ma¢ka, Stanis-"));
        assertThat(verses.get(23).getContent(), containsString(""));
    }


    @Test
    public void shouldAddTextLayerToThePageFromTei()
            throws Exception {
        //given
        Long pageId = 1L;

        byte[] ba = getClass().getClassLoader().getResourceAsStream("test/tei/teiTranscription3.xml").readAllBytes();
        MockMultipartFile multipartFile = new MockMultipartFile("file", "teiTranscription3.xml", "application/xml", ba);

        //when
        mvc.perform(
                MockMvcRequestBuilders
                        .multipart("/api/pages/{pageId}/content", pageId)
                        .file(multipartFile)
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk());

        //then
        Optional<Page> page = pageRepository.findById(pageId);
        List<Verse> verses = verseRepository.findByPageOrderByVerseNoAsc(page.get());

        assertThat(verses.size(),is(42));

        for (int i = 0; i < verses.size(); i++) {
            assertThat(verses.get(i).getVerseNo(), is(i + 1));
        }

        assertThat(verses.get(0).getContent(), containsString("113874II/5a-b"));
        assertThat(verses.get(5).getContent(), containsString("ISBN 83-04-00489-5"));
        assertThat(verses.get(10).getContent(), containsString("Wrocławska Drukarnia Naukowa. Zam. 2064/80 - B3 - cena zł 130. -"));
        assertThat(verses.get(17).getContent(), containsString("318. (89v) - Paxima rotha Miroslaii testibus contra Paxino Stani-"));
        assertThat(verses.get(41).getContent(), containsString("nej sprawy."));
    }


    private List<Verse> initVerses() {
        BoundingBox boundingBox1 = new BoundingBox(633, 414, 1535, 417);
        BoundingBox boundingBox2 = new BoundingBox(737, 670, 1729, 728);
        BoundingBox boundingBox3 = new BoundingBox(393, 806, 2054, 863);
        BoundingBox boundingBox4 = new BoundingBox(403, 905, 2057, 976);
        BoundingBox boundingBox5 = new BoundingBox(631, 1076, 1843, 1125);
        BoundingBox boundingBox6 = new BoundingBox(482, 1202, 2130, 1259);
        BoundingBox boundingBox7 = new BoundingBox(312, 1276, 2132, 1335);
        BoundingBox boundingBox8 = new BoundingBox(479, 1349, 2132, 1408);
        BoundingBox boundingBox9 = new BoundingBox(309, 1425, 2132, 1482);
        BoundingBox boundingBox10 = new BoundingBox(477, 1499, 2133, 1558);
        BoundingBox boundingBox11 = new BoundingBox(310, 1573, 2132, 1632);
        BoundingBox boundingBox12 = new BoundingBox(320, 1647, 2132, 1706);
        BoundingBox boundingBox13 = new BoundingBox(321, 1722, 1874, 1779);
        BoundingBox boundingBox14 = new BoundingBox(474, 1794, 2132, 1854);
        BoundingBox boundingBox15 = new BoundingBox(309, 1868, 2127, 1929);
        BoundingBox boundingBox16 = new BoundingBox(306, 1943, 2130, 2002);
        BoundingBox boundingBox17 = new BoundingBox(322, 2018, 2128, 2078);
        BoundingBox boundingBox18 = new BoundingBox(321, 2093, 2131, 2151);
        BoundingBox boundingBox19 = new BoundingBox(325, 2166, 2130, 2227);
        BoundingBox boundingBox20 = new BoundingBox(322, 2241, 2127, 2301);
        BoundingBox boundingBox21 = new BoundingBox(306, 2315, 2127, 2375);
        BoundingBox boundingBox22 = new BoundingBox(320, 2389, 2001, 2445);
        BoundingBox boundingBox23 = new BoundingBox(477, 2464, 2126, 2522);
        BoundingBox boundingBox24 = new BoundingBox(316, 2537, 2127, 2595);
        BoundingBox boundingBox25 = new BoundingBox(323, 2613, 2129, 2670);
        BoundingBox boundingBox26 = new BoundingBox(324, 2686, 2128, 2747);
        BoundingBox boundingBox27 = new BoundingBox(309, 2760, 2129, 2817);
        BoundingBox boundingBox28 = new BoundingBox(326, 2835, 2127, 2893);
        BoundingBox boundingBox29 = new BoundingBox(304, 2913, 1037, 2968);
        BoundingBox boundingBox30 = new BoundingBox(478, 2981, 2128, 3039);
        BoundingBox boundingBox31 = new BoundingBox(325, 3056, 2126, 3117);
        BoundingBox boundingBox32 = new BoundingBox(309, 3129, 2124, 3190);
        BoundingBox boundingBox33 = new BoundingBox(323, 3204, 2123, 3265);
        BoundingBox boundingBox34 = new BoundingBox(316, 3277, 2127, 3339);
        BoundingBox boundingBox35 = new BoundingBox(313, 3352, 2024, 3412);
        BoundingBox boundingBox36 = new BoundingBox(2119, 3769, 2633, 3888);
        Verse verse1 = new Verse();
        Verse verse2 = new Verse();
        Verse verse3 = new Verse();
        Verse verse4 = new Verse();
        Verse verse5 = new Verse();
        Verse verse6 = new Verse();
        Verse verse7 = new Verse();
        Verse verse8 = new Verse();
        Verse verse9 = new Verse();
        Verse verse10 = new Verse();
        Verse verse11 = new Verse();
        Verse verse12 = new Verse();
        Verse verse13 = new Verse();
        Verse verse14 = new Verse();
        Verse verse15 = new Verse();
        Verse verse16 = new Verse();
        Verse verse17 = new Verse();
        Verse verse18 = new Verse();
        Verse verse19 = new Verse();
        Verse verse20 = new Verse();
        Verse verse21 = new Verse();
        Verse verse22 = new Verse();
        Verse verse23 = new Verse();
        Verse verse24 = new Verse();
        Verse verse25 = new Verse();
        Verse verse26 = new Verse();
        Verse verse27 = new Verse();
        Verse verse28 = new Verse();
        Verse verse29 = new Verse();
        Verse verse30 = new Verse();
        Verse verse31 = new Verse();
        Verse verse32 = new Verse();
        Verse verse33 = new Verse();
        Verse verse34 = new Verse();
        Verse verse35 = new Verse();
        Verse verse36 = new Verse();
        verse1.setContent(" ");
        verse2.setContent("LIST PASTERSKI");
        verse3.setContent("J. E. Hachana Karaimow w Polsce do rodakow");
        verse4.setContent("z powodu 10-lecia Odrodzenia Rzeczypospolitej.");
        verse5.setContent("W BOGU UKOCHANI BRACIA | SIOSTRY!");
        verse6.setContent("Niech Bog Najwyzszy biogostawi Was i zachowa, i niech");
        verse7.setContent("‘sie pomnozy taska i spokéj w Was od Boga Wszechmocnego.");
        verse8.setContent("W dniu dzisiejszym Polska cata obchodzi swe wielkie");
        verse9.setContent("Swieto — dziesigta rocznice wskrzeszenia Panstwowosci Polskiej.");
        verse10.setContent("Z gorq_ sto trzydziescie lat temu, trzy panstwa_ sasied-");
        verse11.setContent("nie, korzystajgc z tymczasowego kryzysu wewnetrznego w kraju,");
        verse12.setContent("zdradliwie przylaczyly do swoich wtosci odwieczne ziemie pol-");
        verse13.setContent("skie i ludnos¢ ich kordonami podzielity na trzy czesci.");
        verse14.setContent("Szlachetny, wolnosé milujacy Naréd Polski nigdy nie mdgt");
        verse15.setContent("‘pogodzié sie z takim nieslychanym, bezprzyktadnym w dziejach");
        verse16.setContent("-$Swiata gwattem i w ciagu tych stu trzydziestu lat wielokrotnie");
        verse17.setContent("zrywat sie do walki o wolnos¢c. Nie majac oreza w reku,");
        verse18.setContent("zlewajac sie i broczac krwia, nardd ten czasowo ustepowat");
        verse19.setContent("przed gwalcicielami, z tem jednak, by w lepszej dobie powsta¢");
        verse20.setContent("ponownie. Dziesiatki, setki tysiecy szlachetnych synow Polski");
        verse21.setContent("ztozyto kosci swe na polach walki, tysiqgcami szli oni na Smieré¢,");
        verse22.setContent("do wiezien, do twierdz, na zatracenie, w tajgi syberyjskie...");
        verse23.setContent("Panstwa sasiednie, rozszarpawszy na czesci zywy organizm");
        verse24.setContent("Polski, nie moglty jednak ztamac i pognebi¢ ducha polskiego.");
        verse25.setContent("Nardéd o kulturze wielowiekowej, nie ustepujace], a moze i prze-");
        verse26.setContent("wyzszajacej kulture swych sasiadow, nardd, ktdéry wydal ze sie-");
        verse27.setContent("ebie Mickiewicza i Kosciuszke, nardédd, ktdéry dat swiatu Koper-");
        verse28.setContent("nika, Sienkiewicza, Matejke, Szopena... nie mdgt by¢ zgnebiony");
        verse29.setContent("-duchowo przez zaborcow.");
        verse30.setContent("Nastat rok 1918, zaswitat dzien 11. listopada, przez");
        verse31.setContent("Boga zeslany, i wybila godzina wolnosci. Prastare ziemice pol-");
        verse32.setContent("skie znéw wrdécily do odwiecznych wtodarzy swoich i nad niemi");
        verse33.setContent("znoéw szybuje Bialy Orzet Polski, nad gmachami panstwowemi");
        verse34.setContent("odrodzonej Rzeczypospolitej znowu powiewa dwubarwna, bialo-");
        verse35.setContent("amarantowa choragiew narodowa, aby nie opas¢ juz nigdy.");
        verse36.setContent("[2] JAZYSZEAR");
        verse1.setBoundingBox(boundingBox1);
        verse2.setBoundingBox(boundingBox2);
        verse3.setBoundingBox(boundingBox3);
        verse4.setBoundingBox(boundingBox4);
        verse5.setBoundingBox(boundingBox5);
        verse6.setBoundingBox(boundingBox6);
        verse7.setBoundingBox(boundingBox7);
        verse8.setBoundingBox(boundingBox8);
        verse9.setBoundingBox(boundingBox9);
        verse10.setBoundingBox(boundingBox10);
        verse11.setBoundingBox(boundingBox11);
        verse12.setBoundingBox(boundingBox12);
        verse13.setBoundingBox(boundingBox13);
        verse14.setBoundingBox(boundingBox14);
        verse15.setBoundingBox(boundingBox15);
        verse16.setBoundingBox(boundingBox16);
        verse17.setBoundingBox(boundingBox17);
        verse18.setBoundingBox(boundingBox18);
        verse19.setBoundingBox(boundingBox19);
        verse20.setBoundingBox(boundingBox20);
        verse21.setBoundingBox(boundingBox21);
        verse22.setBoundingBox(boundingBox22);
        verse23.setBoundingBox(boundingBox23);
        verse24.setBoundingBox(boundingBox24);
        verse25.setBoundingBox(boundingBox25);
        verse26.setBoundingBox(boundingBox26);
        verse27.setBoundingBox(boundingBox27);
        verse28.setBoundingBox(boundingBox28);
        verse29.setBoundingBox(boundingBox29);
        verse30.setBoundingBox(boundingBox30);
        verse31.setBoundingBox(boundingBox31);
        verse32.setBoundingBox(boundingBox32);
        verse33.setBoundingBox(boundingBox33);
        verse34.setBoundingBox(boundingBox34);
        verse35.setBoundingBox(boundingBox35);
        verse36.setBoundingBox(boundingBox36);

        List<Verse> givenVerses = new ArrayList<>();
        givenVerses.add(verse1);
        givenVerses.add(verse2);
        givenVerses.add(verse3);
        givenVerses.add(verse4);
        givenVerses.add(verse5);
        givenVerses.add(verse6);
        givenVerses.add(verse7);
        givenVerses.add(verse8);
        givenVerses.add(verse9);
        givenVerses.add(verse10);
        givenVerses.add(verse11);
        givenVerses.add(verse12);
        givenVerses.add(verse13);
        givenVerses.add(verse14);
        givenVerses.add(verse15);
        givenVerses.add(verse16);
        givenVerses.add(verse17);
        givenVerses.add(verse18);
        givenVerses.add(verse19);
        givenVerses.add(verse20);
        givenVerses.add(verse21);
        givenVerses.add(verse22);
        givenVerses.add(verse23);
        givenVerses.add(verse24);
        givenVerses.add(verse25);
        givenVerses.add(verse26);
        givenVerses.add(verse27);
        givenVerses.add(verse28);
        givenVerses.add(verse29);
        givenVerses.add(verse30);
        givenVerses.add(verse31);
        givenVerses.add(verse32);
        givenVerses.add(verse33);
        givenVerses.add(verse34);
        givenVerses.add(verse35);
        givenVerses.add(verse36);
        return givenVerses;
    }


    @Test
    public void shouldNotMarkPageAsEmptyWhenHasVerses()
            throws Exception {

        Long pageId = 1L;
        Long transcriptionId = 1L;

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, pageId)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("empty", is(false)));

        mvc.perform(put("/api/pages/{pageId}/empty", pageId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }


    @Test
    public void shouldMarkPageAsEmpty()
            throws Exception {
        Long transcriptionId = 1L;

        byte[] ba = ResourceFileContentLoader.loadFileContentAsBytes("test1Pdf.pdf");
        MockMultipartFile file1 = new MockMultipartFile("file", "filename11c.pdf", "text/plain", ba);

        MvcResult response = mvc.perform(MockMvcRequestBuilders.multipart("/api/transcriptions/{transcriptionId}/add-pages", transcriptionId)
                .file(file1).header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("pages.list", hasSize(5))).andReturn();

        JSONArray pagesJSONArray = JsonPath.read(response.getResponse().getContentAsString(), "pages.list");

        List<Long> ids = new ArrayList<>();
        for (Object o : pagesJSONArray) {
            LinkedHashMap pageMap = (LinkedHashMap) o;
            ids.add(Long.valueOf(pageMap.get("id").toString()));
        }

        long pageId = Collections.max(ids);

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, pageId)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("empty", is(false)));

        mvc.perform(put("/api/pages/{pageId}/empty", pageId).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, pageId)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("empty", is(true)));

    }

}

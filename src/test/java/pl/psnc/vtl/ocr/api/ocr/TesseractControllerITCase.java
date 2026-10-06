package pl.psnc.vtl.ocr.api.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.psnc.vtl.ocr.api.ocr.tesseract.VerseBboxesDto;
import pl.psnc.vtl.ocr.api.ocr.tesseract.OcrBatchResponse;
import pl.psnc.vtl.ocr.api.ocr.tesseract.VerseBboxDto;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.psnc.vtl.ocr.matcher.TextContentMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
public class TesseractControllerITCase {

    @Autowired
    private MockMvc mvc;


    @Test
    public void shouldRecognizeTiff() throws Exception {
        final int transcriptionId = 1;
        final String filename = "page0249.tif";

        String response =
                mvc.perform(
                        get("/api/ocr/{transcriptionId}/recognize-hocr/{filename}", transcriptionId, filename)
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Document hocr = Jsoup.parse(response);

        assertEquals(hocr.body().getElementsByClass("ocr_page").size(), 1);
        assertEquals(hocr.body().getElementsByClass("ocr_carea").size(), 1);
        assertThat(hocr.body().getElementsByClass("ocr_par").size(), greaterThan(5));
        assertThat(hocr.body().getElementsByClass("ocr_line").size(), greaterThan(15));
        assertThat(hocr.body().getElementsByClass("ocrx_word").size(), greaterThan(130)); // actually there are around 135
    }

    @Test
    public void shouldNotRecognizeNonExistentImage() throws Exception {
        final int transcriptionId = 1;
        final String filename = "non-existent.tif";

        mvc.perform(
                get("/api/ocr/{transcriptionId}/recognize-hocr/{filename}", transcriptionId, filename)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void shouldRecognizeSubImageBatchTextProperly() throws Exception {
        final int transcriptionId = 1;
        final String filename = "page0249.tif";

        final List<VerseBboxDto> verses = Arrays.asList(
                new VerseBboxDto(1L, 150, 260, 180, 260),
                new VerseBboxDto(2L, 1500, 2200, 190, 250),
                new VerseBboxDto(3L, 265, 1935, 590, 650),
                new VerseBboxDto(4L, 250, 2160, 765, 834),
                new VerseBboxDto(5L, 1200, 2160, 960, 1020)
        );

        final VerseBboxesDto batchRequest = new VerseBboxesDto(verses);
        final String requestBody = new ObjectMapper().writeValueAsString(batchRequest);

        String responseBody =
                mvc.perform(
                        post("/api/ocr/{transcriptionId}/recognize-subimg-batch/{filename}?lang=pol", transcriptionId, filename)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                ).andReturn().getResponse().getContentAsString();

        OcrBatchResponse response = new ObjectMapper().readValue(responseBody, OcrBatchResponse.class);
        Map<Long, String> content = response.getVersesContent();

        assertEquals(5, content.size());
        assertThat(content.get(1L), fuzzyMatch("249"));
        assertThat(content.get(2L), fuzzyMatch("Pisarz 14, ks. III r. 1427"));
        assertThat(content.get(3L), fuzzyMatch("Zapiska łacińska w rkp. po następnej rocie, odnoszącej się do innej sprawy."));
        assertThat(content.get(4L), fuzzyMatch("318. (89v) — Paxina rotha Miroslaii testibus contra Paxino Stani"));
        assertThat(content.get(5L), fuzzyMatch("Tako mi Bog pomoż i święty krzyż,"));
    }

    @Test
    public void shouldRecognizeImage()
            throws Exception {

        final long transcriptionId = 2;
        String filename = "Image5.jpg";
        String responseBody = mvc.perform(get("/api/ocr/{transcriptionId}/recognize-subimg/{filename}?x1=0&y1=0&x2=3000&y2=3000&lang=pol",
                transcriptionId, filename).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(responseBody, fuzzyMatch("LIST PASTERSKI J. E. Hachana Karaimów w Polsce do rodaków z powodu 10-lecia Odrodzenia Rzeczypospolitej. W BOGU UKOCHANI BRACIA I SIOSTRY! Niech Bóg Najwyższy błogosławi Was i zachowa, i niech się pomnoży łaska i spokój w Was od Boga Wszechmocnego. W dniu dzisiejszym Polska cała obchodzi swe wielkie święto — dziesiątą rocznicę wskrzeszenia Państwowości Polskiej. Z górą sto trzydzieście lat temu, trzy państwa sąsiednie, korzystając z tymczasowego kryzysu wewnętrznego w kraju, zdradliwie przyłączyły do swoich włości odwieczne ziemie polskie i ludność ich kordonami podzieliły na trzy części. Szlachetny, wolność miłujący Naród Polski nigdy nie mógł pogodzić się z takim niesłychanym, bezprzykładnym w dziejach świata gwałtem i w ciągu tych stu trzydziestu lat wielokrotnie zrywał się do walki o wolność. Nie mając oręża w ręku, zlewając się i brocząc krwią, naród ten czasowo ustępował przed gwałcicielami, z tem jednak, by w lepszej dobie powstać ponownie. Dziesiątki, setki tysięcy szlachetnych synów Polski złożyło kości swe na polach walki, tysiącami szli oni na śmierć, do więzień, do twierdz, na zatracenie, w tajgi syberyjskie... Państwa sąsiednie, rozszarpawszy na części żywy organizm Polski, nie mogły jednak złamać i pognębić ducha polskięgo. Naród o kulturze wielowiekowej, nie ustępującej, a może i przewyższającej kulturę swych sąsiadów, naród, który wydał ze siebie Mickiewicza i Kościuszkę, naród, który dał światu Kopernika, Sienkiewicza, Matejkę, Szopena... nie mógł być zgnębiony duchowo przez zaborców."));
    }
}

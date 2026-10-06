package pl.psnc.vtl.portal.services.parsers.ocr;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.repositories.ocrtools.OCRToolsRepository;
import pl.psnc.vtl.portal.repositories.ocrtools.OcrBoundingBox;

import java.util.List;
import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class OCRToolsServiceITCase {

    @Autowired
    private OCRToolsRepository ocrToolsRepository;

    @Autowired
    private HOCRParser hocrParser;


    @Test
    public void shouldPerformOcrOnFullPdfPageInHocrFormat() {
        final long transcriptionId = 3;
        final String pageFilename = "wniosekurlopowy.pdf";

        String hocr = ocrToolsRepository.recognizePage(transcriptionId, pageFilename, null, "pol", false);
        List<Verse> verseList = hocrParser.parseVerses(hocr).get(0);

        assertThat(verseList.size(), greaterThan(0));
    }

    @Test
    public void shouldPerformOcrOnFullTifPageInHocrFormat() {
        final long transcriptionId = 1;
        final String pageFilename = "page0004r.tif";

        String hocr = ocrToolsRepository.recognizePage(transcriptionId, pageFilename, 1, "pol", false);
        List<Verse> verseList = hocrParser.parseVerses(hocr).get(0);

        assertThat(verseList.size(), greaterThan(0));
    }

    @Test
    public void shouldRecognizePartsOfThePageInPlaintextFormat() {
        final long transcriptionId = 1;
        final String pageFilename = "page0004r.tif";

        Long vese1Id = 1l;
        OcrBoundingBox box = new OcrBoundingBox(vese1Id, new BoundingBox(4, 312, 2150, 421));

        Map<Long, String> lines = ocrToolsRepository.recognizePageFragments(transcriptionId, pageFilename, 1,
                List.of(box), "pol", false, false);

        assertThat(lines.get(vese1Id), containsString("AKADEMIA"));
    }

}

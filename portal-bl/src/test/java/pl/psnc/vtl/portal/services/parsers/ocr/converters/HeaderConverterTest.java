package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.Test;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.verses.FormeWork;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;
import pl.psnc.vtl.portal.services.parsers.ocr.HocrConverterFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class HeaderConverterTest {

    @Test
    public void shouldPrepareProperHeaderVerse_1() {
        //given
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        HeaderConverter headerConverter = new HeaderConverter(hocrConverterFactory.getConverterSettings());
        final String html = "<div>Hello world</div>";
        Element tag = Jsoup.parse(html, "", Parser.xmlParser());
        //when
        List<Verse> verses = headerConverter.convert(tag);

        //then
        assertThat(verses.size(), is(0));
    }

    @Test
    public void shouldPrepareProperHeaderVerseForVerseWithOneLine() {
        //given
        final String headerHtml = "<div class='ocr_header' id='block_1_1' title=\"bbox 1544 198 2153 248\">\n" +
                "    <p class='ocr_par' id='par_1_1' lang='eng' title=\"bbox 1544 198 2153 248\">\n" +
                "     <span class='ocr_line' id='line_1_1'\n" +
                "           title=\"bbox 1544 198 2153 248; baseline -0.001 -14; x_size 50; x_descenders 14; x_ascenders 11\"><span\n" +
                "             class='ocrx_word' id='word_1_2' title='bbox 1544 198 1681 235; x_wconf 91'>Pisarz</span> <span\n" +
                "             class='ocrx_word' id='word_1_3' title='bbox 1717 200 1775 240; x_wconf 96'>14,</span> <span\n" +
                "             class='ocrx_word' id='word_1_4' title='bbox 1805 199 1864 233; x_wconf 96'>ks.</span> <span\n" +
                "             class='ocrx_word' id='word_1_5' title='bbox 1896 198 1961 232; x_wconf 87'>III</span> <span\n" +
                "             class='ocrx_word' id='word_1_6' title='bbox 1991 209 2023 232; x_wconf 85'>r.</span> <span\n" +
                "             class='ocrx_word' id='word_1_7' title='bbox 2058 200 2153 233; x_wconf 96'>1427</span>\n" +
                "     </span>\n" +
                "    </p>\n" +
                "</div>";
        Document doc = Jsoup.parse(headerHtml);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        HeaderConverter headerConverter = new HeaderConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = headerConverter.convert(doc.getElementsByClass("ocr_header").first());
        //then
        assertThat(verses.size(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(1544));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(198));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(2153));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(248));
        assertThat(verses.get(0).getContent(), is("Pisarz 14, ks. III r. 1427"));
        assertThat(verses.get(0).getFormatting().getFormeWork(), is(FormeWork.HEADER));
        assertThat(verses.get(0).getVerseNo(), is(1));
    }

    @Test
    public void shouldPrepareProperHeaderVerseWithTwoLines() {
        //given
        final String headerHtml = "<div class='ocr_header' id='block_1_1' title=\"bbox 1544 198 2153 248\">\n" +
                "    <p class='ocr_par' id='par_1_1' lang='eng' title=\"bbox 1544 198 2153 248\">\n" +
                "     <span class='ocr_line' id='line_1_1'\n" +
                "           title=\"bbox 1544 198 2153 248; baseline -0.001 -14; x_size 50; x_descenders 14; x_ascenders 11\"><span\n" +
                "             class='ocrx_word' id='word_1_2' title='bbox 1544 198 1681 235; x_wconf 91'>Pisarz</span> <span\n" +
                "             class='ocrx_word' id='word_1_3' title='bbox 1717 200 1775 240; x_wconf 96'>14,</span> <span\n" +
                "             class='ocrx_word' id='word_1_4' title='bbox 1805 199 1864 233; x_wconf 96'>ks.</span> <span\n" +
                "             class='ocrx_word' id='word_1_5' title='bbox 1896 198 1961 232; x_wconf 87'>III</span> <span\n" +
                "             class='ocrx_word' id='word_1_6' title='bbox 1991 209 2023 232; x_wconf 85'>r.</span> <span\n" +
                "             class='ocrx_word' id='word_1_7' title='bbox 2058 200 2153 233; x_wconf 96'>1427</span>\n" +
                "     </span>\n" +
                "     <span class='ocr_line' id='line1_2' title=\"bbox 1 2 3 4;\">" +
                "           another line" +
                "     </span>" +
                "    </p>\n" +
                "</div>";
        Document doc = Jsoup.parse(headerHtml);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        HeaderConverter headerConverter = new HeaderConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = headerConverter.convert(doc.getElementsByClass("ocr_header").first());
        //then
        assertThat(verses.size(), is(2));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(1544));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(198));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(2153));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(248));
        assertThat(verses.get(0).getContent(), is("Pisarz 14, ks. III r. 1427"));
        assertThat(verses.get(0).getFormatting().getFormeWork(), is(nullValue()));
        assertThat(verses.get(0).getVerseNo(), is(1));

        assertThat(verses.get(1).getBoundingBox().getUpperLeftX(), is(1));
        assertThat(verses.get(1).getBoundingBox().getUpperLeftY(), is(2));
        assertThat(verses.get(1).getBoundingBox().getLowerRightX(), is(3));
        assertThat(verses.get(1).getBoundingBox().getLowerRightY(), is(4));
        assertThat(verses.get(1).getContent(), is("another line"));
        assertThat(verses.get(1).getFormatting().getFormeWork(), is(FormeWork.HEADER));
        assertThat(verses.get(1).getVerseNo(), is(2));
    }

}
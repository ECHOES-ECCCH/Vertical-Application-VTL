package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;
import pl.psnc.vtl.portal.services.parsers.ocr.HocrConverterFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CaptionConverterTest {

    @Test
    public void shouldPrepareProperCaptionVerse_1() {
        //given
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        CaptionConverter captionConverter =new CaptionConverter(hocrConverterFactory.getConverterSettings());
        final String html = "<div>Hello world</div>";
        Element tag = Jsoup.parse(html);
        //when
        List<Verse> verses = captionConverter.convert(tag);

        //then
        assertThat(verses.size(), is(0));
    }

    @Test
    public void shouldPrepareProperCaptionVerseWithSingleLine() {
        //given
        final String html = "" +
                "<div class='ocr_caption' id='block_1_5' title=\"bbox 258 771 2154 834\">\n" +
                "   <p class='ocr_par' id='par_1_5' lang='eng' title=\"bbox 258 771 2154 834\">\n" +
                "       <span class='ocr_line' id='line_1_7' title=\"bbox 258 771 2154 834; baseline -0.005 -14; x_size 54; x_descenders 13; x_ascenders 12\">" +
                "           <span class='ocrx_word' id='word_1_45' title='bbox 258 784 362 824; x_wconf 92'>318.</span> " +
                "           <span class='ocrx_word' id='word_1_46' title='bbox 400 781 533 834; x_wconf 91'>(89v)</span> " +
                "           <span class='ocrx_word' id='word_1_47' title='bbox 572 804 612 808; x_wconf 91'>—</span> " +
                "           <span class='ocrx_word' id='word_1_48' title='bbox 640 777 823 819; x_wconf 91'>Paxina</span> " +
                "           <span class='ocrx_word' id='word_1_49' title='bbox 867 776 1007 817; x_wconf 92'>rotha</span> " +
                "           <span class='ocrx_word' id='word_1_50' title='bbox 1051 773 1279 816; x_wconf 91'>Miroslaii</span> " +
                "           <span class='ocrx_word' id='word_1_51' title='bbox 1314 773 1519 815; x_wconf 90'>testibus</span> " +
                "           <span class='ocrx_word' id='word_1_52' title='bbox 1559 774 1724 813; x_wconf 92'>contra</span> " +
                "           <span class='ocrx_word' id='word_1_53' title='bbox 1771 771 1952 813; x_wconf 92'>Paxino</span> " +
                "           <span class='ocrx_word' id='word_1_54' title='bbox 2000 771 2154 812; x_wconf 81'>Stani-</span>\n" +
                "       </span>\n" +
                "   </p>\n" +
                "</div>";
        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        CaptionConverter captionConverter = new CaptionConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = captionConverter.convert(doc.getElementsByClass("ocr_caption").first());
        //then
        assertThat(verses.size(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(258));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(771));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(2154));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(834));
        assertThat(verses.get(0).getContent(), is("318. (89v) — Paxina rotha Miroslaii testibus contra Paxino Stani-"));
        assertThat(verses.get(0).getFormatting().isImageCaption(), is(true));
        assertThat(verses.get(0).getVerseNo(), is(1));
    }

    @Test
    public void shouldPrepareProperCaptionVerseWithTwoLines() {
        //given
        final String html = "" +
                "<div class='ocr_caption' id='block_1_5' title=\"bbox 258 771 2154 834\">\n" +
                "   <p class='ocr_par' id='par_1_5' lang='eng' title=\"bbox 258 771 2154 834\">\n" +
                "       <span class='ocr_line' id='line_1_7' title=\"bbox 258 771 2154 834; baseline -0.005 -14; x_size 54; x_descenders 13; x_ascenders 12\">" +
                "           <span class='ocrx_word' id='word_1_45' title='bbox 258 784 362 824; x_wconf 92'>318.</span> " +
                "           <span class='ocrx_word' id='word_1_46' title='bbox 400 781 533 834; x_wconf 91'>(89v)</span> " +
                "           <span class='ocrx_word' id='word_1_47' title='bbox 572 804 612 808; x_wconf 91'>—</span> " +
                "           <span class='ocrx_word' id='word_1_48' title='bbox 640 777 823 819; x_wconf 91'>Paxina</span> " +
                "           <span class='ocrx_word' id='word_1_49' title='bbox 867 776 1007 817; x_wconf 92'>rotha</span> " +
                "           <span class='ocrx_word' id='word_1_50' title='bbox 1051 773 1279 816; x_wconf 91'>Miroslaii</span> " +
                "           <span class='ocrx_word' id='word_1_51' title='bbox 1314 773 1519 815; x_wconf 90'>testibus</span> " +
                "           <span class='ocrx_word' id='word_1_52' title='bbox 1559 774 1724 813; x_wconf 92'>contra</span> " +
                "           <span class='ocrx_word' id='word_1_53' title='bbox 1771 771 1952 813; x_wconf 92'>Paxino</span> " +
                "           <span class='ocrx_word' id='word_1_54' title='bbox 2000 771 2154 812; x_wconf 81'>Stani-</span>\n" +
                "       </span>\n" +
                "       <span class='ocr_line' id='line_1_7' title=\"bbox 1 2 3 4;\">" +
                "           another line with caption"+
                "       </span>\n" +
                "   </p>\n" +
                "</div>";
        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        CaptionConverter captionConverter = new CaptionConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = captionConverter.convert(doc.getElementsByClass("ocr_caption").first());
        //then
        assertThat(verses.size(), is(2));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(258));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(771));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(2154));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(834));
        assertThat(verses.get(0).getContent(), is("318. (89v) — Paxina rotha Miroslaii testibus contra Paxino Stani-"));
        assertThat(verses.get(0).getFormatting().isImageCaption(),is(true));
        assertThat(verses.get(0).getVerseNo(), is(1));

        assertThat(verses.get(1).getBoundingBox().getUpperLeftX(), is(1));
        assertThat(verses.get(1).getBoundingBox().getUpperLeftY(), is(2));
        assertThat(verses.get(1).getBoundingBox().getLowerRightX(), is(3));
        assertThat(verses.get(1).getBoundingBox().getLowerRightY(), is(4));
        assertThat(verses.get(1).getContent(), is("another line with caption"));
        assertThat(verses.get(1).getFormatting().isImageCaption(),is(true));
        assertThat(verses.get(1).getVerseNo(), is(2));
    }

}
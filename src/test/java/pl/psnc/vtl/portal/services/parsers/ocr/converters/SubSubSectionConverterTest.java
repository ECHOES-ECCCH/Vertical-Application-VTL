package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Ignore;
import org.junit.Test;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.verses.Heading;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;
import pl.psnc.vtl.portal.services.parsers.ocr.HocrConverterFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SubSubSectionConverterTest {

    @Test
    public void shouldPrepareProperSubSectionVerse_1() {
        //given
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        SubSubSectionConverter subSubSectionConverter = new SubSubSectionConverter(hocrConverterFactory.getConverterSettings());
        final String html = "<div>Hello world</div>";
        Element tag = Jsoup.parse(html);
        //when
        List<Verse> verses = subSubSectionConverter.convert(tag);

        //then
        assertThat(verses.size(), is(0));
    }

    @Test
    public void shouldPrepareProperSubSectionVersesWithSingleLine() {
        //given
        final String html = "" +
                "<h4 class='ocr_subsubsection' id='subsubsec_1_6' lang='eng' title=\"bbox 168 856 343 896\">\n" +
                "   <span class='ocr_line' id='line_1_8' title=\"bbox 168 856 343 896\">" +
                "       <span class='ocrx_word' id='word_1_55' title='bbox 168 856 343 896; x_wconf 90'>slaum:</span>\n" +
                "   </span>\n" +
                "</h4>";

        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        SubSubSectionConverter subSubSectionConverter = new SubSubSectionConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = subSubSectionConverter.convert(doc.getElementsByClass("ocr_subsubsEction").first());
        //then
        assertThat(verses.size(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(168));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(856));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(343));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(896));
        assertThat(verses.get(0).getContent(), is("slaum:"));
        assertThat(verses.get(0).getFormatting().getHeading(), is(Heading.END_OF_HEADING_3));
        assertThat(verses.get(0).getVerseNo(), is(1));
    }


    @Test
    public void shouldPrepareProperSubSectionVersesWithSingleLineAndOneWordWhenHasBoldFormatting() {
        //given
        final String html = "" +
                "<h4 class='ocr_subsubsection' id='subsubsec_1_6' lang='eng' title=\"bbox 168 856 343 896\">\n" +
                "   <span class='ocr_line' id='line_1_8' title=\"bbox 168 856 343 896\">" +
                "       <span class='ocrx_word' id='word_1_55' title='bbox 168 856 343 896; x_wconf 90'>A<strong>la ma k</strong>ota</span>\n" +
                "   </span>\n" +
                "</h4>";

        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        SubSubSectionConverter subSubSectionConverter = new SubSubSectionConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = subSubSectionConverter.convert(doc.getElementsByClass("ocr_subsubsEction").first());
        //then
        assertThat(verses.size(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(168));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(856));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(343));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(896));
        assertThat(verses.get(0).getContent(), is("A<strong>la ma k</strong>ota"));
        assertThat(verses.get(0).getFormatting().getHeading(), is(Heading.END_OF_HEADING_3));
        assertThat(verses.get(0).getVerseNo(), is(1));
    }


    @Test
    public void shouldPrepareProperSubSectionVersesWithSingleLineAndTwoWordsWhenHasBoldFormatting() {
        //given
        final String html = "" +
                "<h4 class='ocr_subsubsection' id='subsubsec_1_6' lang='eng' title=\"bbox 168 856 343 896\">\n" +
                "   <span class='ocr_line' id='line_1_8' title=\"bbox 168 856 343 1089\">" +
                "       <span class='ocrx_word' id='word_1_55' title='bbox 168 856 343 896; x_wconf 90'>A<strong>la ma k</strong>ota</span>\n" +
                "       <span class='ocrx_word' id='word_1_55' title='bbox 168 910 343 1089; x_wconf 90'>i <em>psa</em>.</span>\n" +
                "   </span>\n" +
                "</h4>";

        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        SubSubSectionConverter subSubSectionConverter = new SubSubSectionConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = subSubSectionConverter.convert(doc.getElementsByClass("ocr_subsubsEction").first());
        //then
        assertThat(verses.size(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(168));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(856));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(343));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(1089));
        assertThat(verses.get(0).getContent(), is("A<strong>la ma k</strong>ota i <em>psa</em>."));
        assertThat(verses.get(0).getFormatting().getHeading(), is(Heading.END_OF_HEADING_3));
        assertThat(verses.get(0).getVerseNo(), is(1));
    }


    @Test
    public void shouldPrepareProperSubSectionVersesWithSingleLineAndNoWordsWhenHasBoldFormatting() {
        //given
        final String html = "" +
                "<h4 class='ocr_subsubsection' id='subsubsec_1_6' lang='eng' title=\"bbox 168 856 343 896\">\n" +
                "   <span class='ocr_line' id='line_1_8' title=\"bbox 168 856 343 1089\">A<strong>la ma k</strong>ota i <em>psa</em>.</span>\n" +
                "</h4>";

        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        SubSubSectionConverter subSubSectionConverter = new SubSubSectionConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = subSubSectionConverter.convert(doc.getElementsByClass("ocr_subsubsEction").first());
        //then
        assertThat(verses.size(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(168));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(856));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(343));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(1089));
        assertThat(verses.get(0).getContent(), is("A<strong>la ma k</strong>ota i <em>psa</em>."));
        assertThat(verses.get(0).getFormatting().getHeading(), is(Heading.END_OF_HEADING_3));
        assertThat(verses.get(0).getVerseNo(), is(1));
    }

    @Test
    public void shouldPrepareProperCaptionVerseWithMultipleLines() {
        //given
        final String html = "" +
                "<h4 class='ocr_subsubsection' id='subsubsec_1_6' lang='eng' title=\"bbox 168 856 343 896\">\n" +
                "   <span class='ocr_line' id='line_1_8' title=\"bbox 1 2 3 4\">" +
                "       <span class='ocrx_word' id='word_1_55' title='bbox 168 856 343 896; x_wconf 90'>line 1</span>\n" +
                "   </span>\n" +
                "   <span class='ocr_line' id='line_1_8' title=\"bbox 5 6 7 8\">" +
                "       <span class='ocrx_word' id='word_1_55' title='bbox 168 856 343 896; x_wconf 90'>line 2</span>\n" +
                "   </span>\n" +
                "</h4>";

        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        SubSubSectionConverter subSubSectionConverter = new SubSubSectionConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = subSubSectionConverter.convert(doc.getElementsByClass("ocr_subsubsEctiOn").first());
        //then
        assertThat(verses.size(), is(2));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(2));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(3));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(4));
        assertThat(verses.get(0).getContent(), is("line 1"));
        assertThat(verses.get(0).getFormatting().getHeading(), is(nullValue()));
        assertThat(verses.get(0).getVerseNo(), is(1));

        assertThat(verses.get(1).getBoundingBox().getUpperLeftX(), is(5));
        assertThat(verses.get(1).getBoundingBox().getUpperLeftY(), is(6));
        assertThat(verses.get(1).getBoundingBox().getLowerRightX(), is(7));
        assertThat(verses.get(1).getBoundingBox().getLowerRightY(), is(8));
        assertThat(verses.get(1).getContent(), is("line 2"));
        assertThat(verses.get(1).getFormatting().getHeading(), is(Heading.END_OF_HEADING_3));
        assertThat(verses.get(1).getVerseNo(), is(2));
    }

    @Test
    public void shouldPrepareProperCaptionVerseWithMultipleLinesWhenManyWords() {
        //given
        final String html = "" +
                "<h4 class='ocr_subsubsection' id='subsubsec_1_6' lang='eng' title=\"bbox 168 856 343 896\">\n" +
                "   <span class='ocr_line' id='line_1_8' title=\"bbox 1 2 3 4\">" +
                "       <span class='ocrx_word' id='word_1_55' title='bbox 168 856 343 896; x_wconf 90'>line 1</span>\n" +
                 "<span class='ocrx_word' id='word_1_55' title='bbox 168 856 343 896; x_wconf 90'>line 2</span>\n" +
                "   </span>\n" +
                "</h4>";

        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        SubSubSectionConverter subSubSectionConverter = new SubSubSectionConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = subSubSectionConverter.convert(doc.getElementsByClass("ocr_subsubsEctiOn").first());
        //then
        assertThat(verses.size(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(2));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(3));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(4));
        assertThat(verses.get(0).getContent(), is("line 1 line 2"));
        assertThat(verses.get(0).getFormatting().getHeading(), is(Heading.END_OF_HEADING_3));
        assertThat(verses.get(0).getVerseNo(), is(1));
    }
}
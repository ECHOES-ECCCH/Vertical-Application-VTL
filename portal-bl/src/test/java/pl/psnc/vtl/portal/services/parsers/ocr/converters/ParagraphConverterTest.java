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
import static org.hamcrest.Matchers.*;

public class ParagraphConverterTest {

    @Test
    public void shouldPrepareProperParagraphVerse_1() {
        //given
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        ParagraphConverter paragraphConverter = new ParagraphConverter(hocrConverterFactory.getConverterSettings());
        final String html = "<div>Hello world</div>";
        Element tag = Jsoup.parse(html);
        //when
        List<Verse> verses = paragraphConverter.convert(tag);

        //then
        assertThat(verses.size(), is(0));
    }

    @Test
    public void shouldPrepareProperParagraphVerseWithSingleLine() {
        //given
        final String html = "" +
                "<p class='ocr_par' id='par_1_20' lang='eng' title=\"bbox 165 1813 2153 1973\">\n" +
                "   <span class='ocr_line' id='line_1_40' title=\"bbox 3 4 5 6; baseline -0.001 -14; x_size 50; x_descenders 14; x_ascenders 11\">jakis para</span>\n" +
                "</p>";
        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        ParagraphConverter paragraphConverter = new ParagraphConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = paragraphConverter.convert(doc.getElementsByClass("ocr_par").first());
        //then
        assertThat(verses.size(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(3));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(4));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(5));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(6));
        assertThat(verses.get(0).getContent(), is("jakis para"));
        assertThat(verses.get(0).getFormatting().isEndOfParagraph(), is(true));
        assertThat(verses.get(0).getVerseNo(), is(1));
    }

    @Test
    public void shouldPrepareProperParagraphVerseWithTwoLines() {
        //given
        final String html = "" +
                "<p class='ocr_par' id='par_1_20' lang='eng' title=\"bbox 165 1813 2153 1973\">\n" +
                "   <span class='ocr_line' id='line_1_40' title=\"bbox 165 1813 2153 1873; baseline -0.001 -14; x_size 50; x_descenders 14; x_ascenders 11\">Jakiś footer część 1</span>\n" +
                "   <span class='ocr_line' id='line_1_41' title=\"bbox 165 1913 2153 1973; baseline -0.001 -14; x_size 50; x_descenders 14; x_ascenders 11\">\n" +
                "       <span class='ocrx_word' id='word_1_149' title='bbox 165 1913 312 1973'>część</span>\n" +
                "      <span class='ocrx_word' id='word_1_150' title='bbox 340 1913 380 1973; x_wconf 96'>2</span>\n" +
                "   </span>\n" +
                "</p>";
        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        ParagraphConverter paragraphConverter = new ParagraphConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = paragraphConverter.convert(doc.getElementsByClass("ocr_par").first());
        //then
        assertThat(verses.size(), is(2));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(165));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(1813));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(2153));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(1873));
        assertThat(verses.get(0).getContent(), is("Jakiś footer część 1"));
        assertThat(verses.get(0).getFormatting().isEndOfParagraph(), is(false));
        assertThat(verses.get(0).getVerseNo(), is(1));
        //
        assertThat(verses.get(1).getBoundingBox().getUpperLeftX(), is(165));
        assertThat(verses.get(1).getBoundingBox().getUpperLeftY(), is(1913));
        assertThat(verses.get(1).getBoundingBox().getLowerRightX(), is(2153));
        assertThat(verses.get(1).getBoundingBox().getLowerRightY(), is(1973));
        assertThat(verses.get(1).getContent(), is("część 2"));
        assertThat(verses.get(1).getFormatting().isEndOfParagraph(), is(true));
        assertThat(verses.get(1).getVerseNo(), is(2));
    }
}
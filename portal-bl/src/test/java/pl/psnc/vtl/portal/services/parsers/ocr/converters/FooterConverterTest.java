package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

public class FooterConverterTest {
    @Test
    public void shouldPrepareProperHeaderVerse_1() {
        //given
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        FooterConverter footerConverter = new FooterConverter(hocrConverterFactory.getConverterSettings());
        final String footerHtml = "<div>Hello world</div>";
        Element tag = Jsoup.parse(footerHtml);
        //when
        List<Verse> verses = footerConverter.convert(tag);

        //then
        assertThat(verses.size(), is(0));
    }

    @Test
    public void shouldPrepareProperFooterVerseWithSingleLine() {
        //given
        final String footerHtml = "<div class='ocr_footer' id='block_1_20' title=\"bbox 165 1813 2153 1973\">\n" +
                "        <p class='ocr_par' id='par_1_20' lang='eng' title=\"bbox 165 1813 2153 1973\">\n" +
                "            <span class='ocr_line' id='line_1_40' title=\"bbox 165 1813 2153 1873; baseline -0.001 -14; x_size 50; x_descenders 14; x_ascenders 11\">Jakiś footer część 1</span>\n" +
                "        </p>\n" +
                "    </div>";
        Document doc = Jsoup.parse(footerHtml);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        FooterConverter footerConverter = new FooterConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = footerConverter.convert(doc.getElementsByClass("ocr_footer").first());
        //then
        assertThat(verses.size(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(165));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(1813));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(2153));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(1873));
        assertThat(verses.get(0).getContent(), is("Jakiś footer część 1"));
        assertThat(verses.get(0).getFormatting().getFormeWork(), is(FormeWork.FOOTER));
        assertThat(verses.get(0).getVerseNo(), is(1));
    }

    @Test
    public void shouldPrepareProperFooterVerseWithTwoLines() {
        //given
        final String footerHtml = "<div class='ocr_footer' id='block_1_20' title=\"bbox 165 1813 2153 1973\">\n" +
                "        <p class='ocr_par' id='par_1_20' lang='eng' title=\"bbox 165 1813 2153 1973\">\n" +
                "            <span class='ocr_line' id='line_1_40' title=\"bbox 165 1813 2153 1873; baseline -0.001 -14; x_size 50; x_descenders 14; x_ascenders 11\">Jakiś footer część 1</span>\n" +
                "            <span class='ocr_line' id='line_1_41' title=\"bbox 166 1914 2154 1974; baseline -0.001 -14; x_size 50; x_descenders 14; x_ascenders 11\">\n" +
                "                <span class='ocrx_word' id='word_1_149' title='bbox 165 1913 312 1973'>część</span>\n" +
                "                <span class='ocrx_word' id='word_1_150' title='bbox 340 1913 380 1973; x_wconf 96'>2</span>\n" +
                "            </span>\n" +
                "        </p>\n" +
                "    </div>";
        Document doc = Jsoup.parse(footerHtml);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        FooterConverter footerConverter = new FooterConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = footerConverter.convert(doc.getElementsByClass("ocr_footer").first());
        //then
        assertThat(verses.size(), is(2));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(165));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(1813));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(2153));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(1873));
        assertThat(verses.get(0).getContent(), is("Jakiś footer część 1"));
        assertThat(verses.get(0).getFormatting().getFormeWork(), is(nullValue()));
        assertThat(verses.get(0).getVerseNo(), is(1));

        assertThat(verses.get(1).getBoundingBox().getUpperLeftX(), is(166));
        assertThat(verses.get(1).getBoundingBox().getUpperLeftY(), is(1914));
        assertThat(verses.get(1).getBoundingBox().getLowerRightX(), is(2154));
        assertThat(verses.get(1).getBoundingBox().getLowerRightY(), is(1974));
        assertThat(verses.get(1).getContent(), is("część 2"));
        assertThat(verses.get(1).getFormatting().getFormeWork(), is(FormeWork.FOOTER));
        assertThat(verses.get(1).getVerseNo(), is(2));
    }

}
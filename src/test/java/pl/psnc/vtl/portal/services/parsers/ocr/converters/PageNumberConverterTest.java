package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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


public class PageNumberConverterTest {

    @Test
    public void shouldPrepareProperHeaderVerse_1() {
        //given
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        PageNumberConverter pageNumberConverter = new PageNumberConverter(hocrConverterFactory.getConverterSettings());

        final String html = "<div>Hello world</div>";
        Document doc = Jsoup.parse(html);
        //when
        List<Verse> verses = pageNumberConverter.convert(doc);

        //then
        assertThat(verses.size(), is(0));
    }

    @Test
    public void shouldPrepareProperHeaderVerseForVerseWithOneLine() {
        //given
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        PageNumberConverter pageNumberConverter = new PageNumberConverter(hocrConverterFactory.getConverterSettings());

        final String html = "" +
                "<div class='ocr_pageno' id='block_1_0' title=\"bbox 165 198 259 248\">\n" +
                "   <p class='ocr_par' id='par_1_0' lang='eng' title=\"bbox 165 198 259 248\">\n" +
                "       <span class='ocr_line' id='line_1_0' title=\"bbox 165 198 259 248; baseline -0.001 -14; x_size 50; x_descenders 14; x_ascenders 11\"><span class='ocrx_word' id='word_1_1' title='bbox 165 209 259 248; x_wconf 74'>249</span>\n" +
                "       </span>\n" +
                "   </p>\n" +
                "</div>";
        Document doc = Jsoup.parse(html);
        //when
        List<Verse> verses = pageNumberConverter.convert(doc.getElementsByClass("ocr_pageno").first());
        //then
        assertThat(verses.size(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(165));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(198));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(259));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(248));
        assertThat(verses.get(0).getContent(), is("249"));
        assertThat(verses.get(0).getFormatting().getFormeWork(), is(FormeWork.PAGE_NO));
        assertThat(verses.get(0).getVerseNo(), is(1));
    }

    @Test
    public void shouldPrepareProperHeaderVerseWithTwoLines() {
        //given
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        PageNumberConverter pageNumberConverter = new PageNumberConverter(hocrConverterFactory.getConverterSettings());

        final String html = "" +
                "<div class='ocr_pageno' id='block_1_0' title=\"bbox 165 198 259 248\">\n" +
                "   <p class='ocr_par' id='par_1_0' lang='eng' title=\"bbox 165 198 259 248\">\n" +
                "       <span class='ocr_line' id='line_1_0' title=\"bbox 165 198 259 248; baseline -0.001 -14; x_size 50; x_descenders 14; x_ascenders 11\">" +
                "           <span class='ocrx_word' id='word_1_1' title='bbox 165 209 259 248; x_wconf 74'>249</span>\n" +
                "       </span>\n" +
                "       <span class='ocr_line' id='line_1_0' title=\"bbox 3 5 123 111;\">4321</span>" +
                "   </p>\n" +
                "</div>";
        Document doc = Jsoup.parse(html);
        //when
        List<Verse> verses = pageNumberConverter.convert(doc.getElementsByClass("ocr_pageno").first());
        //then
        assertThat(verses.size(), is(2));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(165));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(198));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(259));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(248));
        assertThat(verses.get(0).getContent(), is("249"));
        assertThat(verses.get(0).getFormatting().getFormeWork(), is(nullValue()));
        assertThat(verses.get(0).getVerseNo(), is(1));

        assertThat(verses.get(1).getBoundingBox().getUpperLeftX(), is(3));
        assertThat(verses.get(1).getBoundingBox().getUpperLeftY(), is(5));
        assertThat(verses.get(1).getBoundingBox().getLowerRightX(), is(123));
        assertThat(verses.get(1).getBoundingBox().getLowerRightY(), is(111));
        assertThat(verses.get(1).getContent(), is("4321"));
        assertThat(verses.get(1).getFormatting().getFormeWork(), is(FormeWork.PAGE_NO));
        assertThat(verses.get(1).getVerseNo(), is(2));
    }



}
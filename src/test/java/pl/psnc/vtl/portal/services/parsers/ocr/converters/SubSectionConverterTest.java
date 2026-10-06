package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

public class SubSectionConverterTest {

    @Test
    public void shouldPrepareProperSubSectionVerse_1() {
        //given
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        SubSectionConverter subSectionConverter = new SubSectionConverter(hocrConverterFactory.getConverterSettings());
        final String html = "<div>Hello world</div>";
        Element tag = Jsoup.parse(html);
        //when
        List<Verse> verses = subSectionConverter.convert(tag);

        //then
        assertThat(verses.size(), is(0));
    }

    @Test
    public void shouldPrepareProperSubSectionVersesWithSingleLine() {
        //given
        final String html = "" +
                "<h3 class='ocr_subsection' id='subses_1_4' lang='eng' title=\"bbox 270 591 1931 647\">\n" +
                "   <span class='ocr_line' id='line_1_6' title=\"bbox 270 591 1931 647; baseline -0.007 -12; x_size 46; x_descenders 12; x_ascenders 11\">" +
                "       <span class='ocrx_word' id='word_1_33' title='bbox 270 600 450 647; x_wconf 87'>Zapiska</span> " +
                "       <span class='ocrx_word' id='word_1_34' title='bbox 469 597 648 633; x_wconf 43'>lacinska</span> " +
                "       <span class='ocrx_word' id='word_1_35' title='bbox 670 608 697 631; x_wconf 87'>w</span> " +
                "       <span class='ocrx_word' id='word_1_36' title='bbox 719 596 802 642; x_wconf 87'>rkp.</span> " +
                "       <span class='ocrx_word' id='word_1_37' title='bbox 819 606 872 641; x_wconf 92'>po</span> " +
                "       <span class='ocrx_word' id='word_1_38' title='bbox 892 594 1097 640; x_wconf 88'>nastepnej</span> " +
                "       <span class='ocrx_word' id='word_1_39' title='bbox 1115 594 1229 633; x_wconf 91'>rocie,</span> " +
                "       <span class='ocrx_word' id='word_1_40' title='bbox 1249 592 1474 637; x_wconf 56'>odnoszqcej</span> " +
                "       <span class='ocrx_word' id='word_1_41' title='bbox 1494 592 1550 634; x_wconf 80'>sie</span> " +
                "       <span class='ocrx_word' id='word_1_42' title='bbox 1568 591 1614 625; x_wconf 96'>do</span> " +
                "       <span class='ocrx_word' id='word_1_43' title='bbox 1632 591 1746 636; x_wconf 43'>inne)</span> " +
                "       <span class='ocrx_word' id='word_1_44' title='bbox 1764 601 1931 637; x_wconf 92'>sprawy.</span>\n" +
                "   </span>\n" +
                "</h3>";
        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        SubSectionConverter subSectionConverter = new SubSectionConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = subSectionConverter.convert(doc.getElementsByClass("ocr_subsEction").first());
        //then
        assertThat(verses.size(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(270));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(591));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(1931));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(647));
        assertThat(verses.get(0).getContent(), is("Zapiska lacinska w rkp. po nastepnej rocie, odnoszqcej sie do inne) sprawy."));
        assertThat(verses.get(0).getFormatting().getHeading(), is(Heading.END_OF_HEADING_2));
        assertThat(verses.get(0).getVerseNo(), is(1));

    }

    @Test
    public void shouldPrepareProperCaptionVerseWithMultipleLines() {
        //given
        final String html = "" +
                "<h3 class='ocr_subsection' id='subses_1_4' lang='eng' title=\"bbox 270 591 1931 647\">\n" +
                "   <span class='ocr_line' id='line_1_6' title=\"bbox 2 3 2 3; baseline -0.007 -12; x_size 46; x_descenders 12; x_ascenders 11\">" +
                "       <span class='ocrx_word' id='word_1_33' title='bbox 270 600 450 647; x_wconf 87'>1</span> " +
                "       <span class='ocrx_word' id='word_1_44' title='bbox 1764 601 1931 637; x_wconf 92'>2.</span>\n" +
                "   </span>\n" +
                "   <span class='ocr_line' id='line_1_6' title=\"bbox 100 101 102 103; baseline -0.007 -12; x_size 46; x_descenders 12; x_ascenders 11\">" +
                "       <span class='ocrx_word' id='word_1_33' title='bbox 270 600 450 647; x_wconf 87'>3</span> " +
                "       <span class='ocrx_word' id='word_1_44' title='bbox 1764 601 1931 637; x_wconf 92'>4</span>\n" +
                "   </span>\n" +
                "</h3>";
        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        SubSectionConverter subSectionConverter = new SubSectionConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = subSectionConverter.convert(doc.getElementsByClass("ocr_subsEctiOn").first());
        //then
        assertThat(verses.size(), is(2));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(2));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(3));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(2));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(3));
        assertThat(verses.get(0).getContent(), is("1 2."));
        assertThat(verses.get(0).getFormatting().getHeading(), is(nullValue()));
        assertThat(verses.get(0).getVerseNo(), is(1));

        assertThat(verses.get(1).getBoundingBox().getUpperLeftX(), is(100));
        assertThat(verses.get(1).getBoundingBox().getUpperLeftY(), is(101));
        assertThat(verses.get(1).getBoundingBox().getLowerRightX(), is(102));
        assertThat(verses.get(1).getBoundingBox().getLowerRightY(), is(103));
        assertThat(verses.get(1).getContent(), is("3 4"));
        assertThat(verses.get(1).getFormatting().getHeading(), is(Heading.END_OF_HEADING_2));
        assertThat(verses.get(1).getVerseNo(), is(2));
    }

}
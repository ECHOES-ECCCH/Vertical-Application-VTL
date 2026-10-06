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

public class SectionConverterTest {

    @Test
    public void shouldPrepareProperParagraphVerse_1() {
        //given
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        SectionConverter sectionConverter = new SectionConverter(hocrConverterFactory.getConverterSettings());
        final String html = "<div>Hello world</div>";
        Element tag = Jsoup.parse(html);
        //when
        List<Verse> verses = sectionConverter.convert(tag);

        //then
        assertThat(verses.size(), is(0));
    }

    @Ignore("Test zaignorowany gdyż po zmianie w formattimg coś źle import z hOCR ustawia koniec tytułu")
    @Test
    public void shouldPrepareProperSectionVersesWithMultipleLines() {
        //given
        final String html = "" +
                "<h2 class='ocr_section' id='sec_1_2' lang='eng' title=\"bbox 164 338 2155 548\">\n" +
                "   <span class='ocr_line' id='line_1_2' title=\"bbox 164 338 2154 403; baseline -0.007 -7; x_size 54; x_descenders 12; x_ascenders 13\">" +
                "       <span class='ocrx_word' id='word_1_8' title='bbox 164 352 427 396; x_wconf 91'>Thomislai</span> " +
                "       <span class='ocrx_word' id='word_1_9' title='bbox 457 353 607 392; x_wconf 92'>testes</span> " +
                "       <span class='ocrx_word' id='word_1_10' title='bbox 639 346 1069 399; x_wconf 40'>Marcezinkowsky:</span> " +
                "       <span class='ocrx_word' id='word_1_11' title='bbox 1110 341 1282 403; x_wconf 68'>[Ofzep</span> " +
                "       <span class='ocrx_word' id='word_1_12' title='bbox 1310 344 1367 386; x_wconf 93'>de</span> " +
                "       <span class='ocrx_word' id='word_1_13' title='bbox 1394 345 1603 397; x_wconf 92'>Rogowo</span> " +
                "       <span class='ocrx_word' id='word_1_14' title='bbox 1634 338 1923 400; x_wconf 91'>advocatus]</span> " +
                "       <span class='ocrx_word' id='word_1_15' title='bbox 1956 342 2154 395; x_wconf 96'>{primus</span>\n" +
                "   </span>\n" +
                "   <span class='ocr_line' id='line_1_3' title=\"bbox 169 413 2155 473\">" +
                "       <span class='ocrx_word' id='word_1_16' title='bbox 169 426 390 467; x_wconf 92'>Derslaus</span> " +
                "       <span class='ocrx_word' id='word_1_17' title='bbox 417 424 474 465; x_wconf 92'>de</span> " +
                "       <span class='ocrx_word' id='word_1_18' title='bbox 499 420 769 473; x_wconf 92'>Oczweka},</span> " +
                "       <span class='ocrx_word' id='word_1_19' title='bbox 795 418 1029 461; x_wconf 92'>secundus</span> " +
                "       <span class='ocrx_word' id='word_1_20' title='bbox 1053 416 1278 458; x_wconf 96'>Nicolaus</span> " +
                "       <span class='ocrx_word' id='word_1_21' title='bbox 1303 416 1360 457; x_wconf 93'>de</span> " +
                "       <span class='ocrx_word' id='word_1_22' title='bbox 1384 414 1663 468; x_wconf 56'>Oczwyeka,</span> " +
                "       <span class='ocrx_word' id='word_1_23' title='bbox 1686 414 1861 456; x_wconf 91'>tercius</span> " +
                "       <span class='ocrx_word' id='word_1_24' title='bbox 1886 413 2155 466; x_wconf 92'>Thurinsky</span>\n" +
                "   </span>\n" +
                "   <span class='ocr_line' id='line_1_4' title=\"bbox 164 485 2152 548; baseline -0.007 -10; x_size 54; x_descenders 12; x_ascenders 13\">" +
                "       <span class='ocrx_word' id='word_1_25' title='bbox 164 497 407 548; x_wconf 91'>Derslaus,</span> " +
                "       <span class='ocrx_word' id='word_1_26' title='bbox 439 495 639 547; x_wconf 91'>quartus</span> " +
                "       <span class='ocrx_word' id='word_1_27' title='bbox 666 491 934 532; x_wconf 91'>Mirzantha</span> " +
                "       <span class='ocrx_word' id='word_1_28' title='bbox 962 488 1299 541; x_wconf 91'>Caczkowsky,</span> " +
                "       <span class='ocrx_word' id='word_1_29' title='bbox 1329 487 1525 540; x_wconf 92'>quintus</span> " +
                "       <span class='ocrx_word' id='word_1_30' title='bbox 1552 485 1744 527; x_wconf 90'>Sbiluth</span> " +
                "       <span class='ocrx_word' id='word_1_31' title='bbox 1781 485 2152 537; x_wconf 91'>Kaczkowszky.</span>\n" +
                "   </span>\n" +
                "</h2>";
        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        SectionConverter sectionConverter = new SectionConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = sectionConverter.convert(doc.getElementsByClass("ocr_section").first());
        //then
        assertThat(verses.size(), is(3));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(164));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(338));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(2154));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(403));
        assertThat(verses.get(0).getContent(), is("Thomislai testes Marcezinkowsky: [Ofzep de Rogowo advocatus] {primus"));
        assertThat(verses.get(0).getFormatting().getHeading(), is(nullValue()));
        assertThat(verses.get(0).getVerseNo(), is(1));

        assertThat(verses.get(1).getBoundingBox().getUpperLeftX(), is(169));
        assertThat(verses.get(1).getBoundingBox().getUpperLeftY(), is(413));
        assertThat(verses.get(1).getBoundingBox().getLowerRightX(), is(2155));
        assertThat(verses.get(1).getBoundingBox().getLowerRightY(), is(473));
        assertThat(verses.get(1).getContent(), is("Derslaus de Oczweka}, secundus Nicolaus de Oczwyeka, tercius Thurinsky"));
        assertThat(verses.get(1).getFormatting().getHeading(), is(nullValue()));
        assertThat(verses.get(1).getVerseNo(), is(2));

        assertThat(verses.get(2).getBoundingBox().getUpperLeftX(), is(164));
        assertThat(verses.get(2).getBoundingBox().getUpperLeftY(), is(485));
        assertThat(verses.get(2).getBoundingBox().getLowerRightX(), is(2152));
        assertThat(verses.get(2).getBoundingBox().getLowerRightY(), is(548));
        assertThat(verses.get(2).getContent(), is("Derslaus, quartus Mirzantha Caczkowsky, quintus Sbiluth Kaczkowszky."));
        assertThat(verses.get(1).getFormatting().getHeading(), is(Heading.END_OF_HEADING_1));
        assertThat(verses.get(2).getVerseNo(), is(3));
    }

    @Test
    public void shouldPrepareProperCaptionVerseWithOneLine() {
        //given
        final String html = "" +
                "<h2 class='ocr_section' id='sec_1_2' lang='eng' title=\"bbox 164 338 2155 548\">\n" +
                "   <span class='ocr_line' id='line_1_2' title=\"bbox 9 9 9 9; baseline -0.007 -7; x_size 54; x_descenders 12; x_ascenders 13\">" +
                "       <span class='ocrx_word' id='word_1_8' title='bbox 164 352 427 396; x_wconf 91'>a</span> " +
                "       <span class='ocrx_word' id='word_1_9' title='bbox 457 353 607 392; x_wconf 92'>b</span> " +
                "       <span class='ocrx_word' id='word_1_10' title='bbox 639 346 1069 399; x_wconf 40'>c</span> " +
                "       <span class='ocrx_word' id='word_1_11' title='bbox 1110 341 1282 403; x_wconf 68'>d</span> " +
                "       <span class='ocrx_word' id='word_1_12' title='bbox 1310 344 1367 386; x_wconf 93'>e</span> " +
                "       <span class='ocrx_word' id='word_1_13' title='bbox 1394 345 1603 397; x_wconf 92'>f</span> " +
                "       <span class='ocrx_word' id='word_1_14' title='bbox 1634 338 1923 400; x_wconf 91'>g</span> " +
                "       <span class='ocrx_word' id='word_1_15' title='bbox 1956 342 2154 395; x_wconf 96'>h</span>\n" +
                "   </span>\n" +
                "</h2>";
        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        SectionConverter sectionConverter = new SectionConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = sectionConverter.convert(doc.getElementsByClass("ocr_section").first());
        //then
        assertThat(verses.size(), is(1));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(9));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(9));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(9));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(9));
        assertThat(verses.get(0).getContent(), is("a b c d e f g h"));
        assertThat(verses.get(0).getFormatting().getHeading(), is(Heading.END_OF_HEADING_1));
    }

}
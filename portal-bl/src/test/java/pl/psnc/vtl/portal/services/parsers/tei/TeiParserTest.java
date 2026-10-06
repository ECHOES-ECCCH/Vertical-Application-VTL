package pl.psnc.vtl.portal.services.parsers.tei;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import pl.psnc.vtl.portal.model.verses.FormeWork;
import pl.psnc.vtl.portal.model.verses.Heading;
import pl.psnc.vtl.portal.model.verses.Verse;

import java.io.IOException;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class TeiParserTest {

    @InjectMocks
    TeiParser teiParser;

    private final static String TEI_TRANSCRIPTION_1 = "test/tei/teiTranscription1.xml";

    private final static String TEI_TRANSCRIPTION_3 = "test/tei/teiTranscription3.xml";

    private String loadTeiFile(String fileName)
            throws IOException {
        return new String(getClass().getClassLoader().getResourceAsStream(fileName).readAllBytes());
    }


    @Test
    public void shouldParseSimplePageVersesFromTeiTranscriptionFile()
            throws IOException {

        List<Verse> verses = teiParser.parseVerses(loadTeiFile(TEI_TRANSCRIPTION_1)).get(0);

        //should properly parse whole simple content
        assertThat(verses.size(), is(5));
        assertThat(verses.get(0).getContent(), is("Ala ma kota1"));
        assertThat(verses.get(1).getContent(), is("Ala ma kota2"));
        assertThat(verses.get(2).getContent(), is("Ala ma kota3"));
        assertThat(verses.get(3).getContent(), is("Ala ma kota4"));
        assertThat(verses.get(4).getContent(), is("Ala ma kota5"));
    }
    @Test
    public void shouldParseComplexPageVersesFromTeiTranscriptionFile()
            throws IOException {

        List<Verse> verses = teiParser.parseVerses(loadTeiFile(TEI_TRANSCRIPTION_3)).get(0);

        assertThat(verses.size(), is(42));

        //should contain figure caption
        //<line ulx="400" uly="2000" lrx="1900" lry="2200">113874II/5a-b</line>
        assertThat(verses.get(0).getContent(), is("113874II/5a-b"));
        assertThat(verses.get(0).getFormatting().isImageCaption(), is(true));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(400));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(2000));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(1900));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(2200));

        //should contain paragraph
        //<line ulx="300" uly="2300" lrx="2100" lry="2400">Redaktor wydawnictwa Anna Kosmulska</line>
        assertThat(verses.get(1).getContent(), is("Redaktor wydawnictwa Anna Kosmulska"));
        assertThat(verses.get(1).getFormatting().isEndOfParagraph(), is(true));
        assertThat(verses.get(1).getBoundingBox().getUpperLeftX(), is(300));
        assertThat(verses.get(1).getBoundingBox().getUpperLeftY(), is(2300));
        assertThat(verses.get(1).getBoundingBox().getLowerRightX(), is(2100));
        assertThat(verses.get(1).getBoundingBox().getLowerRightY(), is(2400));

        //should contain header
        //<line ulx="50" uly="100" lrx="2100" lry="150">Pisarz 14, ks. III r. 1427; pisarz 15, ks. IV r 1429 i pisarz 16, ks. IV r. 1430</line>
        assertThat(verses.get(24).getContent(), is("Pisarz 14, ks. III r. 1427; pisarz 15, ks. IV r 1429 i pisarz 16, ks. IV r. 1430"));
        assertThat(verses.get(24).getFormatting().getFormeWork(), is(FormeWork.HEADER));
        assertThat(verses.get(24).getBoundingBox().getUpperLeftX(), is(50));
        assertThat(verses.get(24).getBoundingBox().getUpperLeftY(), is(100));
        assertThat(verses.get(24).getBoundingBox().getLowerRightX(), is(2100));
        assertThat(verses.get(24).getBoundingBox().getLowerRightY(), is(150));

        //should contain footer
        //<line ulx="950" uly="3600" lrx="1090" lry="3680">K</line>
        assertThat(verses.get(12).getContent(), is("K"));
        assertThat(verses.get(12).getFormatting().getFormeWork(), is(FormeWork.FOOTER));
        assertThat(verses.get(12).getBoundingBox().getUpperLeftX(), is(950));
        assertThat(verses.get(12).getBoundingBox().getUpperLeftY(), is(3600));
        assertThat(verses.get(12).getBoundingBox().getLowerRightX(), is(1090));
        assertThat(verses.get(12).getBoundingBox().getLowerRightY(), is(3680));


        //should contain pageNum
        //<line ulx="50" uly="100" lrx="150" lry="200">249</line>
        assertThat(verses.get(13).getContent(), is("249"));
        assertThat(verses.get(13).getFormatting().getFormeWork(), is(FormeWork.PAGE_NO));
        assertThat(verses.get(13).getBoundingBox().getUpperLeftX(), is(50));
        assertThat(verses.get(13).getBoundingBox().getUpperLeftY(), is(100));
        assertThat(verses.get(13).getBoundingBox().getLowerRightX(), is(150));
        assertThat(verses.get(13).getBoundingBox().getLowerRightY(), is(200));

        //should contain section
        //<line ulx="150" uly="1700" lrx="2000" lry="1800">Przciwko Hance z Kiszkowa (..)</line>
        assertThat(verses.get(37).getContent(), containsString("Przciwko Hance z Kiszkowa"));
        assertThat(verses.get(37).getFormatting().getHeading(), is(Heading.END_OF_HEADING_1));
        assertThat(verses.get(37).getBoundingBox().getUpperLeftX(), is(150));
        assertThat(verses.get(37).getBoundingBox().getUpperLeftY(), is(1700));
        assertThat(verses.get(37).getBoundingBox().getLowerRightX(), is(2000));
        assertThat(verses.get(37).getBoundingBox().getLowerRightY(), is(1800));

        //should contain subsection
        //<line ulx="150" uly="1980" lrx="2000" lry="2080">Mirosław Maćka Stanisława Jędrzeja</line>
        assertThat(verses.get(39).getContent(), is("Mirosław Maćka Stanisława Jędrzeja"));
        assertThat(verses.get(39).getFormatting().getHeading(), is(Heading.END_OF_HEADING_2));
        assertThat(verses.get(39).getBoundingBox().getUpperLeftX(), is(150));
        assertThat(verses.get(39).getBoundingBox().getUpperLeftY(), is(1980));
        assertThat(verses.get(39).getBoundingBox().getLowerRightX(), is(2000));
        assertThat(verses.get(39).getBoundingBox().getLowerRightY(), is(2080));

        //should contain subsubsection
        //<line ulx="150" uly="2220" lrx="2000" lry="2320">nej sprawy.</line>
        assertThat(verses.get(41).getContent(), is("nej sprawy."));
        assertThat(verses.get(41).getFormatting().getHeading(), is(Heading.END_OF_HEADING_3));
        assertThat(verses.get(41).getBoundingBox().getUpperLeftX(), is(150));
        assertThat(verses.get(41).getBoundingBox().getUpperLeftY(), is(2220));
        assertThat(verses.get(41).getBoundingBox().getLowerRightX(), is(2000));
        assertThat(verses.get(41).getBoundingBox().getLowerRightY(), is(2320));

        //should contain end-of-item
        //<line ulx="300" uly="3200" lrx="2150" lry="3290">Papier druk. sat. kl. III, 80g.</line>
        assertThat(verses.get(8).getContent(), is("Papier druk. sat. kl. III, 80g."));
        assertThat(verses.get(8).getFormatting().getListForm().toString(), is("end-of-item"));
        assertThat(verses.get(8).getBoundingBox().getUpperLeftX(), is(300));
        assertThat(verses.get(8).getBoundingBox().getUpperLeftY(), is(3200));
        assertThat(verses.get(8).getBoundingBox().getLowerRightX(), is(2150));
        assertThat(verses.get(8).getBoundingBox().getLowerRightY(), is(3290));

        //should contain end-of-list
        //<line ulx="300" uly="3300" lrx="2150" lry="3390">Wrocławska Drukarnia Naukowa. Zam. 2064/80 - B3 - cena zł 130. -</line>
        assertThat(verses.get(10).getContent(), is("Wrocławska Drukarnia Naukowa. Zam. 2064/80 - B3 - cena zł 130. -"));
        assertThat(verses.get(10).getFormatting().getListForm().toString(), is("end-of-list"));
        assertThat(verses.get(10).getBoundingBox().getUpperLeftX(), is(300));
        assertThat(verses.get(10).getBoundingBox().getUpperLeftY(), is(3300));
        assertThat(verses.get(10).getBoundingBox().getLowerRightX(), is(2150));
        assertThat(verses.get(10).getBoundingBox().getLowerRightY(), is(3390));


        //should contain end-of-cell
        //<line ulx="200" uly="920" lrx="800" lry="1020">tha|rzina nyqgdt bogushewa</line>
        assertThat(verses.get(28).getContent(), is("tha|rzina nyqgdt bogushewa"));
        assertThat(verses.get(28).getFormatting().getTabularForm().toString(), is("end-of-cell"));
        assertThat(verses.get(28).getBoundingBox().getUpperLeftX(), is(200));
        assertThat(verses.get(28).getBoundingBox().getUpperLeftY(), is(920));
        assertThat(verses.get(28).getBoundingBox().getLowerRightX(), is(800));
        assertThat(verses.get(28).getBoundingBox().getLowerRightY(), is(1020));

        //should contain end-of-row
        //<line ulx="1000" uly="920" lrx="1800" lry="1020">Katarzyna mnidy Boguszewa żpmna</line>
        assertThat(verses.get(31).getContent(), is("Katarzyna mnidy Boguszewa żpmna"));
        assertThat(verses.get(31).getFormatting().getTabularForm().toString(), is("end-of-row"));
        assertThat(verses.get(31).getBoundingBox().getUpperLeftX(), is(1000));
        assertThat(verses.get(31).getBoundingBox().getUpperLeftY(), is(920));
        assertThat(verses.get(31).getBoundingBox().getLowerRightX(), is(1800));
        assertThat(verses.get(31).getBoundingBox().getLowerRightY(), is(1020));

        //should contain end-of-table
        //<line ulx="200" uly="1270" lrx="1800" lry="1370">{Grodzedz testumus rotha Baranmek}</line>
        assertThat(verses.get(36).getContent(), is("{Grodzedz testumus rotha Baranmek}"));
        assertThat(verses.get(36).getFormatting().getTabularForm().toString(), is("end-of-table"));
        assertThat(verses.get(36).getBoundingBox().getUpperLeftX(), is(200));
        assertThat(verses.get(36).getBoundingBox().getUpperLeftY(), is(1270));
        assertThat(verses.get(36).getBoundingBox().getLowerRightX(), is(1800));
        assertThat(verses.get(36).getBoundingBox().getLowerRightY(), is(1370));
    }
}

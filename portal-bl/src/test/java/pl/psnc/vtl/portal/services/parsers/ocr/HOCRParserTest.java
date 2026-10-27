package pl.psnc.vtl.portal.services.parsers.ocr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.Verse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class HOCRParserTest {

    @InjectMocks
    HOCRParser hocrParser;

    private final static String SIMPLE_HOCR_FILE_NAME = "hocrWithParagraphsOnly.html";

    private final static String COMPLEX_HOCR_FILE_NAME = "hocrWithComplexStructure.html";

    @Test
    public void shouldParseVersesFromSimpleHocr()
            throws IOException {

        List<Verse> verses = hocrParser.parseVerses(loadXMLFile(SIMPLE_HOCR_FILE_NAME)).get(0);
        List<Verse> exceptedVerses = initVerses();

        assertThat(exceptedVerses.size(), is(verses.size()));

        for (int i = 0; i < verses.size(); i++) {
            assertThat(verses.get(i).getContent(), is(exceptedVerses.get(i).getContent()));
            assertThat(verses.get(i).getBoundingBox(), is(exceptedVerses.get(i).getBoundingBox()));
            assertThat(verses.get(i).getFormatting().isEndOfParagraph(), is(exceptedVerses.get(i).getFormatting().isEndOfParagraph()));
            assertThat(verses.get(i).getVerseNo(), is(i + 1));
        }
    }

    @Test
    public void shouldParseVersesFromComplexHocr()
            throws IOException {

        List<Verse> verses = hocrParser.parseVerses(loadXMLFile(COMPLEX_HOCR_FILE_NAME)).get(0);
        List<Verse> exceptedVerses = initVerses();

        assertThat(exceptedVerses.size(), is(verses.size()));
    }

    private List<Verse> initVerses() {
        BoundingBox boundingBox1 = new BoundingBox( 165, 198, 2153, 248);
        BoundingBox boundingBox2 = new BoundingBox(164, 338, 2154, 403);
        BoundingBox boundingBox3 = new BoundingBox(169, 413, 2155, 473);
        BoundingBox boundingBox4 = new BoundingBox(164, 485, 2152, 548);
        BoundingBox boundingBox5 = new BoundingBox(0, 46, 28, 2793);
        BoundingBox boundingBox6 = new BoundingBox(270, 591, 1931, 647);
        BoundingBox boundingBox7 = new BoundingBox(258, 771, 2154, 834);
        BoundingBox boundingBox8 = new BoundingBox(168, 856, 343, 896);
        BoundingBox boundingBox9 = new BoundingBox(170, 965, 1124, 1026);
        BoundingBox boundingBox10 = new BoundingBox(168, 1038, 1126, 1096);
        BoundingBox boundingBox11 = new BoundingBox(169, 1109, 1125, 1160);
        BoundingBox boundingBox12 = new BoundingBox(169, 1181, 1128, 1239);
        BoundingBox boundingBox13 = new BoundingBox(167, 1252, 1127, 1312);
        BoundingBox boundingBox14 = new BoundingBox(169, 1325, 1130, 1381);
        BoundingBox boundingBox15 = new BoundingBox(171, 1416, 339, 1444);
        BoundingBox boundingBox16 = new BoundingBox(1204, 962, 2156, 1017);
        BoundingBox boundingBox17 = new BoundingBox(1202, 1034, 2158, 1090);
        BoundingBox boundingBox18 = new BoundingBox(1202, 1096, 2155, 1159);
        BoundingBox boundingBox19 = new BoundingBox(1202, 1177, 2158, 1233);
        BoundingBox boundingBox20 = new BoundingBox(1204, 1248, 2158, 1304);
        BoundingBox boundingBox21 = new BoundingBox(1204, 1323, 1702, 1377);
        BoundingBox boundingBox22 = new BoundingBox(261, 1582, 2158, 1645);
        BoundingBox boundingBox23 = new BoundingBox(170, 1653, 2158, 1713);
        BoundingBox boundingBox24 = new BoundingBox(0, 0, 2596, 3760);

        Verse verse1 = new Verse();
        Verse verse2 = new Verse();
        Verse verse3 = new Verse();
        Verse verse4 = new Verse();
        Verse verse5 = new Verse();
        Verse verse6 = new Verse();
        Verse verse7 = new Verse();
        Verse verse8 = new Verse();
        Verse verse9 = new Verse();
        Verse verse10 = new Verse();
        Verse verse11 = new Verse();
        Verse verse12 = new Verse();
        Verse verse13 = new Verse();
        Verse verse14 = new Verse();
        Verse verse15 = new Verse();
        Verse verse16 = new Verse();
        Verse verse17 = new Verse();
        Verse verse18 = new Verse();
        Verse verse19 = new Verse();
        Verse verse20 = new Verse();
        Verse verse21 = new Verse();
        Verse verse22 = new Verse();
        Verse verse23 = new Verse();
        Verse verse24 = new Verse();

        verse1.setContent("249 Pisarz 14, ks. III r. 1427");
        verse2.setContent("Thomislai testes Marcezinkowsky: [Ofzep de Rogowo advocatus] {primus");
        verse3.setContent("Derslaus de Oczweka}, secundus Nicolaus de Oczwyeka, tercius Thurinsky");
        verse4.setContent("Derslaus, quartus Mirzantha Caczkowsky, quintus Sbiluth Kaczkowszky.");
        verse5.setContent("");
        verse6.setContent("Zapiska lacinska w rkp. po nastepnej rocie, odnoszqcej sie do inne) sprawy.");
        verse7.setContent("318. (89v) — Paxina rotha Miroslaii testibus contra Paxino Stani-");
        verse8.setContent("slaum:");
        verse9.setContent("Tako my bog pomofz ifzwathykrifz |");
        verse10.setContent("Jako tho polf{zlyedze ogefziza wpra|-");
        verse11.setContent("wo witawil miroflaw maczka | fta-");
        verse12.setContent("niflawa Jandrzeya yizto yelt | yego");
        verse13.setContent("prawa oczezyf{zna alye tho | [zalta-");
        verse14.setContent("wila mathka yego [tara | in media");
        verse15.setContent("marca.");
        verse16.setContent("Tako mi Bog pomoz i Swiety krzyz,");
        verse17.setContent("jako to polsledzie, 0 jez sie w prawo");
        verse18.setContent("wstawit Miroslaw: Ma¢ka, Stanis-");
        verse19.setContent("lawa, Jedrzeja, iz to jest jego prawa");
        verse20.setContent("o¢ezyzna, ale to zastawila matka");
        verse21.setContent("jego stara — — —");
        verse22.setContent("319. (90) — Testes. Miroslaw de Paxino erga Taxinszky (!) Stanislaum,");
        verse23.setContent("Mathiam. Testes: Mathias Zoleezsky, secundus Paulus Zoleczszky, tercius");
        verse24.setContent("");

        verse1.setBoundingBox(boundingBox1);
        verse2.setBoundingBox(boundingBox2);
        verse3.setBoundingBox(boundingBox3);
        verse4.setBoundingBox(boundingBox4);
        verse5.setBoundingBox(boundingBox5);
        verse6.setBoundingBox(boundingBox6);
        verse7.setBoundingBox(boundingBox7);
        verse8.setBoundingBox(boundingBox8);
        verse9.setBoundingBox(boundingBox9);
        verse10.setBoundingBox(boundingBox10);
        verse11.setBoundingBox(boundingBox11);
        verse12.setBoundingBox(boundingBox12);
        verse13.setBoundingBox(boundingBox13);
        verse14.setBoundingBox(boundingBox14);
        verse15.setBoundingBox(boundingBox15);
        verse16.setBoundingBox(boundingBox16);
        verse17.setBoundingBox(boundingBox17);
        verse18.setBoundingBox(boundingBox18);
        verse19.setBoundingBox(boundingBox19);
        verse20.setBoundingBox(boundingBox20);
        verse21.setBoundingBox(boundingBox21);
        verse22.setBoundingBox(boundingBox22);
        verse23.setBoundingBox(boundingBox23);
        verse24.setBoundingBox(boundingBox24);

        verse1.setFormatting(createFormattingWithEndOfParagraph());
        verse2.setFormatting(new Formatting());
        verse3.setFormatting(new Formatting());
        verse4.setFormatting(createFormattingWithEndOfParagraph());
        verse5.setFormatting(createFormattingWithEndOfParagraph());
        verse6.setFormatting(createFormattingWithEndOfParagraph());
        verse7.setFormatting(createFormattingWithEndOfParagraph());
        verse8.setFormatting(createFormattingWithEndOfParagraph());
        verse9.setFormatting(new Formatting());
        verse10.setFormatting(new Formatting());
        verse11.setFormatting(new Formatting());
        verse12.setFormatting(new Formatting());
        verse13.setFormatting(new Formatting());
        verse14.setFormatting(new Formatting());
        verse15.setFormatting(createFormattingWithEndOfParagraph());
        verse16.setFormatting(new Formatting());
        verse17.setFormatting(new Formatting());
        verse18.setFormatting(new Formatting());
        verse19.setFormatting(new Formatting());
        verse20.setFormatting(new Formatting());
        verse21.setFormatting(createFormattingWithEndOfParagraph());
        verse22.setFormatting(new Formatting());
        verse23.setFormatting(createFormattingWithEndOfParagraph());
        verse24.setFormatting(createFormattingWithEndOfParagraph());


        List<Verse> givenVerses = new ArrayList<>();
        givenVerses.add(verse1);
        givenVerses.add(verse2);
        givenVerses.add(verse3);
        givenVerses.add(verse4);
        givenVerses.add(verse5);
        givenVerses.add(verse6);
        givenVerses.add(verse7);
        givenVerses.add(verse8);
        givenVerses.add(verse9);
        givenVerses.add(verse10);
        givenVerses.add(verse11);
        givenVerses.add(verse12);
        givenVerses.add(verse13);
        givenVerses.add(verse14);
        givenVerses.add(verse15);
        givenVerses.add(verse16);
        givenVerses.add(verse17);
        givenVerses.add(verse18);
        givenVerses.add(verse19);
        givenVerses.add(verse20);
        givenVerses.add(verse21);
        givenVerses.add(verse22);
        givenVerses.add(verse23);
        givenVerses.add(verse24);

        return givenVerses;
    }

    private Formatting createFormattingWithEndOfParagraph() {
        Formatting formatting = new Formatting();
        formatting.setEndOfParagraph(true);
        return formatting;
    }

    private String loadXMLFile(String fileName)
            throws IOException {
        return new String(getClass().getClassLoader().getResourceAsStream(fileName).readAllBytes());
    }

}
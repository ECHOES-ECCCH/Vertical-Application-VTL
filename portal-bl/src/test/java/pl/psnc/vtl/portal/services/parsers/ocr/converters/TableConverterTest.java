package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.verses.TabularForm;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ConverterFactory;
import pl.psnc.vtl.portal.services.parsers.ocr.HocrConverterFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class TableConverterTest {

    @Test
    public void shouldPrepareProperSubSectionVerse_1() {
        //given
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        TableConverter tableConverter = new TableConverter(hocrConverterFactory.getConverterSettings());
        final String html = "<div>Hello world</div>";
        Element tag = Jsoup.parse(html);
        //when
        List<Verse> verses = tableConverter.convert(tag);

        //then
        assertThat(verses.size(), is(0));
    }

    @Test
    public void shouldPrepareTableVerses() {
        //given
        final String html = "" +
                "<table class='ocr_table' id='table_1_7' lang='eng' title=\"bbox 167 965 2158 1444\">\n" +
                "   <tr>\n" +
                "       <td>\n" +
                "           <span class='ocr_line' id='line_1_9' title=\"bbox 170 965 1124 1026; baseline -0.008 -12; x_size 54; x_descenders 12; x_ascenders 13\">Tako my bog pomofz ifzwathykrifz |</span>\n" +
                "           <span class='ocr_line' id='line_1_10' title=\"bbox 168 1038 1126 1096; baseline -0.006 -12; x_size 54; x_descenders 12; x_ascenders 13\">" +
                "               <span class='ocrx_word' id='word_1_62' title='bbox 168 1046 293 1087; x_wconf 96'>Jako</span> " +
                "               <span class='ocrx_word' id='word_1_63' title='bbox 313 1045 399 1086; x_wconf 93'>tho</span> " +
                "               <span class='ocrx_word' id='word_1_64' title='bbox 418 1042 714 1096; x_wconf 64'>polf{zlyedze</span> " +
                "               <span class='ocrx_word' id='word_1_65' title='bbox 733 1038 942 1093; x_wconf 18'>ogefziza</span> " +
                "               <span class='ocrx_word' id='word_1_66' title='bbox 960 1040 1126 1091; x_wconf 40'>wpra|-</span>\n" +
                "           </span>\n" +
                "       </td>\n" +
                "       <td>\n" +
                "           <span class='ocr_line' id='line_1_16' title=\"bbox 1204 962 2156 1017; baseline -0.003 -12; x_size 55; x_descenders 13; x_ascenders 13\">" +
                "               <span class='ocrx_word' id='word_1_93' title='bbox 1204 964 1336 1007; x_wconf 96'>Tako</span> " +
                "               <span class='ocrx_word' id='word_1_94' title='bbox 1361 964 1426 1005; x_wconf 96'>mi</span> " +
                "               <span class='ocrx_word' id='word_1_95' title='bbox 1449 964 1552 1017; x_wconf 93'>Bog</span> " +
                "               <span class='ocrx_word' id='word_1_96' title='bbox 1574 967 1745 1017; x_wconf 90'>pomoz</span> " +
                "               <span class='ocrx_word' id='word_1_97' title='bbox 1770 963 1785 1003; x_wconf 92'>i</span> " +
                "               <span class='ocrx_word' id='word_1_98' title='bbox 1809 962 1974 1015; x_wconf 77'>Swiety</span> " +
                "               <span class='ocrx_word' id='word_1_99' title='bbox 1997 962 2156 1015; x_wconf 92'>krzyz,</span>\n" +
                "           </span>\n" +
                "           <span class='ocr_line' id='line_1_17' title=\"bbox 1202 1034 2158 1090; baseline -0.004 -12; x_size 54; x_descenders 12; x_ascenders 13\">" +
                "               <span class='ocrx_word' id='word_1_100' title='bbox 1202 1036 1315 1090; x_wconf 96'>jako</span> " +
                "               <span class='ocrx_word' id='word_1_101' title='bbox 1337 1038 1389 1077; x_wconf 92'>to</span> " +
                "               <span class='ocrx_word' id='word_1_102' title='bbox 1410 1035 1679 1089; x_wconf 90'>polsledzie,</span> " +
                "               <span class='ocrx_word' id='word_1_103' title='bbox 1704 1047 1730 1076; x_wconf 87'>0</span> " +
                "               <span class='ocrx_word' id='word_1_104' title='bbox 1753 1034 1825 1088; x_wconf 96'>jez</span> " +
                "               <span class='ocrx_word' id='word_1_105' title='bbox 1850 1034 1913 1086; x_wconf 93'>sie</span> " +
                "               <span class='ocrx_word' id='word_1_106' title='bbox 1934 1047 1977 1076; x_wconf 72'>w</span> " +
                "               <span class='ocrx_word' id='word_1_107' title='bbox 1998 1045 2158 1087; x_wconf 92'>prawo</span>\n" +
                "           </span>\n" +
                "       </td>\n" +
                "   </tr>\n" +
                "   <tr>\n" +
                "       <td>\n" +
                "           <span class='ocr_line' id='line_1_11' title=\"bbox 169 1109 1125 1160; baseline -0.006 -4; x_size 51; x_descenders 9; x_ascenders 14\">" +
                "               <span class='ocrx_word' id='word_1_67' title='bbox 169 1131 239 1158; x_wconf 93'>wo</span> " +
                "               <span class='ocrx_word' id='word_1_68' title='bbox 271 1115 468 1158; x_wconf 88'>witawil</span> " +
                "               <span class='ocrx_word' id='word_1_69' title='bbox 500 1112 732 1154; x_wconf 91'>miroflaw</span> " +
                "               <span class='ocrx_word' id='word_1_70' title='bbox 762 1110 955 1153; x_wconf 91'>maczka</span> " +
                "               <span class='ocrx_word' id='word_1_71' title='bbox 955 1112 999 1160; x_wconf 93'>|</span> " +
                "               <span class='ocrx_word' id='word_1_72' title='bbox 1035 1109 1125 1151; x_wconf 73'>fta-</span>\n" +
                "           </span>\n" +
                "           <span class='ocr_line' id='line_1_12' title=\"bbox 169 1181 1128 1239; baseline -0.009 -9; x_size 55; x_descenders 12; x_ascenders 15\">" +
                "               <span class='ocrx_word' id='word_1_73' title='bbox 169 1188 360 1230; x_wconf 90'>niflawa</span> " +
                "               <span class='ocrx_word' id='word_1_74' title='bbox 385 1186 659 1239; x_wconf 91'>Jandrzeya</span> " +
                "               <span class='ocrx_word' id='word_1_75' title='bbox 685 1182 815 1237; x_wconf 80'>yizto</span> " +
                "               <span class='ocrx_word' id='word_1_76' title='bbox 841 1181 944 1236; x_wconf 48'>yelt</span> " +
                "               <span class='ocrx_word' id='word_1_77' title='bbox 975 1184 978 1232; x_wconf 92'>|</span> " +
                "               <span class='ocrx_word' id='word_1_78' title='bbox 1007 1184 1128 1234; x_wconf 73'>yego</span>\n" +
                "           </span>\n" +
                "       </td>\n" +
                "       <td>\n" +
                "           <span class='ocr_line' id='line_1_18' title=\"bbox 1202 1096 2155 1159; baseline -0.004 -9; x_size 55; x_descenders 12; x_ascenders 15\">" +
                "               <span class='ocrx_word' id='word_1_108' title='bbox 1202 1107 1402 1150; x_wconf 32'>wstawit</span> " +
                "               <span class='ocrx_word' id='word_1_109' title='bbox 1443 1107 1699 1148; x_wconf 90'>Miroslaw:</span> " +
                "               <span class='ocrx_word' id='word_1_110' title='bbox 1747 1096 1939 1159; x_wconf 34'>Ma¢ka,</span> " +
                "               <span class='ocrx_word' id='word_1_111' title='bbox 1978 1104 2155 1147; x_wconf 91'>Stanis-</span>\n" +
                "           </span>\n" +
                "           <span class='ocr_line' id='line_1_19' title=\"bbox 1202 1177 2158 1233; baseline -0.003 -13; x_size 53; x_descenders 12; x_ascenders 12\">" +
                "               <span class='ocrx_word' id='word_1_112' title='bbox 1202 1180 1338 1233; x_wconf 91'>lawa,</span> " +
                "               <span class='ocrx_word' id='word_1_113' title='bbox 1361 1179 1598 1232; x_wconf 92'>Jedrzeja,</span> " +
                "               <span class='ocrx_word' id='word_1_114' title='bbox 1621 1178 1662 1219; x_wconf 96'>iz</span> " +
                "               <span class='ocrx_word' id='word_1_115' title='bbox 1683 1180 1734 1219; x_wconf 96'>to</span> " +
                "               <span class='ocrx_word' id='word_1_116' title='bbox 1757 1178 1850 1232; x_wconf 93'>jest</span> " +
                "               <span class='ocrx_word' id='word_1_117' title='bbox 1870 1177 1976 1231; x_wconf 93'>jego</span> " +
                "               <span class='ocrx_word' id='word_1_118' title='bbox 1997 1189 2158 1230; x_wconf 90'>prawa</span>\n" +
                "           </span>\n" +
                "       </td>\n" +
                "   </tr>\n" +
                "   <tr>\n" +
                "       <td>\n" +
                "           <span class='ocr_line' id='line_1_13' title=\"bbox 167 1252 1127 1312; baseline -0.009 -10; x_size 53; x_descenders 11; x_ascenders 14\">" +
                "               <span class='ocrx_word' id='word_1_79' title='bbox 167 1273 329 1312'>prawa</span> " +
                "               <span class='ocrx_word' id='word_1_80' title='bbox 358 1256 643 1311; x_wconf 12'>oczezyf{zna</span> " +
                "               <span class='ocrx_word' id='word_1_81' title='bbox 671 1256 777 1308; x_wconf 91'>alye</span> " +
                "               <span class='ocrx_word' id='word_1_82' title='bbox 803 1255 888 1296; x_wconf 96'>tho</span> " +
                "               <span class='ocrx_word' id='word_1_83' title='bbox 923 1256 927 1304; x_wconf 93'>|</span> " +
                "               <span class='ocrx_word' id='word_1_84' title='bbox 958 1252 1127 1294; x_wconf 18'>[zalta-</span>\n" +
                "           </span>\n" +
                "       </td>\n" +
                "       <td>\n" +
                "           <span class='ocr_line' id='line_1_20' title=\"bbox 1204 1248 2158 1304; baseline -0.005 -11; x_size 53; x_descenders 12; x_ascenders 13\">" +
                "               <span class='ocrx_word' id='word_1_119' title='bbox 1204 1249 1453 1304; x_wconf 83'>o¢ezyzna,</span> " +
                "               <span class='ocrx_word' id='word_1_120' title='bbox 1499 1250 1571 1292; x_wconf 96'>ale</span> " +
                "               <span class='ocrx_word' id='word_1_121' title='bbox 1613 1252 1665 1291; x_wconf 92'>to</span> " +
                "               <span class='ocrx_word' id='word_1_122' title='bbox 1708 1248 1949 1291; x_wconf 91'>zastawila</span> " +
                "               <span class='ocrx_word' id='word_1_123' title='bbox 1992 1248 2158 1290; x_wconf 91'>matka</span>\n" +
                "           </span>\n" +
                "       </td>\n" +
                "   </tr>\n" +
                "</table>";

        Document doc = Jsoup.parse(html);
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        TableConverter tableConverter = new TableConverter(hocrConverterFactory.getConverterSettings());
        //when
        List<Verse> verses = tableConverter.convert(doc.getElementsByClass("ocr_table").first());
        //then
        assertThat(verses.size(), is(10));

        assertThat(verses.get(0).getBoundingBox().getUpperLeftX(), is(170));
        assertThat(verses.get(0).getBoundingBox().getUpperLeftY(), is(965));
        assertThat(verses.get(0).getBoundingBox().getLowerRightX(), is(1124));
        assertThat(verses.get(0).getBoundingBox().getLowerRightY(), is(1026));
        assertThat(verses.get(0).getContent(), is("Tako my bog pomofz ifzwathykrifz |"));
        assertThat(verses.get(0).getFormatting().getTabularForm(), is(nullValue()));
        assertThat(verses.get(0).getVerseNo(), is(1));

        assertThat(verses.get(1).getBoundingBox().getUpperLeftX(), is(168));
        assertThat(verses.get(1).getBoundingBox().getUpperLeftY(), is(1038));
        assertThat(verses.get(1).getBoundingBox().getLowerRightX(), is(1126));
        assertThat(verses.get(1).getBoundingBox().getLowerRightY(), is(1096));
        assertThat(verses.get(1).getContent(), is("Jako tho polf{zlyedze ogefziza wpra|-"));
        assertThat(verses.get(1).getFormatting().getTabularForm(), is(TabularForm.END_OF_CELL));
        assertThat(verses.get(1).getVerseNo(), is(2));

        assertThat(verses.get(2).getBoundingBox().getUpperLeftX(), is(1204));
        assertThat(verses.get(2).getBoundingBox().getUpperLeftY(), is(962));
        assertThat(verses.get(2).getBoundingBox().getLowerRightX(), is(2156));
        assertThat(verses.get(2).getBoundingBox().getLowerRightY(), is(1017));
        assertThat(verses.get(2).getContent(), is("Tako mi Bog pomoz i Swiety krzyz,"));
        assertThat(verses.get(2).getFormatting().getTabularForm(), is(nullValue()));
        assertThat(verses.get(2).getVerseNo(), is(3));

        assertThat(verses.get(3).getBoundingBox().getUpperLeftX(), is(1202));
        assertThat(verses.get(3).getBoundingBox().getUpperLeftY(), is(1034));
        assertThat(verses.get(3).getBoundingBox().getLowerRightX(), is(2158));
        assertThat(verses.get(3).getBoundingBox().getLowerRightY(), is(1090));
        assertThat(verses.get(3).getContent(), is("jako to polsledzie, 0 jez sie w prawo"));
        assertThat(verses.get(3).getFormatting().getTabularForm(), is(TabularForm.END_OF_ROW));
        assertThat(verses.get(3).getVerseNo(), is(4));

        assertThat(verses.get(4).getBoundingBox().getUpperLeftX(), is(169));
        assertThat(verses.get(4).getBoundingBox().getUpperLeftY(), is(1109));
        assertThat(verses.get(4).getBoundingBox().getLowerRightX(), is(1125));
        assertThat(verses.get(4).getBoundingBox().getLowerRightY(), is(1160));
        assertThat(verses.get(4).getContent(), is("wo witawil miroflaw maczka | fta-"));
        assertThat(verses.get(4).getFormatting().getTabularForm(), is(nullValue()));
        assertThat(verses.get(4).getVerseNo(), is(5));

        assertThat(verses.get(5).getBoundingBox().getUpperLeftX(), is(169));
        assertThat(verses.get(5).getBoundingBox().getUpperLeftY(), is(1181));
        assertThat(verses.get(5).getBoundingBox().getLowerRightX(), is(1128));
        assertThat(verses.get(5).getBoundingBox().getLowerRightY(), is(1239));
        assertThat(verses.get(5).getContent(), is("niflawa Jandrzeya yizto yelt | yego"));
        assertThat(verses.get(5).getFormatting().getTabularForm(), is(TabularForm.END_OF_CELL));
        assertThat(verses.get(5).getVerseNo(), is(6));

        assertThat(verses.get(6).getBoundingBox().getUpperLeftX(), is(1202));
        assertThat(verses.get(6).getBoundingBox().getUpperLeftY(), is(1096));
        assertThat(verses.get(6).getBoundingBox().getLowerRightX(), is(2155));
        assertThat(verses.get(6).getBoundingBox().getLowerRightY(), is(1159));
        assertThat(verses.get(6).getContent(), is("wstawit Miroslaw: Ma¢ka, Stanis-"));
        assertThat(verses.get(6).getFormatting().getTabularForm(), is(nullValue()));
        assertThat(verses.get(6).getVerseNo(), is(7));

        assertThat(verses.get(7).getBoundingBox().getUpperLeftX(), is(1202));
        assertThat(verses.get(7).getBoundingBox().getUpperLeftY(), is(1177));
        assertThat(verses.get(7).getBoundingBox().getLowerRightX(), is(2158));
        assertThat(verses.get(7).getBoundingBox().getLowerRightY(), is(1233));
        assertThat(verses.get(7).getContent(), is("lawa, Jedrzeja, iz to jest jego prawa"));
        assertThat(verses.get(7).getFormatting().getTabularForm(), is(TabularForm.END_OF_ROW));
        assertThat(verses.get(7).getVerseNo(), is(8));

        assertThat(verses.get(8).getBoundingBox().getUpperLeftX(), is(167));
        assertThat(verses.get(8).getBoundingBox().getUpperLeftY(), is(1252));
        assertThat(verses.get(8).getBoundingBox().getLowerRightX(), is(1127));
        assertThat(verses.get(8).getBoundingBox().getLowerRightY(), is(1312));
        assertThat(verses.get(8).getContent(), is("prawa oczezyf{zna alye tho | [zalta-"));
        assertThat(verses.get(8).getFormatting().getTabularForm(), is(TabularForm.END_OF_CELL));
        assertThat(verses.get(8).getVerseNo(), is(9));

        assertThat(verses.get(9).getBoundingBox().getUpperLeftX(), is(1204));
        assertThat(verses.get(9).getBoundingBox().getUpperLeftY(), is(1248));
        assertThat(verses.get(9).getBoundingBox().getLowerRightX(), is(2158));
        assertThat(verses.get(9).getBoundingBox().getLowerRightY(), is(1304));
        assertThat(verses.get(9).getContent(), is("o¢ezyzna, ale to zastawila matka"));
        assertThat(verses.get(9).getFormatting().getTabularForm(), is(TabularForm.END_OF_TABLE));
        assertThat(verses.get(9).getVerseNo(), is(10));
    }
}
package pl.psnc.vtl.portal.services.pages.content;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.repositories.pages.PageLayersRepository;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;

import javax.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class HOCRTranscriptionPageContentGeneratorTestITCase {

    @Autowired
    HOCRTranscriptionPageContentGenerator hocrTranscriptionPageContentGenerator;

    @Autowired
    PageRepository pageRepository;
    @Autowired
    PageLayersRepository pageLayersRepository;


    @Test
    public void shouldReturnHOCRForPage() {
        Page page = pageRepository.findById(4L).orElseThrow(EntityNotFoundException::new);
        List<Layer> layers = pageLayersRepository.findAllByPageId(4l).stream().map(PageLayer::getLayer).collect(
                Collectors.toList());
        List<ImmutablePair<Layer, String>> hOCR = hocrTranscriptionPageContentGenerator.generate(page, layers);
        Assert.assertThat(hOCR, is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">  \n" +
                "  <head> \n" +
                "    <title/>  \n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"/>  \n" +
                "    <meta name=\"ocr-system\" content=\"Virtual Laboratory Transcription\"/>  \n" +
                "    <meta name=\"ocr-capabilities\" content=\"ocr_page ocr_carea ocr_par ocr_line ocrx_word ocrp_wconf\"/> \n" +
                "  </head>  \n" +
                "  <body> \n" +
                "    <div class=\"ocr_page\" id=\"page_4\" title=\"image &quot;page250.tif&quot;; bbox 0 0 2595 3759; ppageno 4\"> \n" +
                "      <div class=\"ocr_header\" id=\"block_4_43\" title=\"bbox 50 100 2100 150\"> \n" +
                "        <p class=\"ocr_par\" id=\"par_4_43\" title=\"bbox 50 100 2100 150\"> \n" +
                "          <span class=\"ocr_line\" id=\"line_4_1\" title=\"bbox 50 100 2100 150\">Pisarz 14, ks. III r. 1427; pisarz 15, ks. IV r 1429 i pisarz 16, ks. IV r. 1430</span> \n" +
                "        </p> \n" +
                "      </div>  \n" +
                "      <div class=\"ocr_pageno\" id=\"block_4_44\" title=\"bbox 2300 100 2400 200\"> \n" +
                "        <p class=\"ocr_par\" id=\"par_4_44\" title=\"bbox 2300 100 2400 200\"> \n" +
                "          <span class=\"ocr_line\" id=\"line_4_2\" title=\"bbox 2300 100 2400 200\">250</span> \n" +
                "        </p> \n" +
                "      </div>  \n" +
                "      <div class=\"ocr_carea\" id=\"sec_4_45\" title=\"bbox 200 700 1800 1370\"> \n" +
                "        <table class=\"ocr_table\" id=\"table_4_45\" title=\"bbox 200 700 1800 1370\"> \n" +
                "          <tr> \n" +
                "            <td> \n" +
                "              <span class=\"ocr_line\" id=\"line_4_3\" title=\"bbox 200 700 800 800\">Tako mÿ pomosſÿ bog ÿſzwynntyj</span>  \n" +
                "              <span class=\"ocr_line\" id=\"line_4_4\" title=\"bbox 200 810 800 910\">kryfz Jakotho ſwathczimi ikao ka-</span>  \n" +
                "              <span class=\"ocr_line\" id=\"line_4_5\" title=\"bbox 200 920 800 1020\">tha|rzina nyqgdt bogushewa</span> \n" +
                "            </td>  \n" +
                "            <td> \n" +
                "              <span class=\"ocr_line\" id=\"line_4_6\" title=\"bbox 1000 700 1800 800\">Tako mi pomoży Bog i święty</span>  \n" +
                "              <span class=\"ocr_line\" id=\"line_4_7\" title=\"bbox 1000 810 1800 910\">krzyż, jako to świadczymy, jako</span>  \n" +
                "              <span class=\"ocr_line\" id=\"line_4_8\" title=\"bbox 1000 920 1800 1020\">Katarzyna mnidy Boguszewa żpmna</span> \n" +
                "            </td> \n" +
                "          </tr>  \n" +
                "          <tr> \n" +
                "            <td> \n" +
                "              <span class=\"ocr_line\" id=\"line_4_9\" title=\"bbox 200 1040 800 1140\">poſzagu <em>deece marcarun grossoru</em>\n" +
                "              </span>  \n" +
                "              <span class=\"ocr_line\" id=\"line_4_10\" title=\"bbox 200 1150 800 1250\"><em>latorym yney | nyulos</em> wdom\n" +
                "              </span> \n" +
                "            </td>  \n" +
                "            <td> \n" +
                "              <span class=\"ocr_line\" id=\"line_4_11\" title=\"bbox 1000 1040 1800 1140\">podsagu - - - - i nie wnmisła w dom</span>  \n" +
                "              <span class=\"ocr_line\" id=\"line_4_12\" title=\"bbox 1000 1150 1800 1250\">Dobiesław&lt;w&gt;ow Grodzi&lt;e&gt;go</span> \n" +
                "            </td> \n" +
                "          </tr>  \n" +
                "          <tr> \n" +
                "            <td> \n" +
                "              <span class=\"ocr_line\" id=\"line_4_13\" title=\"bbox 200 1270 1800 1370\">{Grodzedz testumus rotha Baranmek}</span> \n" +
                "            </td> \n" +
                "          </tr> \n" +
                "        </table> \n" +
                "      </div>  \n" +
                "      <div class=\"ocr_carea\" id=\"sec_4_56\" title=\"bbox 150 1700 2000 1800\"> \n" +
                "        <h2 class=\"ocr_section\" id=\"sec_4_56\" title=\"bbox 150 1700 2000 1800\"> \n" +
                "          <span class=\"ocr_line\" id=\"line_4_14\" title=\"bbox 150 1700 2000 1800\">Przciwko Hance z Kiszkowa o <em>czterrze</em> grzywny przez dziweięc skot\n" +
                "          </span> \n" +
                "        </h2> \n" +
                "      </div>  \n" +
                "      <div class=\"ocr_carea\" id=\"sec_4_57\" title=\"bbox 150 1820 450 1920\"> \n" +
                "        <h2 class=\"ocr_section\" id=\"sec_4_57\" title=\"bbox 150 1820 450 1920\"> \n" +
                "          <span class=\"ocr_line\" id=\"line_4_15\" title=\"bbox 150 1820 450 1920\">rękojmią</span> \n" +
                "        </h2> \n" +
                "      </div>  \n" +
                "      <div class=\"ocr_carea\" id=\"sec_4_58\" title=\"bbox 150 1980 2000 2080\"> \n" +
                "        <h3 class=\"ocr_subsection\" id=\"sec_4_58\" title=\"bbox 150 1980 2000 2080\"> \n" +
                "          <span class=\"ocr_line\" id=\"line_4_16\" title=\"bbox 150 1980 2000 2080\">Mirosław Maćka Stanisława Jędrzeja</span> \n" +
                "        </h3> \n" +
                "      </div>  \n" +
                "      <div class=\"ocr_carea\" id=\"sec_4_59\" title=\"bbox 150 2100 2000 2200\"> \n" +
                "        <h4 class=\"ocr_subsubsection\" id=\"sec_4_59\" title=\"bbox 150 2100 2000 2200\"> \n" +
                "          <span class=\"ocr_line\" id=\"line_4_17\" title=\"bbox 150 2100 2000 2200\">Zapisa łacińska z rkp. po nastęþnej rocie, odnoszącej się do in-</span> \n" +
                "        </h4> \n" +
                "      </div>  \n" +
                "      <div class=\"ocr_carea\" id=\"sec_4_60\" title=\"bbox 150 2220 2000 2320\"> \n" +
                "        <h4 class=\"ocr_subsubsection\" id=\"sec_4_60\" title=\"bbox 150 2220 2000 2320\"> \n" +
                "          <span class=\"ocr_line\" id=\"line_4_18\" title=\"bbox 150 2220 2000 2320\">nej sprawy.</span> \n" +
                "        </h4> \n" +
                "      </div> \n" +
                "    </div> \n" +
                "  </body> \n" +
                "</html>\n"));

    }

}
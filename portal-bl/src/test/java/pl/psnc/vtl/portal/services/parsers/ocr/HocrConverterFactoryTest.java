package pl.psnc.vtl.portal.services.parsers.ocr;

import org.junit.Test;
import pl.psnc.vtl.portal.services.parsers.ocr.converters.*;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class HocrConverterFactoryTest {

    @Test
    public void shouldCreateConverterForPageNumber() {
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        assertThat(hocrConverterFactory.getConverter("ocr_pageno"), instanceOf(PageNumberConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_pageNo"), instanceOf(PageNumberConverter.class));
        assertThat(hocrConverterFactory.getConverter("OCR_PAGENO"), instanceOf(PageNumberConverter.class));
    }

    @Test
    public void shouldCreateConverterForHeader() {
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        assertThat(hocrConverterFactory.getConverter("ocr_header"), instanceOf(HeaderConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_headeR"), instanceOf(HeaderConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_HEADER"), instanceOf(HeaderConverter.class));
    }

    @Test
    public void shouldCreateConverterForFooter() {
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        assertThat(hocrConverterFactory.getConverter("ocr_footer"), instanceOf(FooterConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_fooTer"), instanceOf(FooterConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_FOOTER"), instanceOf(FooterConverter.class));
    }

    @Test
    public void shouldCreateConverterForCaption() {
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        assertThat(hocrConverterFactory.getConverter("ocr_caption"), instanceOf(CaptionConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_caPtiOn"), instanceOf(CaptionConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_CAPTION"), instanceOf(CaptionConverter.class));
    }

    @Test
    public void shouldCreateConverterForArea() {
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        assertThat(hocrConverterFactory.getConverter("ocr_carea"), instanceOf(AreaConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_CArea"), instanceOf(AreaConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_CAREA"), instanceOf(AreaConverter.class));
    }

    @Test
    public void shouldCreateConverterForParagraph() {
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        assertThat(hocrConverterFactory.getConverter("ocr_par"), instanceOf(ParagraphConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_PaR"), instanceOf(ParagraphConverter.class));
        assertThat(hocrConverterFactory.getConverter("OCR_PAR"), instanceOf(ParagraphConverter.class));
    }

    @Test
    public void shouldCreateConverterForSection() {
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        assertThat(hocrConverterFactory.getConverter("ocr_section"), instanceOf(SectionConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_sectiOn"), instanceOf(SectionConverter.class));
        assertThat(hocrConverterFactory.getConverter("OCR_SECTION"), instanceOf(SectionConverter.class));
    }

    @Test
    public void shouldCreateConverterForSubSection() {
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        assertThat(hocrConverterFactory.getConverter("ocr_subsection"), instanceOf(SubSectionConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_subsectiON"), instanceOf(SubSectionConverter.class));
        assertThat(hocrConverterFactory.getConverter("OCR_SUBSECTION"), instanceOf(SubSectionConverter.class));
    }

    @Test
    public void shouldCreateConverterForSubSubSection() {
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        assertThat(hocrConverterFactory.getConverter("ocr_subsubsection"), instanceOf(SubSubSectionConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_subSUBsection"), instanceOf(SubSubSectionConverter.class));
        assertThat(hocrConverterFactory.getConverter("OCR_SUBSUBSECTION"), instanceOf(SubSubSectionConverter.class));
    }

    @Test
    public void shouldCreateConverterForTable() {
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        assertThat(hocrConverterFactory.getConverter("ocr_table"), instanceOf(TableConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_tabLE"), instanceOf(TableConverter.class));
        assertThat(hocrConverterFactory.getConverter("OCR_TABLE"), instanceOf(TableConverter.class));
    }

    @Test
    public void shouldCreateNoOpConverter() {
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        assertThat(hocrConverterFactory.getConverter("ok"), instanceOf(NoOpConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_tabLE1"), instanceOf(NoOpConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_parr"), instanceOf(NoOpConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocrr_caption"), instanceOf(NoOpConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_foote"), instanceOf(NoOpConverter.class));
        assertThat(hocrConverterFactory.getConverter("ocr_pagenoo"), instanceOf(NoOpConverter.class));
    }
}
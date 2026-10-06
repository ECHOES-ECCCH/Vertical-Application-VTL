package pl.psnc.vtl.portal.services.parsers.tei;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TeiElementsSupplierTest {

    @InjectMocks
    TeiElementsSupplier teiElementsSupplier;

    @Test
    public void shouldReplaceSimpleStrong(){
        String html = "Ala <hi rend=\"strong\">ma</hi> kota";
        String desiredHtml = "Ala <strong>ma</strong> kota";

        assertEquals(desiredHtml, teiElementsSupplier.prepareHtmlForTEI(html));
    }

    @Test
    public void shouldReplaceSimpleItalic(){
        String html = "Ala <hi rend=\"italic\">ma</hi> kota";
        String desiredHtml = "Ala <em>ma</em> kota";

        assertEquals(desiredHtml, teiElementsSupplier.prepareHtmlForTEI(html));
    }

    @Test
    public void shouldReplaceTwoItalic(){
        String html = "Ala <hi rend=\"italic\">ma</hi> kota Ala <hi rend=\"italic\">ma</hi> kota";
        String desiredHtml = "Ala <em>ma</em> kota Ala <em>ma</em> kota";

        assertEquals(desiredHtml, teiElementsSupplier.prepareHtmlForTEI(html));
    }

    @Test
    public void shouldReplaceTwoStrong(){
        String html = "Ala <hi rend=\"strong\">ma</hi> kota Ala <hi rend=\"strong\">ma</hi> kota";
        String desiredHtml = "Ala <strong>ma</strong> kota Ala <strong>ma</strong> kota";

        assertEquals(desiredHtml, teiElementsSupplier.prepareHtmlForTEI(html));
    }

    @Test
    public void shouldReplaceItalicInsideStrong(){
        String html = "Al<hi rend=\"strong\">a <hi rend=\"italic\">ma</hi> k</hi>ota";
        String desiredHtml = "Al<strong>a <em>ma</em> k</strong>ota";

        assertEquals(desiredHtml, teiElementsSupplier.prepareHtmlForTEI(html));
    }

    @Test
    public void shouldReplaceStrongInsideItalic(){
        String html = "Al<hi rend=\"italic\">a <hi rend=\"strong\">ma</hi> k</hi>ota";
        String desiredHtml = "Al<em>a <strong>ma</strong> k</em>ota";

        assertEquals(desiredHtml, teiElementsSupplier.prepareHtmlForTEI(html));
    }

    @Test
    public void shouldReplaceStrongInsideStrong(){
        String html = "Al<hi rend=\"strong\">a <hi rend=\"strong\">ma</hi> k</hi>ota";
        String desiredHtml = "Al<strong>a <strong>ma</strong> k</strong>ota";

        assertEquals(desiredHtml, teiElementsSupplier.prepareHtmlForTEI(html));
    }
}
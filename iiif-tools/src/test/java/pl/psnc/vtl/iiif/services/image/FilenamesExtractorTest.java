package pl.psnc.vtl.iiif.services.image;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FilenamesExtractorTest {


    @Test
    public void shouldExtractNamesFromOneUrl() throws Exception {
        List<String> urls = new ArrayList<String>();
        urls.add("https://rotha.ehum.psnc.pl/image/Pn.35/35_1388_25v_PoZ1_4.tif");

        FilenamesExtractor extractor = new FilenamesExtractor(urls);
        List<String> filenames = extractor.getFilenames();
        assertThat(filenames.get(0), is("35_1388_25v_PoZ1_4.tif"));
    }

    @Test
    public void shouldExtractNamesFromIIIFUrl() throws Exception {
        List<String> urls = new ArrayList<>();
        urls.add("https://bad.bsa.pcss.pl/iipsrv/iipsrv.fcgi/?IIIF=t9/przykladowy-plik_Page0000.tif/full/full/0/default.jpg");
        urls.add("https://bad.bsa.pcss.pl/iipsrv/iipsrv.fcgi?IIIF=t9/przykladowy_Page0000.tif/full/full/0/default.jpg");
        urls.add("https://bad.bsa.pcss.pl/iipsrv/iipsrv.fcgi?IIIF=Historia-1_Page0000.tif/full/full/0/default.jpg");
        urls.add("https://bad.bsa.pcss.pl/iipsrv/iipsrv.fcgi/?IIIF=Historia_Page0000.tif/full/full/0/default.jpg");
        urls.add("https://iiif.io/api/image/3.0/example/reference/59d09e6773341f28ea166e9f3c1e674f-gallica_ark_12148_bpt6k1526005v_f19/full/max/0/default.jpg");
        urls.add("https://iiif.io/api/image/3.0/example/reference/59d09e6773341f28ea166e9f3c1e674f-gallica_ark_12148_bpt6k1526005v_f18/full/max/0/default.jpg");

        FilenamesExtractor extractor = new FilenamesExtractor(urls);
        List<String> filenames = extractor.getFilenames();
        assertThat(filenames.get(0), is("przykladowy-plik_Page0000.tif.jpg"));
        assertThat(filenames.get(1), is("przykladowy_Page0000.tif.jpg"));
        assertThat(filenames.get(2), is("Historia-1_Page0000.tif.jpg"));
        assertThat(filenames.get(3), is("Historia_Page0000.tif.jpg"));
        assertThat(filenames.get(4), is("59d09e6773341f28ea166e9f3c1e674f-gallica_ark_12148_bpt6k1526005v_f19.jpg"));
        assertThat(filenames.get(5), is("59d09e6773341f28ea166e9f3c1e674f-gallica_ark_12148_bpt6k1526005v_f18.jpg"));
    }

    @Test
    public void shouldExtractNamesFromLastParts() throws Exception {
        List<String> urls = new ArrayList<String>();
        urls.add("https://rotha.ehum.psnc.pl/image/Pn.35/35_1388_25v_PoZ1_4.tif");
        urls.add("https://rotha.ehum.psnc.pl/image/Pn.35/35_1388_26r_PoZ1_4.tif");
        urls.add("https://rotha.ehum.psnc.pl/image/Pn.35/35_1388_26v_PoZ1_4.tif");
        urls.add("https://rotha.ehum.psnc.pl/image/Pn.35/35_1388_27_PoZ1_4.tif");

        FilenamesExtractor extractor = new FilenamesExtractor(urls);
        List<String> filenames = extractor.getFilenames();
        assertThat(filenames.get(0), is("35_1388_25v_PoZ1_4.tif"));
        assertThat(filenames.get(1), is("35_1388_26r_PoZ1_4.tif"));
        assertThat(filenames.get(2), is("35_1388_26v_PoZ1_4.tif"));
        assertThat(filenames.get(3), is("35_1388_27_PoZ1_4.tif"));
    }

    @Test
    public void shouldExtractNamesFromUrlEncodedParts() throws Exception {
        List<String> urls = new ArrayList<String>();
        urls.add("https://rotha.ehum.psnc.pl/image/Pn.35/35%201388%2025v%20PoZ1%204.tif");
        urls.add("https://rotha.ehum.psnc.pl/image/Pn.35/35 1388 26r PoZ1 4.tif");
        urls.add("https://rotha.ehum.psnc.pl/image/Pn.35/35%201388%2026v%20PoZ1%204.tif");
        urls.add("https://rotha.ehum.psnc.pl/image/Pn.35/35 1388 27 PoZ1 4.tif");

        FilenamesExtractor extractor = new FilenamesExtractor(urls);
        List<String> filenames = extractor.getFilenames();
        assertThat(filenames.get(0), is("35_1388_25v_PoZ1_4.tif"));
        assertThat(filenames.get(1), is("35_1388_26r_PoZ1_4.tif"));
        assertThat(filenames.get(2), is("35_1388_26v_PoZ1_4.tif"));
        assertThat(filenames.get(3), is("35_1388_27_PoZ1_4.tif"));
    }

    @Test
    public void shouldExtractNamesFromNotTheSameSizeParts() throws Exception {
        List<String> urls = new ArrayList<String>();
        urls.add("http://fbc.pionier.net.pl/zbiorki/Content/1159/OLM073/files/OLM073_prukaz_Svazarm_1.jpg");
        urls.add("http://fbc.pionier.net.pl/zbiorki/Content/1159/OLM073/OLM073_klub_cestovatelu_1.jpg");
        urls.add("http://fbc.pionier.net.pl/zbiorki/Content/1159/OLM073/OLM073_camping_1.jpg");
        urls.add("http://fbc.pionier.net.pl/zbiorki/Content/1159/OLM073/files/OLM073_prukaz_Svazarm_2.jpg");
        urls.add("http://fbc.pionier.net.pl/zbiorki/Content/1159/OLM073/files/OLM073_prukaz_automotoklub_2.jpg");
        urls.add("http://fbc.pionier.net.pl/zbiorki/Content/1159/OLM073/OLM073_klub_cestovatelu_2.jpg");
        urls.add("http://fbc.pionier.net.pl/zbiorki/Content/1159/OLM073/OLM073_camping_2.jpg");
        urls.add("http://fbc.pionier.net.pl/zbiorki/Content/1159/OLM073/files/OLM073_prukaz_automotoklub_1.jpg");

        FilenamesExtractor extractor = new FilenamesExtractor(urls);
        List<String> filenames = extractor.getFilenames();
        assertThat(filenames.get(0), is("OLM073_prukaz_Svazarm_1.jpg"));
        assertThat(filenames.get(1), is("OLM073_klub_cestovatelu_1.jpg"));
        assertThat(filenames.get(2), is("OLM073_camping_1.jpg"));
        assertThat(filenames.get(3), is("OLM073_prukaz_Svazarm_2.jpg"));
        assertThat(filenames.get(4), is("OLM073_prukaz_automotoklub_2.jpg"));
        assertThat(filenames.get(5), is("OLM073_klub_cestovatelu_2.jpg"));
        assertThat(filenames.get(6), is("OLM073_camping_2.jpg"));
        assertThat(filenames.get(7), is("OLM073_prukaz_automotoklub_1.jpg"));

    }

    @Test
    public void shouldExtractNamesFromMiddleParts() throws Exception {
        List<String> urls = new ArrayList<String>();
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0002/full/full/0/default.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0003/full/full/0/default.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0004/full/full/0/default.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0005/full/full/0/default.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0006/full/full/0/default.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0007/full/full/0/default.jpg");

        FilenamesExtractor extractor = new FilenamesExtractor(urls);
        List<String> filenames = extractor.getFilenames();
        assertThat(filenames.get(0), is("GSL_GSL_8033_II_50206_0002.jpg"));
        assertThat(filenames.get(1), is("GSL_GSL_8033_II_50206_0003.jpg"));
        assertThat(filenames.get(2), is("GSL_GSL_8033_II_50206_0004.jpg"));
        assertThat(filenames.get(3), is("GSL_GSL_8033_II_50206_0005.jpg"));
        assertThat(filenames.get(4), is("GSL_GSL_8033_II_50206_0006.jpg"));
        assertThat(filenames.get(5), is("GSL_GSL_8033_II_50206_0007.jpg"));
    }

    @Test
    public void shouldExtractNamesFromAccentedUrl() throws Exception {
        List<String> urls = new ArrayList<String>();
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/Żółw%20Ślimak/full/full/0/default.pdf");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/Źrebię/full/full/0/default.pdf");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/prosōidía/full/full/0/default.pdf");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/Aleksandrów+Łódź/full/full/0/default.pdf");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/Straße/full/full/0/default.pdf");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/øϮǭʡʢĂăãȦȧÄäÌìḾḿſẂẃŸÿẎẏꜲꜳÆæǢǣﬀﬃﬄﬁﬂĲĳǇǈǉʪʫɮŒœẞßﬆﬅʦʧ/full/full/0/default.pdf");

        FilenamesExtractor extractor = new FilenamesExtractor(urls);
        List<String> filenames = extractor.getFilenames();
        assertThat(filenames.get(0), is("Zolw_Slimak.pdf"));
        assertThat(filenames.get(1), is("Zrebie.pdf"));
        assertThat(filenames.get(2), is("prosoidia.pdf"));
        assertThat(filenames.get(3), is("Aleksandrow_Lodz.pdf"));
        assertThat(filenames.get(4), is("Straße.pdf"));
        assertThat(filenames.get(5), is("øϮoʡʢAaaAaAaIiMmſWwYyYyꜲꜳÆæÆæﬀﬃﬄﬁﬂĲĳǇǈǉʪʫɮŒœẞßﬆﬅʦʧ.pdf"));
    }

    @Test
    public void shouldExtractNamesFromDifferentParts() throws Exception {
        List<String> urls = new ArrayList<String>();
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0002/full/full/1/default.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0003/full/full/2/default.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0004/full/full/0/default1.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0005/full/full/0/default2.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0006/full1/full/0/default.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0007/full2/full/0/default.jpg");

        FilenamesExtractor extractor = new FilenamesExtractor(urls);
        List<String> filenames = extractor.getFilenames();
        assertThat(filenames.get(0), is("1.jpg"));
        assertThat(filenames.get(1), is("2.jpg"));
        assertThat(filenames.get(2), is("default1.jpg"));
        assertThat(filenames.get(3), is("default2.jpg"));
        assertThat(filenames.get(4), is("full1.jpg"));
        assertThat(filenames.get(5), is("full2.jpg"));
    }

    @Test(expected = NameCannotBeExtractedException.class)
    public void shouldNotExtractNamesWhenAreTheSame() throws Exception {
        List<String> urls = new ArrayList<String>();
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0002/full/full/0/default.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0003/full/full/0/default.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0004/full/full/0/default.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0005/full/full/0/default.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0006/full/full/0/default.jpg");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0002/full/full/0/default.jpg");

        FilenamesExtractor extractor = new FilenamesExtractor(urls);
        extractor.getFilenames();
    }

    @Test(expected = NameCannotBeExtractedException.class)
    public void shouldNotExtractNamesWhenUrlsAreTheSame() throws Exception {
        List<String> urls = new ArrayList<String>();
        urls.add("https://rotha.ehum.psnc.pl/image/Pn.35/35_1388_25v_PoZ1_4.tif");
        urls.add("https://rotha.ehum.psnc.pl/image/Pn.35/35_1388_26r_PoZ1_4.tif");
        urls.add("https://rotha.ehum.psnc.pl/image/Pn.35/35%201388%2026r%20PoZ1+4.tif");

        FilenamesExtractor extractor = new FilenamesExtractor(urls);
        extractor.getFilenames();
    }

    @Test(expected = NameCannotBeExtractedException.class)
    public void shouldNotExtractNamesWhenAreAlmostTheSame() throws Exception {
        List<String> urls = new ArrayList<String>();
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/Żółw%20Ślimak/full/full/0/default.pdf");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/Zolw%20Slimak/full/full/0/default.pdf");

        FilenamesExtractor extractor = new FilenamesExtractor(urls);
        extractor.getFilenames();
    }

    @Test(expected = NameCannotBeExtractedException.class)
    public void shouldNotExtractNamesFromMalformedUrl() throws Exception {
        List<String> urls = new ArrayList<String>();
        urls.add("lam.uni.wroc.pl/iiif/");
        urls.add("https://glam.uni.wroc.pl/iiif/image-api/Zolw%20Slimak/full/full/0/default.pdf");

        FilenamesExtractor extractor = new FilenamesExtractor(urls);
        extractor.getFilenames();
    }

}

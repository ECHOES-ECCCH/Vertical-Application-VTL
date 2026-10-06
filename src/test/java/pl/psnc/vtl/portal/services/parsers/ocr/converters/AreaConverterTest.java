package pl.psnc.vtl.portal.services.parsers.ocr.converters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Test;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.ocr.HocrConverterFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AreaConverterTest {

    @Test
    public void shouldPrepareProperAreaVerse_1() {
        //given
        HocrConverterFactory hocrConverterFactory = new HocrConverterFactory();
        AreaConverter areaConverter = new AreaConverter(hocrConverterFactory.getConverterSettings());
        final String footerHtml = "<div>Hello world</div>";
        Element tag = Jsoup.parse(footerHtml);
        //when
        List<Verse> verses = areaConverter.convert(tag);

        //then
        assertThat(verses.size(), is(0));
    }
}
package pl.psnc.vtl.portal.services.parsers.tei;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.parsers.TranscriptionContentParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeiParser implements TranscriptionContentParser {


    @Override
    public List<List<Verse>> parseVerses(InputStream inputStream) throws IOException {
        List<List<Verse>> result = new ArrayList<>();
        TeiConverterFactory converterFactory = new TeiConverterFactory();
        Document doc = Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), "unused");
        Elements teiPages = doc.body().getElementsByTag(Tei.TEI_PAGE_TYPE_NAME);
        for (Element pageElement : teiPages) {
            List<Verse> pageVerses = new ArrayList<>();
            result.add(pageVerses);
            for (Element verseElement : pageElement.children()) {
                pageVerses.addAll(converterFactory.getConverter(verseElement).convert(verseElement));
            }
            converterFactory.resetVerseNumbering();
        }
        return result;
    }

    @Override
    public boolean support(TranscriptionPageContentType pageContentType) {
        return TranscriptionPageContentType.TEI == pageContentType;
    }
}

package pl.psnc.vtl.portal.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

public class HTMLParser {

    /**
     * clear text from HTML tags
     * @param content enriched text
     * @return plain text
     */
    public static String plainText(String content) {
        Document doc = Jsoup.parse(content);
        Safelist sl = Safelist.none();
        String plainText = Jsoup.clean(doc.html(), sl);
        return plainText;
    }
}

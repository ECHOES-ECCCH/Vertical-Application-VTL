package pl.psnc.vtl.portal.utils;

import org.junit.Test;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionInfo;

public class HTMLParserTest {

    @Test
    public void plainText() {
        assert HTMLParser.plainText("kkkk").equals("kkkk");
        assert HTMLParser.plainText("<siv>kkkk</siv>").equals("kkkk");
        assert HTMLParser.plainText("<div>kkkk").equals("kkkk");
    }

}
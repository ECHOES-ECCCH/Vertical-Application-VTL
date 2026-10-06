package pl.psnc.vtl.portal.model.transcription;

import org.junit.Test;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionInfo;
import pl.psnc.vtl.portal.utils.HTMLParser;

public class TranscriptionInfoTest {



    @Test
    public void removeWhiteSpace() {
        TranscriptionInfo info = new TranscriptionInfo();
        info.removeWhitespace();
        assert (info.getDescriptionPlain()) == null;
        assert (info.getDocumentSrcPlain()) == null;
        assert (info.getGuidelinesPlain()) == null;
        assert (info.getImgSrcPlain()) == null;

        info.setDescription("<siv>kkkk</siv>");
        info.setDocumentSrc("<div>kkkk");
        info.setGuidelines("<siv>kkkk</siv>");
        info.setImgSrc("kkkk");
        info.removeWhitespace();
        assert (info.getDescriptionPlain()).equals("kkkk");
        assert (info.getDocumentSrcPlain()).equals("kkkk");
        assert (info.getGuidelinesPlain()).equals("kkkk");
        assert (info.getImgSrcPlain()).equals("kkkk");
    }
}
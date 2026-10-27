package pl.psnc.vtl.portal.model.verse;

import org.junit.Test;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionInfo;
import pl.psnc.vtl.portal.model.verses.VerseContent;

public class VerseContentTest {



    @Test
    public void removeWhiteSpace() {
        VerseContent vc = new VerseContent();
        vc.removeWhitespace();
        assert (vc.getPlainContent()) == null;

        vc.setContent("<siv>kkkk</siv>");
        vc.removeWhitespace();
        assert (vc.getPlainContent()).equals("kkkk");
    }
}
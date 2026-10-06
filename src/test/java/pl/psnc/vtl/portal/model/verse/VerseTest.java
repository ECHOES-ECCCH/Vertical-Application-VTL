package pl.psnc.vtl.portal.model.verse;

import org.junit.Test;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.verses.VerseContent;

public class VerseTest {


    @Test
    public void removeWhiteSpace() {
        Verse v = new Verse();
        v.removeWhitespace();
        assert (v.getPlainContent()) == null;

        v.setContent("<siv>kkkk</siv>");
        v.removeWhitespace();
        assert (v.getPlainContent()).equals("kkkk");
    }
}
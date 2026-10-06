package pl.psnc.vtl.portal.conf.converters;

import org.junit.Test;
import pl.psnc.vtl.portal.model.verses.FormeWork;

import java.text.ParseException;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class EnumFormatterTest {

    @Test
    public void print() {
        EnumFormatter<FormeWork> f = new EnumFormatter<>(FormeWork.class);
        String str = f.print(FormeWork.FOOTER, Locale.CANADA);
        String ex = "footer";
        assertEquals(ex, str);
    }


    @Test
    public void parse()
            throws ParseException {
        EnumFormatter<FormeWork> f = new EnumFormatter<>(FormeWork.class);
        String value = "footer";
        FormeWork variable = (FormeWork) f.parse(value, Locale.CANADA);
        assertEquals(value, f.print(variable, Locale.CANADA));
    }

}
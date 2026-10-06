package pl.psnc.vtl.portal.conf.converters;

import lombok.AllArgsConstructor;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

@AllArgsConstructor
public class EnumFormatter<E extends Enum<E>> implements Formatter<E> {

    private final Class<E> enumClass;


    @Override
    public String print(E enumeration, Locale locale) {
        return enumeration.name().replace('_', '-').toLowerCase();
    }


    @Override
    public E parse(String text, Locale locale)
            throws ParseException {
        try {
            E enumeration = E.valueOf(enumClass, text.toUpperCase().replace('-', '_'));
            return enumeration;
        } catch (Exception e) {
            throw new ParseException("Incorrect value '" + text + "' for enum '" + enumClass.getName() + "'!", 0);
        }
    }

}

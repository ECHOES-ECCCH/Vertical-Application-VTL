package pl.psnc.vtl.portal.services.exports.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TextFileSeparators {

    public static final String VERSE_SEPARATOR = " ";
    public static final String LINE_SEPARATOR = "\n";
    public static final String TABLE_CELL_SEPARATOR = "\t";
    public static final String SECTION_SEPARATOR = "\n\n";

    public void prepareForVerseAppend(StringBuilder sb, String nextVerseContent) {
        if (sb.length() == 0)
            return;
        char lastChar = sb.charAt(sb.length() - 1);
        if (lastChar == '\u002D' || lastChar == '\u2010' || lastChar == '\u2013') {
            if (endsWithWord(sb) && startsWithWord(nextVerseContent))
                sb.deleteCharAt(sb.length() - 1);
        } else if ((lastChar == '\u2212' || lastChar == '\u2014') && endsWithWord(sb)
                && startsWithWord(nextVerseContent)) {
            // do nothing
        } else if (!Character.isWhitespace(lastChar)) {
            sb.append(TextFileSeparators.VERSE_SEPARATOR);
        }
    }

    private static boolean endsWithWord(StringBuilder sb) {
        for (int i = sb.length() - 2; i >= 0; i--) {
            char c = sb.charAt(i);
            if (Character.isWhitespace(c))
                return true;
            if (!Character.isLetter(c))
                return false;
        }
        return true;
    }

    private static boolean startsWithWord(String verseContent) {
        for (int i = 0; i < verseContent.length(); i++) {
            char c = verseContent.charAt(i);
            if (Character.isWhitespace(c))
                return true;
            if (!Character.isLetter(c))
                return false;
        }
        return true;
    }
}

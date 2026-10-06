package pl.psnc.vtl.portal.services.parsers.tei;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.verses.Formatting;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.model.verses.VerseStatus;
import pl.psnc.vtl.portal.services.parsers.DocumentElementsSupplier;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeiElementsSupplier extends DocumentElementsSupplier {

    private static final String patternStrongString = "(<hi rend=\"strong\">)(((?!</hi>|<hi rend=\"strong\">|<hi rend=\"italic\">).)*)(</hi>)";
    private static final String patternItalicString = "(<hi rend=\"italic\">)(((?!</hi>|<hi rend=\"italic\">|<hi rend=\"strong\">).)*)(</hi>)";
    private static final String patternUnderlineString = "(<hi rend=\"underline\">)(((?!</hi>|<hi rend=\"underline\">|<hi rend=\"underline\">).)*)(</hi>)";
    private static final String patternStrikethroughString = "(<del rend=\"strikethrough\">)(((?!</del>|<del rend=\"strikethrough\">|<del rend=\"strikethrough\">).)*)(</del>)";

    private static final Pattern patternStrong = Pattern.compile(patternStrongString);
    private static final Pattern patternItalic = Pattern.compile(patternItalicString);
    private static final Pattern patternUnderline = Pattern.compile(patternUnderlineString);
    private static final Pattern patternStrikethrough = Pattern.compile(patternStrikethroughString);

    @Override
    public Elements supplyLines(Element element) {
        return element.getElementsByTag(Tei.TEI_LINE_TYPE_NAME);
    }

    @Override
    public Verse createVerseWithContentAndBboxOnly(Element element) {
        Verse verse = new Verse();
        verse.setStatus(VerseStatus.NONE);
        verse.setBoundingBox(prepareBoundingBoxForElement(element));
        String content = getContent(element);
        verse.setContent(content);
        verse.setFormatting(new Formatting());
        verse.setVerseNo(verseNumberSupplier.getNextVerseNumber());
        return verse;
    }


    public String prepareHtmlForTEI(String html){

        boolean foundStrong = true;
        boolean foundItalic = true;
        boolean foundStrikethrough = true;
        boolean foundUnderline = true;

        while(foundStrong || foundItalic || foundStrikethrough || foundUnderline) {

            Matcher matcherStrong = patternStrong.matcher(html);
            foundStrong = false;
            int groupCount = matcherStrong.groupCount();
            while (matcherStrong.find()) {
                String foundHtml = matcherStrong.group(1) + matcherStrong.group(2) + matcherStrong.group(groupCount);
                String desiredHtml = "<strong>" + matcherStrong.group(2) + "</strong>";
                html = html.replace(foundHtml, desiredHtml);
                foundStrong = true;
            }

            Matcher matcherItalic = patternItalic.matcher(html);
            foundItalic = false;
            groupCount = matcherStrong.groupCount();
            while (matcherItalic.find()) {
                String foundHtml = matcherItalic.group(1) + matcherItalic.group(2) + matcherItalic.group(groupCount);
                String desiredHtml = "<em>" + matcherItalic.group(2) + "</em>";
                html = html.replace(foundHtml, desiredHtml);
                foundItalic = true;
            }

            Matcher matcherUnderline = patternUnderline.matcher(html);
            foundUnderline = false;
            groupCount = matcherStrong.groupCount();
            while (matcherUnderline.find()) {
                String foundHtml = matcherUnderline.group(1) + matcherUnderline.group(2) + matcherUnderline.group(groupCount);
                String desiredHtml = "<u>" + matcherUnderline.group(2) + "</u>";
                html = html.replace(foundHtml, desiredHtml);
                foundUnderline = true;
            }

            Matcher matcherStrikethrough = patternStrikethrough.matcher(html);
            foundStrikethrough = false;
            groupCount = matcherStrong.groupCount();
            while (matcherStrikethrough.find()) {
                String foundHtml = matcherStrikethrough.group(1) + matcherStrikethrough.group(2) + matcherStrikethrough.group(groupCount);
                String desiredHtml = "<s>" + matcherStrikethrough.group(2) + "</s>";
                html = html.replace(foundHtml, desiredHtml);
                foundStrikethrough = true;
            }
        }
        return html;
    }



    private String getContent(Element element) {
        String html = element.html();
        html = prepareHtmlForTEI(html);
        String typeAttribute = element.attributes().get("type");
        if(typeAttribute.equals("figure")){
            return null;
        }
        return html;
    }

    @Override
    public BoundingBox prepareBoundingBoxForElement(Element element) {
        try {
            BoundingBox boundingBox = new BoundingBox();
            Attributes attributes = element.attributes();
            boundingBox.setUpperLeftX(Integer.parseInt(attributes.get("ulx")));
            boundingBox.setUpperLeftY(Integer.parseInt(attributes.get("uly")));
            boundingBox.setLowerRightX(Integer.parseInt(attributes.get("lrx")));
            boundingBox.setLowerRightY(Integer.parseInt(attributes.get("lry")));
            return boundingBox;
        }
        catch (Exception ignore) {
            //reading bbox-s for element is only 'potential' option
        }

        return null;
    }
}

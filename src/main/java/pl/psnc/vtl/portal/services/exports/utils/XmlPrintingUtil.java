package pl.psnc.vtl.portal.services.exports.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import pl.psnc.vtl.portal.services.exports.tei.TeiElementComposerUtil;

import java.io.StringWriter;
import java.util.Map.Entry;

@UtilityClass
@Slf4j
public class XmlPrintingUtil {

    private Namespace teiNamespace = new Namespace("", "http://www.tei-c.org/ns/1.0");

    public String prettyPrintHocr(String hocr) {
        try {
            for (String tag : TeiElementComposerUtil.HTML_TO_TEI_HI_REND.keySet()) {
                hocr = hocr.replace("<" + tag + ">", "&lt;" + tag + "&gt;");
                hocr = hocr.replace("</" + tag + ">", "&lt;/" + tag + "&gt;");
            }
            hocr = prettyPrint(DocumentHelper.parseText(hocr), false);
            for (String tag : TeiElementComposerUtil.HTML_TO_TEI_HI_REND.keySet()) {
                hocr = hocr.replace("&lt;" + tag + "&gt;", "<" + tag + ">");
                hocr = hocr.replace("&lt;/" + tag + "&gt;", "</" + tag + ">");
            }
            return hocr;
        } catch (DocumentException e) {
            log.error("Error parsing hocr:\n" + hocr, e);
            return hocr;
        }
    }

    public String prettyPrintTei(Document document) {
        applyNamespace(document.getRootElement(), teiNamespace);
        String tei = prettyPrint(document, true);
        for (Entry<String, String> e : TeiElementComposerUtil.HTML_TO_TEI_HI_REND.entrySet()) {
            tei = tei.replace("&lt;" + e.getKey() + "&gt;", "<hi rend=\"" + e.getValue() + "\">");
            tei = tei.replace("&lt;/" + e.getKey() + "&gt;", "</hi>");
        }
        return tei;
    }

    private String prettyPrint(Document document, boolean html) {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setAttributeQuoteCharacter('"');
        try (StringWriter sw = new StringWriter()) {
            XMLWriter writer = html ? new HTMLWriter(sw, format) : new XMLWriter(sw, format);
            writer.write(document);
            return sw.toString();
        } catch (Exception e) {
            log.error("Error pretty printing xml:\n" + document.asXML(), e);
            return document.asXML();
        }
    }

    private void applyNamespace(Element element, Namespace namespace) {
        element.setQName(QName.get(element.getName(), namespace));
        for (Element child : element.elements()) {
            applyNamespace(child, namespace);
        }
    }
}

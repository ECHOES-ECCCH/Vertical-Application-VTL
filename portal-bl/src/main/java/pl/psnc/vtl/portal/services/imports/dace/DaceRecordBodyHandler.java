package pl.psnc.vtl.portal.services.imports.dace;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class DaceRecordBodyHandler {
    private static final XPathExpression titleExpression;
    private static final XPathExpression alternativeTitleExpression;
    private static final XPathExpression descriptionExpression;
    private static final XPathExpression subjectExpression;
    private static final XPathExpression dateExpression;
    private static final XPathExpression typeExpression;
    private static final XPathExpression contentUrlsExpression;


    private static final String XPATH_DC_TITLE = "/*[name()='dace:bsa']//*[name()='dc:title']";
    private static final String XPATH_TERMS_ALTERNATIVE = "/*[name()='dace:bsa']//*[name()='terms:alternative']";
    private static final String XPATH_DESCRIPTION = "/*[name()='dace:bsa']//*[name()='dc:description']";
    private static final String XPATH_SUBJECT = "/*[name()='dace:bsa']//*[name()='dc:subject']";
    private static final String XPATH_DATE = "/*[name()='dace:bsa']//*[name()='dc:date']";
    private static final String XPATH_TYPE = "/*[name()='dace:bsa']//*[name()='dc:type']";
    private static final String CONTENT_URLS_TYPE = "/*[name()='dace:bsa']//*[name()='dace:contentUrl']";

    static {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            titleExpression = factory.newXPath().compile(XPATH_DC_TITLE);
            alternativeTitleExpression = factory.newXPath().compile(XPATH_TERMS_ALTERNATIVE);
            descriptionExpression = factory.newXPath().compile(XPATH_DESCRIPTION);
            subjectExpression = factory.newXPath().compile(XPATH_SUBJECT);
            dateExpression = factory.newXPath().compile(XPATH_DATE);
            typeExpression = factory.newXPath().compile(XPATH_TYPE);
            contentUrlsExpression = factory.newXPath().compile(CONTENT_URLS_TYPE);
        } catch (XPathExpressionException e) {
            log.error("Something is wrong with XPath expressions!", e);
            throw new RuntimeException(e);
        }
    }

    private final Document xmlRecordBody;

    public DaceRecordBodyHandler(String recordBody) {
        try {
            xmlRecordBody = parseXML(recordBody);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Cannot instantiate document builder to parse XML", e);
        }
    }

    private Document parseXML(String recordBody) throws ParserConfigurationException {
        try (StringReader xmlBodyReader = new StringReader(recordBody)) {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(xmlBodyReader));
        } catch (SAXException | IOException e) {
            throw new IllegalArgumentException(String.format("Cannot read XML: %s", recordBody));
        }
    }

    public Optional<String> getTitle() {
        Optional<String> title = findFirstString(xmlRecordBody, titleExpression);

        if (title.isPresent()) {
            return title;
        }
        return findFirstString(xmlRecordBody, alternativeTitleExpression);
    }

    public List<String> getDescriptions() {
        return findStrings(xmlRecordBody, descriptionExpression);
    }

    public List<String> getKeywords() {
        return findStrings(xmlRecordBody, subjectExpression).stream()
                .filter(s -> !Strings.isNullOrEmpty(s)).collect(Collectors.toList());
    }

    public Optional<String> getDate() {
        return findFirstString(xmlRecordBody, dateExpression);
    }

    public List<String> getTypes() {
        return findStrings(xmlRecordBody, typeExpression);
    }

    public List<String> getContentUrls() {
        return findStrings(xmlRecordBody, contentUrlsExpression);
    }

    private static Optional<String> findFirstString(Document xmlRecordBody, XPathExpression xPathExpression) {
        List<String> allElements = findStrings(xmlRecordBody, xPathExpression);
        return (allElements.isEmpty() || Strings.isNullOrEmpty(allElements.get(0))) ? Optional.empty() : Optional.of(
                allElements.get(0));
    }

    private static List<String> findStrings(Document xmlRecordBody, XPathExpression xPathExpression) {
        List<String> result = new ArrayList<>();
        try {
            NodeList nodes = (NodeList) xPathExpression.evaluate(xmlRecordBody, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getTextContent());
            }
        } catch (XPathExpressionException e) {
            log.info("Could not find element in the record based on expression: {}", xPathExpression, e);
        }
        return result;
    }
}

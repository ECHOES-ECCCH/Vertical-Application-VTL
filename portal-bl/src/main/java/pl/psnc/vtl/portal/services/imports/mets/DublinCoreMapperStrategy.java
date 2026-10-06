package pl.psnc.vtl.portal.services.imports.mets;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.MapperStrategy;
import pl.psnc.vtl.portal.services.imports.TextFormatValidator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;

public class DublinCoreMapperStrategy implements MapperStrategy {

    @Override
    public TranscriptionCore mapToImportTemplate(String content){
        try {
            InputSource inputSource = new InputSource(new StringReader(content));
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            DublinCoreSAXHandler dublinCoreSAXHandler = new DublinCoreSAXHandler();
            xmlReader.setContentHandler(dublinCoreSAXHandler);
            xmlReader.parse(inputSource);
            return dublinCoreSAXHandler.transcriptionCore;
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw TextFormatValidator.getMalformedXMLException();
        }

    }


}

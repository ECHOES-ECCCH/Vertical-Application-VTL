package pl.psnc.vtl.portal.services.imports.tei;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import pl.psnc.vtl.portal.dto.transcriptions.AnalyticInfoCore;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.services.imports.MapperStrategy;
import pl.psnc.vtl.portal.services.imports.TextFormatValidator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;


public class TeiMapperStrategy implements MapperStrategy {

    @Override
    public TranscriptionCore mapToImportTemplate(String content) {
        try {
            TeiSAXHandler saxHandler = new TeiSAXHandler();
            InputSource inputSource = new InputSource(new StringReader(content));
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(saxHandler);
            xmlReader.parse(inputSource);

            TranscriptionCore transcriptionCore = saxHandler.getTranscriptionCore();
            if (transcriptionCore.getAnalyticInfo().getTitle() == null) {
                transcriptionCore.getAnalyticInfo().setTitle(transcriptionCore.getMonographicInfo().getTitle());
            }

            return transcriptionCore;
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw TextFormatValidator.getMalformedXMLException();
        } catch (Exception e) {
            throw TextFormatValidator.getWrongTypeXMLException();
        }
    }
}

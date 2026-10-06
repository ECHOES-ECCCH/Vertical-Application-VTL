package pl.psnc.vtl.portal.services.imports.tei;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import pl.psnc.vtl.portal.services.imports.LoadedFile;
import pl.psnc.vtl.portal.services.imports.LinkMapperStrategy;
import pl.psnc.vtl.portal.services.imports.TextFormatValidator;
import pl.psnc.vtl.portal.services.imports.PublicationLinks;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;

public class TeiLinkMapperStrategy implements LinkMapperStrategy {

    @Override
    public PublicationLinks getTranscriptionLinks(LoadedFile loadedFile) {
        try {
            InputSource inputSource = new InputSource( new StringReader(loadedFile.getFileContent()));
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            TeiLinkSAXHandler teiLinkSAXHandler = new TeiLinkSAXHandler();
            teiLinkSAXHandler.setRepositoryUrl(loadedFile.getResourceUrl());
            xmlReader.setContentHandler(teiLinkSAXHandler);
            xmlReader.parse(inputSource);
            return teiLinkSAXHandler.getLinks();
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw TextFormatValidator.getMalformedXMLException();
        }
    }
}

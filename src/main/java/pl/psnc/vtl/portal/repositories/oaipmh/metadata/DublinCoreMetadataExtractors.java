package pl.psnc.vtl.portal.repositories.oaipmh.metadata;

import org.apache.logging.log4j.util.Strings;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.oaipmh.OaiItemUtils;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.extractors.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;

public enum DublinCoreMetadataExtractors {
    TITLE("title", new TitleExtractor()),
    CREATOR("creator", new FunctionBasedExtractor(t -> List.of())),
    SUBJECT("subject", new SubjectExtractor()),
    DESCRIPTION("description", new DescriptionExtractor()),
    PUBLISHER("publisher", new ConfigurationBasedExtractor("publisher")),
    CONTRIBUTOR("contributor", new FunctionBasedExtractor(t -> List.of())),
    DATE("date", new DateExtractor()),
    TYPE("type", new ConfigurationBasedExtractor("type")),
    FORMAT("format", new FunctionBasedExtractor(t -> List.of())),
    IDENTIFIER("identifier",
            new FunctionBasedExtractor(t -> List.of(new DcValue(OaiItemUtils.buildOaiId(t.getId()), Strings.EMPTY)))),
    SOURCE("source", new SourceExtractor()),
    LANGUAGE("language", new LanguageExtractor()),
    RELATION("relation", new FunctionBasedExtractor(t -> List.of())),
    COVERAGE("coverage", new FunctionBasedExtractor(t -> List.of())),
    RIGHTS("rights", new ConfigurationBasedExtractor("rights"));

    private final String localName;
    private final ValuesExtractor valuesExtractor;

    DublinCoreMetadataExtractors(String localName, ValuesExtractor valuesExtractor) {
        this.localName = localName;
        this.valuesExtractor = valuesExtractor;
    }

    public void write(XMLStreamWriter writer, Transcription transcription) throws XMLStreamException {
        List<DcValue> dcValues = valuesExtractor.extractValues(transcription);
        for (DcValue dcValue : dcValues) {
            if (Strings.isNotBlank(dcValue.getValue())) {
                writer.writeStartElement("http://purl.org/dc/elements/1.1/", localName);
                if (Strings.isNotBlank(dcValue.getXmlLanguage())) {
                    writer.writeAttribute("xml:lang", dcValue.getXmlLanguage());
                }
                writer.writeCData(dcValue.getValue());
                writer.writeEndElement();
            }
        }
    }
}

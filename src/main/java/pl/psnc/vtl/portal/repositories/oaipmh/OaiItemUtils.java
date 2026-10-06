package pl.psnc.vtl.portal.repositories.oaipmh;

import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLOutputFactory2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.conf.oaipmh.OaiPmhMetadataConfiguration;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.DcValue;
import pl.psnc.vtl.portal.repositories.oaipmh.metadata.DublinCoreMetadataExtractors;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OaiItemUtils {
    private static final XMLOutputFactory factory = XMLOutputFactory2.newFactory();
    private static final String OAI_ID_FORMAT = "oai:%s:%d";
    private static String OAI_DOMAIN;
    private static OaiPmhMetadataConfiguration configuration;

    public static String buildOaiId(Long transcriptionId) {
        return String.format(OAI_ID_FORMAT, OAI_DOMAIN, transcriptionId);
    }
    public static Long getTranscriptionIdFromOaiId(String oaiId) {
        try {
            String transcriptionId = oaiId.split(":")[2];
            return Long.parseLong(transcriptionId);
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Cannot determine local identifier from given oaiId: %s", oaiId), e);
        }
    }
    public static String convertToOaiDc(Transcription transcription) {
        StringWriter out = new StringWriter();
        try {
            XMLStreamWriter writer = factory.createXMLStreamWriter(out);
            writer.writeStartElement("oai_dc:dc");
            writer.writeNamespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
            writer.writeNamespace("dc", "http://purl.org/dc/elements/1.1/");
            writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            writer.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
                    "http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
            for (DublinCoreMetadataExtractors extractor : DublinCoreMetadataExtractors.values()) {
                extractor.write(writer, transcription);
            }
            writer.writeEndElement();
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Could not create OaiDc metadata!", e);
        }
        return out.toString();
    }
    @Value("${oai-repository.identifier.domain}")
    public void setOaiDomain(String oaiDomain) {
        OaiItemUtils.OAI_DOMAIN = oaiDomain;
    }
    public static String mapToXmlLang(String langCode) {
        return configuration.mapToXmlLang(langCode);
    }
    public static List<DcValue> getDcFieldConfiguration(String fieldName) {
        Map<String, String> xmlLangToValueMapForField = configuration.getXmlLangToValueMapForField(fieldName);
        return Objects.requireNonNullElseGet(xmlLangToValueMapForField.entrySet().stream()
                .map(entry -> new DcValue(entry.getValue(), entry.getKey())).collect(Collectors.toList()), List::of);
    }
    @Autowired
    public void setConfiguration(OaiPmhMetadataConfiguration conf) {
        configuration = conf;
    }
}

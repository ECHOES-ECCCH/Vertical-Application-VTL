package pl.psnc.vtl.portal.services.transcriptions.content;

import lombok.AllArgsConstructor;
import net.sf.saxon.TransformerFactoryImpl;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.services.exports.utils.XmlPrintingUtil;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

@Service
@AllArgsConstructor
public class AltoTranscriptionContentGenerator implements TranscriptionContentGenerator {

    private final HOCRTranscriptionContentGenerator hOCRTranscriptionContentGenerator;

    public static final Templates templates;

    static {
        try {
            TransformerFactory factory = new TransformerFactoryImpl();
            factory.setAttribute("http://saxon.sf.net/feature/version-warning", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

            Source xsltSource = new StreamSource(new ClassPathResource("hocr__alto.xsl").getInputStream());
            templates = factory.newTemplates(xsltSource);
        } catch (IOException | TransformerConfigurationException e) {
            throw new RuntimeException("Failed to initialize XSLT templates", e);
        }
    }

    @Override
    public boolean supports(TranscriptionPageContentType transcriptionPageContentType) {
        return TranscriptionPageContentType.ALTO == transcriptionPageContentType;
    }

    @Override
    public String generate(Transcription transcription, Layer layer) {
        String hocr = hOCRTranscriptionContentGenerator.generate(transcription, layer);

        StringWriter altoWriter = new StringWriter();
        try {
            templates.newTransformer().transform(
                            new StreamSource(new StringReader(hocr)),
                            new StreamResult(altoWriter));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }

        return XmlPrintingUtil.prettyPrintHocr(altoWriter.toString());
    }

}

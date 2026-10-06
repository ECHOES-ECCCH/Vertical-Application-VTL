package pl.psnc.vtl.portal.services.pages.content;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.services.exports.utils.XmlPrintingUtil;
import pl.psnc.vtl.portal.services.transcriptions.content.AltoTranscriptionContentGenerator;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AltoTranscriptionPageContentGenerator implements TranscriptionPageContentGenerator {

    private final HOCRTranscriptionPageContentGenerator hOCRTranscriptionPageContentGenerator;

    @Override
    public boolean supports(TranscriptionPageContentType transcriptionPageContentType) {
        return TranscriptionPageContentType.ALTO == transcriptionPageContentType;
    }

    @Override
    public List<ImmutablePair<Layer, String>> generate(Page page, List<Layer> layer) throws IOException {
        List<ImmutablePair<Layer, String>> hocrs = hOCRTranscriptionPageContentGenerator.generate(page, layer);
        List<ImmutablePair<Layer, String>> result = new ArrayList<>(hocrs.size());

        try {
            Transformer transformer = AltoTranscriptionContentGenerator.templates.newTransformer();
            for (ImmutablePair<Layer, String> pair : hocrs) {
                String hocr = pair.getValue();
                StringWriter altoWriter = new StringWriter();
                transformer.transform(
                        new StreamSource(new StringReader(hocr)),
                        new StreamResult(altoWriter));
                result.add(new ImmutablePair<>(pair.getKey(), XmlPrintingUtil.prettyPrintHocr(altoWriter.toString())));
            }
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

}

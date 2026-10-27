package pl.psnc.vtl.portal.services.transcriptions.content;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.services.exports.mappers.HOCRMapper;
import pl.psnc.vtl.portal.services.exports.utils.XmlPrintingUtil;
import pl.psnc.vtl.portal.services.pages.content.HOCRTranscriptionPageContentGenerator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class HOCRTranscriptionContentGenerator implements TranscriptionContentGenerator {

    private  HOCRTranscriptionPageContentGenerator hocrTranscriptionPageContentGenerator;

    private  PageRepository pageRepository;

    @Override
    public boolean supports(TranscriptionPageContentType transcriptionPageContentType) {
        return TranscriptionPageContentType.HOCR == transcriptionPageContentType;
    }

    @Override
    public String generate(Transcription transcription, Layer layer) {
        List<Page> pages = pageRepository.findByTranscriptionIdOrderByPageNo(transcription.getId());
        StringBuilder result = new StringBuilder();
        String body = pages.stream().map((Page page) -> hocrTranscriptionPageContentGenerator.getHOCRBody(page, layer))
                .collect(Collectors.joining());
        result.append(HOCRMapper.getHead());
        result.append("<body>\n").append(body).append("</body>\n");
        result.append("</html>");
        return XmlPrintingUtil.prettyPrintHocr(result.toString());
    }


}

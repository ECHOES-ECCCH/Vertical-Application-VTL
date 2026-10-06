package pl.psnc.vtl.portal.services.eBook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsRepository;
import pl.psnc.vtl.portal.repositories.layers.LayerRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;

import javax.persistence.EntityNotFoundException;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobiConverterService implements EBookExporter {

    private final IIIFToolsRepository iiifToolsRepository;

    private final TranscriptionRepository transcriptionRepository;
    private final LayerRepository layerRepository;

    private final EbooksDataSupplier ebooksDataSupplier;


    @Override
    public void exportEbook(EbookDto edto, String hash) {
        try {
            Transcription transcription = transcriptionRepository.findById(edto.getTranscriptionId())
                    .orElseThrow(() -> new EntityNotFoundException("Unable to find transcrition"));
            Layer layer = layerRepository.findById(edto.getLayerId())
                    .orElseThrow(() -> new EntityNotFoundException("Unable to find layer"));

            ImmutablePair<List<MultipartFile>, HashMap<String, String>> htmlData = ebooksDataSupplier
                    .getHtmlFiles(transcription, layer, edto.getIndexLanguage());
            List<MultipartFile> htmls = htmlData.getLeft();
            HashMap<String, String> pagesIiifUrls = htmlData.getRight();
            iiifToolsRepository
                    .saveEbook(edto.getTranscriptionId(), hash, htmls, ebooksDataSupplier.getTitle(transcription),
                            ebooksDataSupplier.getAuthor(transcription), Ebook.MOBI, pagesIiifUrls, null);
        } catch (Exception e) {
            log.error("Error during mobi conversion", e);
            throw new ConversionException(hash);
        }
    }
}

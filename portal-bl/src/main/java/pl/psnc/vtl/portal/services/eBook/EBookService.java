package pl.psnc.vtl.portal.services.eBook;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.iiiftools.ExportedEBook;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;

import javax.persistence.EntityNotFoundException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EBookService {

    private final EBookStorageAware eBookStorageAware;

    private final TranscriptionRepository transcriptionRepository;

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final PdfExporter pdfExporter;
    private final EpubConverterService epubConverterService;
    private final MobiConverterService mobiConverterService;

    @Value("${vtl.iiif-server-public-url}")
    private String iiifServerUrl;

    private final Map<String, EbookDto> ebookDtosByHash = new ConcurrentHashMap<>();

    public AsyncTaskResponse getEBookLocation(String hash) {
        EbookDto ebookDto = ebookDtosByHash.get(hash);
        if (ebookDto == null)
            return new AsyncTaskResponse(Status.NOT_EXISTS, null);
        transcriptionPermissionService.checkReadAccess(ebookDto.getTranscriptionId());
        ExportedEBook ebook = eBookStorageAware.getEBookLocation(hash, ebookDto);
        String details = ebook.getDetails();
        if (ebook.getStatus() == Status.READY)
            details = iiifServerUrl + details;
        return new AsyncTaskResponse(ebook.getStatus(), details);
    }

    public String addExportProcess(EbookDto ebookDto) {
        transcriptionPermissionService.checkReadAccess(ebookDto.getTranscriptionId());
        String hash = getHash(ebookDto);
        ebookDtosByHash.put(hash, ebookDto);
        Status status = getEBookLocation(hash).getStatus();
        if (status == Status.READY || status == Status.PROCESSING)
            return hash;
        getEBookExporter(ebookDto.getFileFormat()).exportEbook(ebookDto, hash);
        return hash;
    }

    private EBookExporter getEBookExporter(Ebook format) {
        switch (format) {
        case PDF:
            return pdfExporter;
        case EPUB:
            return epubConverterService;
        case MOBI:
            return mobiConverterService;
        default:
            throw new RuntimeException("Unrecognized ebook format: " + format);

        }
    }

    private String getHash(EbookDto eb) {
        Transcription transcription = transcriptionRepository.findById(eb.getTranscriptionId())
                .orElseThrow(EntityNotFoundException::new);
        return String.valueOf(Math.abs(Objects.hash(transcription.getId(), transcription.getLastModificationDate(),
                eb.getLayerId(), eb.isCreateTitlePage(), eb.getFileFormat().toString())));
    }
}

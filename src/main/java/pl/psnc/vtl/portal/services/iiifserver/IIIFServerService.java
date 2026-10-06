package pl.psnc.vtl.portal.services.iiifserver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.repositories.iiifserver.IIIFServerRepository;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;

@Service
@RequiredArgsConstructor
@Slf4j
public class IIIFServerService {

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final IIIFUriResolver iiifUriResolver;

    private final IIIFServerRepository iiifServerRepository;

    private final PageRepository pageRepository;

    public String infoJsonProvider(long pageId) {
        Page page = pageRepository.getOne(pageId);
        transcriptionPermissionService.checkReadAccess(page.getTranscription().getId());
        return iiifServerRepository.iiifServerInfoJsonProvider(iiifUriResolver.getInternalInfoJsonUri(page));
    }

}

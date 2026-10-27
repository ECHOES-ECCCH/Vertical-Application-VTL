package pl.psnc.vtl.portal.services.transcriptions;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.transcriptions.list.TranscriptionInfoForDaceDto;
import pl.psnc.vtl.portal.model.transcriptions.projections.TranscriptionInfoForDaceProjection;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final TranscriptionRepository transcriptionRepository;
    private final TranscriptionPermissionService transcriptionPermissionService;

    public List<TranscriptionInfoForDaceDto> getProjects(String externalId) {
        List<TranscriptionInfoForDaceProjection> tInfos = transcriptionRepository.findAllByExternalIdAndHidden(externalId,
                false);
        tInfos.forEach(t -> transcriptionPermissionService.checkReadAccess(t.getId()));
        return tInfos.stream().map(TranscriptionInfoForDaceProjection::toTranscriptionInfoDto).collect(
                Collectors.toList());
    }


}

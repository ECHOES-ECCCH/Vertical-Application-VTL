package pl.psnc.vtl.portal.services.domains;

import lombok.RequiredArgsConstructor;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.imports.ImportTranscriptionDTO;
import pl.psnc.vtl.portal.model.domains.Domain;
import pl.psnc.vtl.portal.model.imports.FileType;
import pl.psnc.vtl.portal.repositories.domains.DomainRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DomainService {

    private final DomainRepository domainRepository;

    @Value("${export.external-editor-for-domains}")
    private String externalEditorForDomains;

    public void validateDomain(ImportTranscriptionDTO importTranscriptionDTO) throws ConfigurationException {
        validateDomain(importTranscriptionDTO.getResourceUrl(), importTranscriptionDTO.getFileType());
        if (StringUtils.isNotBlank(importTranscriptionDTO.getMetadataUrl()))
            validateDomain(importTranscriptionDTO.getMetadataUrl(), importTranscriptionDTO.getMetadataType());
    }

    public void validateDomain(String resourceUrl, FileType fileType)
            throws ConfigurationException {
        List<String> allowedDomains = domainRepository.getAllowedDomains().stream().map(Domain::getValue).collect(Collectors.toList());
        for (String domain: allowedDomains) {
            if (resourceUrl.contains(domain)) {
                return;
            }
        }
        throw new DomainNotAvailableException("Domain not available", fileType.getValue());
    }

    public void validateExternalEditorDomain(ImportTranscriptionDTO importTranscriptionDTO) throws DomainNotAvailableForExternalEditException {
        for (String domain: externalEditorForDomains.split(",")) {
            if (importTranscriptionDTO.getExternalId().contains(domain)) {
                return;
            }
        }
        throw new DomainNotAvailableForExternalEditException("Domain not available as source for external edit.");
    }

    public List<String> getDomains(String substring, FileType fileType)
            throws ConfigurationException {
        List<Domain> allowedDomains = domainRepository.getAllowedDomains();
        if (substring != null && fileType != null) {
            return allowedDomains.stream()
                    .filter(d -> d.getType().equals(fileType))
                    .filter(d -> d.getValue().contains(substring.toLowerCase()))
                    .map(Domain::getValue)
                    .collect(Collectors.toList());
        }

        if (fileType != null) {
            return allowedDomains.stream()
                    .filter(d -> d.getType().equals(fileType))
                    .map(Domain::getValue)
                    .collect(Collectors.toList());
        }

        if (substring != null) {
            return allowedDomains.stream()
                    .filter(d -> d.getValue().contains(substring.toLowerCase()))
                    .map(Domain::getValue)
                    .collect(Collectors.toList());
        }
        return allowedDomains.stream().map(Domain::getValue).collect(Collectors.toList());
    }
}

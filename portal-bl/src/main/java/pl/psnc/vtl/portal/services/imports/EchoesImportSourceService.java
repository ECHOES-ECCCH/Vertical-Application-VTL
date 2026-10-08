package pl.psnc.vtl.portal.services.imports;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.repositories.echoes.EchoesPlatformRepository;

@Service
@RequiredArgsConstructor
public class EchoesImportSourceService {
    private static final String PROJECT_PREFIX = "echoes:asset:";

    private final EchoesPlatformRepository echoesPlatformRepository;

    public LoadedFile loadAsset(String assetId, String fallbackUrl) {
        LoadedFile source = echoesPlatformRepository.loadAsset(assetId);
        if (source.getResourceUrl() == null)
            source.setResourceUrl(fallbackUrl);
        return source;
    }

    public String getProjectId(String assetId) {
        return PROJECT_PREFIX + assetId;
    }

    public String getAssetId(String projectId) {
        if (StringUtils.isBlank(projectId) || !projectId.startsWith(PROJECT_PREFIX))
            return null;
        return projectId.substring(PROJECT_PREFIX.length());
    }
}

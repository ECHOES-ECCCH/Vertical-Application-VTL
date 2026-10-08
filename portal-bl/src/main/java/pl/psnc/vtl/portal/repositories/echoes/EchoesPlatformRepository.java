package pl.psnc.vtl.portal.repositories.echoes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.util.StreamUtils;
import java.io.OutputStream;
import org.springframework.web.util.UriComponentsBuilder;
import pl.psnc.vtl.portal.repositories.imports.DocumentsLoaderRepository;
import pl.psnc.vtl.portal.security.EchoesAccessTokenProvider;
import pl.psnc.vtl.portal.services.imports.LoadedFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Repository
@RequiredArgsConstructor
public class EchoesPlatformRepository {

    private static final String MANAGED = "MANAGED";
    private static final String REFERENCED = "REFERENCED";

    private final EchoesAccessTokenProvider accessTokenProvider;
    private final DocumentsLoaderRepository documentsLoaderRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final DiskFileItemFactory binaryFileFactory = new DiskFileItemFactory(0, null);

    @Value("${vtl.echoes.common-platform-api-url:}")
    private String commonPlatformApiUrl;

    public LoadedFile loadAsset(String assetId) {
        validateAssetId(assetId);
        EchoesAsset asset = getAsset(assetId);
        if (REFERENCED.equals(asset.getManagementMode())) {
            if (StringUtils.isBlank(asset.getExternalUri())) {
                throw new IllegalStateException("Referenced ECHOES asset does not expose externalUri: " + assetId);
            }
            String content = documentsLoaderRepository.loadDocumentStringFromResource(asset.getExternalUri());
            return new LoadedFile(asset.getExternalUri(), content);
        }

        if (MANAGED.equals(asset.getManagementMode())) {
            byte[] content = getManagedAssetContent(assetId);
            return new LoadedFile(null, new String(content, StandardCharsets.UTF_8));
        }
        throw new IllegalStateException("Unsupported ECHOES asset management mode: " + asset.getManagementMode());
    }

    /** Import a disk-backed upload synchronously */
    public void loadBinaryAsset(String assetId, BinaryAssetConsumer consumer) {
        validateAssetId(assetId);
        EchoesAsset asset = getAsset(assetId);
        boolean managed = MANAGED.equals(asset.getManagementMode());
        String contentUrl;
        if (REFERENCED.equals(asset.getManagementMode())) {
            if (StringUtils.isBlank(asset.getExternalUri())) {
                throw new IllegalStateException("Referenced ECHOES asset does not expose externalUri: " + assetId);
            }
            contentUrl = asset.getExternalUri();
        } else if (managed) {
            contentUrl = assetUrl(assetId) + "/content";
        } else {
            throw new IllegalStateException("Unsupported ECHOES asset management mode: " + asset.getManagementMode());
        }

        String fileName = StringUtils.defaultIfBlank(asset.getFileName(),
                managed ? null : fileNameFromUri(contentUrl));
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalStateException("ECHOES asset does not provide a usable file name: " + assetId);
        }
        restTemplate.execute(contentUrl, HttpMethod.GET, request -> {
            if (managed) request.getHeaders().setBearerAuth(accessTokenProvider.getAccessToken());
        }, response -> {
            FileItem file = binaryFileFactory.createItem("file", "application/octet-stream", false, fileName);
            try {
                try (OutputStream output = file.getOutputStream()) {
                    StreamUtils.copy(response.getBody(), output);
                }
                consumer.accept(new CommonsMultipartFile(file));
                return null;
            } finally {
                file.delete();
            }
        });
    }

    @FunctionalInterface
    public interface BinaryAssetConsumer {
        void accept(MultipartFile file) throws java.io.IOException;
    }

    private EchoesAsset getAsset(String assetId) {
        ResponseEntity<EchoesAsset> response = restTemplate.exchange(
                assetUrl(assetId), HttpMethod.GET, authorizedRequest(), EchoesAsset.class);
        EchoesAsset asset = response.getBody();
        if (asset == null) {
            throw new IllegalStateException("ECHOES returned empty asset metadata for: " + assetId);
        }
        return asset;
    }

    private byte[] getManagedAssetContent(String assetId) {
        ResponseEntity<byte[]> response = restTemplate.exchange(
                assetUrl(assetId) + "/content", HttpMethod.GET, authorizedRequest(), byte[].class);
        byte[] content = response.getBody();
        if (content == null) {
            throw new IllegalStateException("ECHOES returned empty asset content for: " + assetId);
        }
        return content;
    }

    private HttpEntity<Void> authorizedRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessTokenProvider.getAccessToken());
        return new HttpEntity<>(headers);
    }

    private String assetUrl(String assetId) {
        return UriComponentsBuilder.fromHttpUrl(commonPlatformApiUrl)
                .pathSegment("platform", "v1", "assets", assetId)
                .build()
                .encode()
                .toUriString();
    }

    private void validateAssetId(String assetId) {
        requireConfigured();
        if (StringUtils.isBlank(assetId)) {
            throw new IllegalArgumentException("ECHOES assetId must not be blank");
        }
    }

    private String fileNameFromUri(String uri) {
        if (StringUtils.isBlank(uri)) {
            return null;
        }
        String path = URI.create(uri).getPath();
        if (StringUtils.isBlank(path)) {
            return null;
        }
        int separator = path.lastIndexOf('/');
        return separator >= 0 ? path.substring(separator + 1) : path;
    }

    private void requireConfigured() {
        if (StringUtils.isBlank(commonPlatformApiUrl)) {
            throw new IllegalStateException("ECHOES Common Platform API URL is not configured");
        }
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EchoesAsset {
        private String managementMode;
        private String externalUri;
        private String fileName;
    }
}

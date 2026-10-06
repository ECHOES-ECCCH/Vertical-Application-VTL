package pl.psnc.vtl.portal.services.iiifserver;

import com.jayway.jsonpath.DocumentContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.repositories.imports.DocumentsLoaderRepository;
import pl.psnc.vtl.portal.utils.IIIFUtils;

import javax.annotation.PostConstruct;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Component
@RequiredArgsConstructor
public class IIIFUriResolver {

    @Value("${vtl.iiif-server-url}")
    private String iiifServerUrl;

    @Value("${vtl.iiif-server-public-url}")
    private String iiifServerPublicUrl;

    @Value("${vtl.iiif-server-pct}")
    private Float iiifServerPct;

    @Value("${vtl.thumbnail.width}")
    private Integer thumbnailWidth;

    @Value("${vtl.thumbnail.height}")
    private Integer thumbnailHeight;

    @Value("${vtl.training-example.image-padding}")
    private int trainingImagePadding;

    private final DocumentsLoaderRepository documentsLoaderRepository;

    private static final String IIIF_MODULE_URL = "iipsrv/iipsrv.fcgi/?IIIF=t";

    private static final String FULL_IMAGE_EXPRESSION = "/full/full/0/default.jpg";

    private static final String COMPRESSED_IMAGE_EXPRESSION = "/full/pct:%.1f/0/default.jpg";

    private static final String THUMBNAIL_REGION_EXPRESSION = "/pct:0,0,%.1f,%.1f";

    private static final String THUMBNAIL_SIZE_EXPRESSION = "/!%d,%d";

    private static final String THUMBNAIL_SIZE_UPSCALE_EXPRESSION = "/^!%d,%d";

    private String thumbnailSizeExpression;

    private String thumbnailSizeUpscaleExpression;

    private String compressedImageExpression;

    private Map<Long, ImageInfo> pageIdToImageInfo = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        this.thumbnailSizeExpression = String.format(THUMBNAIL_SIZE_EXPRESSION, thumbnailWidth, thumbnailHeight);
        this.thumbnailSizeUpscaleExpression = String.format(THUMBNAIL_SIZE_UPSCALE_EXPRESSION, thumbnailWidth, thumbnailHeight);
        this.compressedImageExpression = String.format(COMPRESSED_IMAGE_EXPRESSION, iiifServerPct);
    }

    public URI getInternalInfoJsonUri(Page page) {
        return getInfoJsonUri(page, iiifServerUrl);
    }

    public URI getPublicInfoJsonUri(Page page) {
        return getInfoJsonUri(page, iiifServerPublicUrl);
    }

    private URI getInfoJsonUri(Page page, String iiifServerUrl) {
        URI uri = URI.create(page.getIiifUrl());
        if (!uri.isAbsolute()) {
            return URI.create(iiifServerUrl + IIIF_MODULE_URL + page.getIiifUrl());
        }
        return uri;
    }

    public String getPublicImageServiceUrl(Page page) {
        URI infoJsonUri = getPublicInfoJsonUri(page);
        return infoJsonUri.toString().replace("/info.json", "");
    }

    public String createPublicImageUrl(long transcriptionId, String pyramidalFilename) {
        StringBuilder iiifImageUrl = new StringBuilder();
        iiifImageUrl.append(iiifServerPublicUrl).append(IIIF_MODULE_URL)
                .append(transcriptionId).append("/").append(pyramidalFilename).append(FULL_IMAGE_EXPRESSION);
        return iiifImageUrl.toString();
    }

    public String getCompressedImageUrl(Page page) {
        URI infoJsonUri = getInternalInfoJsonUri(page);
        StringBuilder iiifImageUrl = new StringBuilder(infoJsonUri.toString().replace("/info.json", ""));
        iiifImageUrl.append(compressedImageExpression);
        return iiifImageUrl.toString();
    }

    public String getLocalPyramidFilename(Page page) {
        URI uri = URI.create(page.getIiifUrl());
        if (!uri.isAbsolute()) {
            String relativePath = page.getIiifUrl().replace("/info.json", "");
            String[] splitPath = relativePath.split("/");
            return splitPath[splitPath.length - 1];
        } else {
            return null;
        }
    }

    public String getPublicImageUrl(Verse verse) {
        URI infoJsonUri = getPublicInfoJsonUri(verse.getPage());
        return getImageUrl(verse, infoJsonUri, 0);
    }

    public String getTrainingImageUrl(Verse verse) {
        URI infoJsonUri = getInternalInfoJsonUri(verse.getPage());
        return getImageUrl(verse, infoJsonUri, trainingImagePadding);
    }

    private String getImageUrl(Verse verse, URI infoJsonUri, int padding) {
        ImageInfo info = pageIdToImageInfo.computeIfAbsent(verse.getPage().getId(), id -> getImageInfo(infoJsonUri));
        BoundingBox vbox = verse.getBoundingBox();
        int upperLeftX = max(0, vbox.getUpperLeftX() - padding);
        int upperLeftY = max(0, vbox.getUpperLeftY() - padding);
        int lowerRightX = min(info.width, vbox.getLowerRightX() + padding);
        int lowerRightY = min(info.height, vbox.getLowerRightY() + padding);
        String iiifParams = MessageFormat.format("/{0},{1},{2},{3}/{4}/0/default.jpg",
                upperLeftX + "",
                upperLeftY + "",
                lowerRightX - upperLeftX + "",
                lowerRightY - upperLeftY + "",
                info.isV3 ? "max" : "full");
        return infoJsonUri.toString().replaceFirst("/?(info.json)?$", iiifParams);
    }

    private ImageInfo getImageInfo(URI infoJsonUri) {
        String infoJsonString = documentsLoaderRepository.loadDocumentStringFromResource(infoJsonUri.toString());
        DocumentContext infoJson = IIIFUtils.parseJson(infoJsonString);
        ImageInfo info = new ImageInfo();
        info.isV3 = IIIFUtils.isImageApiVersion3(infoJson);
        BoundingBox box = IIIFUtils.getBoundingBoxFromInfoJson(infoJsonString);
        info.width = box.getLowerRightX() - box.getUpperLeftX();
        info.height = box.getLowerRightY() - box.getUpperLeftY();
        return info;
    }

    public String getSearchServiceUrl(String origin, long transcriptionId, Long layerId) {
        if(layerId != null) {
            return URI.create(origin + "/api/iiif/transcriptions/" + transcriptionId + "/search?layerId=" + layerId).toString();
        } else {
            return URI.create(origin + "/api/iiif/transcriptions/" + transcriptionId + "/search").toString();
        }
    }

    /**
     * Returns thumbnail url for an image specified in the iiifUrl. Thumbnail is calculated based on the image width and height
     * and the thumbnail size is read from the properties file.
     *
     * @param iiifImageUrl iiif image URL (to the full image)
     * @param width        width of the image
     * @param height       height of the image
     * @return thumbnail URL in iiif server
     */
    public String getThumbnailUrl(String iiifImageUrl, int width, int height, boolean isV3) {
        String region;
        if (width == 0 || height == 0) {
            region = "/square";
        } else if (width > height) {
            region = String.format(Locale.US, THUMBNAIL_REGION_EXPRESSION, 100f * height / width, 100f);
        } else {
            region = String.format(Locale.US, THUMBNAIL_REGION_EXPRESSION, 100f, 100f * width / height);
        }
        String size;
        if ((width < thumbnailWidth || height < thumbnailHeight) && isV3) {
            size = thumbnailSizeUpscaleExpression;
        } else {
            size = thumbnailSizeExpression;
        }
        return iiifImageUrl.replace("/full/full", region + size);
    }

    public String getManifestUrl(String origin, long transcriptionId) {
        return URI.create(origin + "/api/iiif/transcriptions/" + transcriptionId + "/manifest.json").toString();
    }

    public String getComponentUrl(String origin, long transcriptionId, String componentName, long componentId) {
        return URI.create(origin + "/api/iiif/transcriptions/" + transcriptionId + "/" + componentName + "/" + componentId).toString();
    }

    public String getPageLayerUrl(String origin, long transcriptionId, long pageId, long layerId) {
        return URI.create(origin + "/api/iiif/transcriptions/" + transcriptionId + "/" + pageId + "/" + layerId).toString();
    }

    private static class ImageInfo {
        boolean isV3;
        int width;
        int height;
    }
}

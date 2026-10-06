package pl.psnc.vtl.portal.services.imports.iiif;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidJsonException;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.repositories.imports.DocumentsLoaderRepository;
import pl.psnc.vtl.portal.services.iiifserver.IIIFUriResolver;
import pl.psnc.vtl.portal.services.imports.*;
import pl.psnc.vtl.portal.utils.IIIFUtils;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IIIFLinkMapperStrategy implements LinkMapperStrategy {

    private final Map<String, String> mimeTypeToExtension = Map.of(
            "image/jpeg", "jpg",
            "image/jpg", "jpg",
            "image/tiff", "tif",
            "image/png", "png",
            "application/pdf", "pdf",
            "image/x.djvu", "djvu"
    );

    @Override
    public PublicationLinks getTranscriptionLinks(LoadedFile loadedFile) {
        DocumentContext context;
        try {
            context = IIIFUtils.parseJson(loadedFile.getFileContent());
        } catch (InvalidJsonException e) {
            throw new IllegalArgumentException(
                    "Link to the given IIIF manifest is not a proper JSON file! " + loadedFile.getResourceUrl(), e);
        }
        List<String> urls = IIIFUtils.getImagesURLsFromManifestJson(context.jsonString());

        List<Link> links = new LinkedList<>();
        for (int i = 0; i < urls.size(); i++) {
            int width = IIIFUtils.getImagePropertyInt(context, "width", i);
            int height = IIIFUtils.getImagePropertyInt(context, "height", i);

            String url = checkAndFixIIIFUrl(context, urls.get(i), i);
            String thumbnailUrl = BeanLoader.getBean(IIIFUriResolver.class)
                    .getThumbnailUrl(url, width, height, IIIFUtils.isImageApiVersion3(context));
            links.add(new Link(url,
                    width,
                    height,
                    thumbnailUrl,
                    IIIFUtils.getInfoJsonURL(context.jsonString(), i)
            ));
        }
        return new PublicationLinks(links, true);
    }

    private static final Pattern IIIF_IMAGE_URL_PATTERN = Pattern.compile("(.*)/full/([^/]+)/0/default\\.(\\w+)");

    private String checkAndFixIIIFUrl(DocumentContext context, String url, int index) {
        Matcher matcher = IIIF_IMAGE_URL_PATTERN.matcher(url);
        if (matcher.matches()) {
            String size = matcher.group(2);
            if ("full".equals(size) || "pct:100".equals(size))
                return url;
            // some services give @id url to particular size, force full size
            boolean isV3 = isV3(matcher.group(1) + "/info.json");
            return MessageFormat.format("{0}/full/{1}/0/default.{2}",
                    matcher.group(1), isV3 ? "pct:100" : "full", matcher.group(3));
        }

        boolean isV3 = isV3(url);
        String fileExtension = "jpg";
        String mimeType = IIIFUtils.getImagePropertyString(context, "format", index);
        if (mimeType != null)
            fileExtension = mimeTypeToExtension.getOrDefault(mimeType, fileExtension);

        return MessageFormat.format("{0}/full/{1}/0/default.{2}",
                url.replaceFirst("/info.json$", "").replaceFirst(".json$", ""),
                isV3 ? "pct:100" : "full",
                fileExtension);
    }

    private boolean isV3(String infoJsonUrl) {
        var documentsLoaderRepository = BeanLoader.getBean(DocumentsLoaderRepository.class);
        String infoJsonString = documentsLoaderRepository.loadDocumentStringFromResource(infoJsonUrl);
        DocumentContext infoJson = IIIFUtils.parseJson(infoJsonString);
        return IIIFUtils.isImageApiVersion3(infoJson);
    }
}

package pl.psnc.vtl.portal.services.imports;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.psnc.vtl.portal.services.imports.mets.LinkedPublicationHandler;
import pl.psnc.vtl.portal.services.imports.mets.ManifestHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;


public class LinkMappingConfiguration {

    public static final String IMAGE_X_DJVU_MIMETYPE = "image/x.djvu";
    private static final Map<String, Function<List<Link>, PublicationLinks>> publicationLinksHandlers = Map.of(
            MIMETYPE.MANIFEST.getMimeType(), new ManifestHandler(),
            MIMETYPE.LINKED_PUBLICATION.getMimeType(), new LinkedPublicationHandler()
    );
    
    @RequiredArgsConstructor
    private enum MIMETYPE{
        MANIFEST("application/json"),
        TIFF("image/tiff"),
        PDF("application/pdf"),
        JPEG("image/jpeg"),
        JPG("image/jpg"),
        LINKED_PUBLICATION("text/xml"),
        DJVU(IMAGE_X_DJVU_MIMETYPE),
        DJVU_ALTERNATIVE("image/vnd.djvu");

        @Getter
        private final String mimeType;
    }

    //types might be loaded from configurations or shared with other services(iiifTools ...)

    public static final String IFF_EXTENSION = "iff";
    public static final String DJBZ_EXTENSION = "djbz";
    public static final String THUMB_EXTENSION = "thumb";
    public  static  final Set<String> allowedFileExtensions = Set.of("tif", "tiff", "pdf", "jpg", "jpeg", "djvu", "djv",
            IFF_EXTENSION, DJBZ_EXTENSION, THUMB_EXTENSION);

    private static final Map<String, Integer> typeOrderMap = Map.of(
            MIMETYPE.MANIFEST.getMimeType(), 6,
            MIMETYPE.LINKED_PUBLICATION.getMimeType(), 5,
            MIMETYPE.DJVU.getMimeType(), 4,
            MIMETYPE.DJVU_ALTERNATIVE.getMimeType(), 4,
            MIMETYPE.TIFF.getMimeType(), 3,
            MIMETYPE.PDF.getMimeType() , 2,
            MIMETYPE.JPEG.getMimeType(), 1,
            MIMETYPE.JPG.getMimeType(),  1
    );

    private static final Set<String> consideredMimeTypes = typeOrderMap.keySet();

    public static boolean isConsideredMimeType(String mimeTYpe){
        return consideredMimeTypes.contains(mimeTYpe);
    }

    public static Integer getMimeTypeOrder(String mimeType){
        return typeOrderMap.getOrDefault(mimeType, 0);
    }

    public static boolean isValidFileExtension(String url){
        return allowedFileExtensions.stream().anyMatch(url::endsWith);
    }

    public static Function<List<Link>, PublicationLinks> getHandler(String groupMimeType) {
        return  publicationLinksHandlers.getOrDefault(groupMimeType, links -> new PublicationLinks(links, false ));
    }
}

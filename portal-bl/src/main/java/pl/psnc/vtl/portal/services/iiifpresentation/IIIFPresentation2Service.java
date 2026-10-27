package pl.psnc.vtl.portal.services.iiifpresentation;

import de.digitalcollections.iiif.model.ImageContent;
import de.digitalcollections.iiif.model.MimeType;
import de.digitalcollections.iiif.model.Motivation;
import de.digitalcollections.iiif.model.PropertyValue;
import de.digitalcollections.iiif.model.image.ImageApiProfile;
import de.digitalcollections.iiif.model.image.ImageService;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.openannotation.Annotation;
import de.digitalcollections.iiif.model.openannotation.ContentAsText;
import de.digitalcollections.iiif.model.search.ContentSearchService;
import de.digitalcollections.iiif.model.sharedcanvas.AnnotationList;
import de.digitalcollections.iiif.model.sharedcanvas.Canvas;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.iiif.model.sharedcanvas.Sequence;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.psnc.vtl.portal.dto.permissions.ViewMode;
import pl.psnc.vtl.portal.model.layers.Layer;
import pl.psnc.vtl.portal.model.layers.PageLayer;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.verses.Verse;
import pl.psnc.vtl.portal.services.iiifserver.IIIFUriResolver;
import pl.psnc.vtl.portal.services.pages.PageListService;
import pl.psnc.vtl.portal.services.pages.PageService;
import pl.psnc.vtl.portal.services.permissions.TranscriptionPermissionService;
import pl.psnc.vtl.portal.services.transcriptions.TranscriptionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class IIIFPresentation2Service {

    private final TranscriptionService transcriptionService;

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final PageListService pageListService;

    private final PageService pageService;

    private final IIIFUriResolver iiifUriResolver;

    private String origin;


    @SneakyThrows
    public String getTranscriptionManifest(Long transcriptionId, Long layerId) {
        origin = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        Transcription transcription = transcriptionService.requireTranscription(transcriptionId);
        transcriptionPermissionService.checkReadAccess(transcriptionId);

        Manifest manifest = createManifest(transcriptionId, transcription);
        manifest.setServices(createSearchService(transcriptionId, layerId));
        manifest.setSequences(createSequence(transcriptionId, layerId));

        return new IiifObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(manifest);
    }

    private Manifest createManifest(Long transcriptionId, Transcription transcription) {
        //MANIFEST = TRANSCRIPTION
        String manifestID = iiifUriResolver.getManifestUrl(origin, transcriptionId);
        String label = transcription.getTitleLabel();
        return new Manifest(manifestID, label);
    }

    private List<de.digitalcollections.iiif.model.Service> createSearchService(long transcriptionId, Long layerId) {
        ContentSearchService searchService = new ContentSearchService(iiifUriResolver.getSearchServiceUrl(origin, transcriptionId, layerId));
        return List.of(searchService);
    }

    private List<Sequence> createSequence(long transcriptionId, Long layerId) {
        Sequence sequence = new Sequence(iiifUriResolver.getComponentUrl(origin, transcriptionId, "sequence", 1));
        sequence.setCanvases(createCanvases(transcriptionId, layerId));
        return List.of(sequence);
    }

    private List<Canvas> createCanvases(long transcriptionId, Long layerId) {
        //CANVAS = PAGE
        List<Canvas> canvasList = new ArrayList<>();
        for (Page page : pageListService.getPages(transcriptionId)) {
            canvasList.add(createCanvas(transcriptionId, page, layerId));
        }
        return canvasList;
    }

    private Canvas createCanvas(long transcriptionId, Page page, Long layerId) {
        Canvas canvas = new Canvas(iiifUriResolver.getComponentUrl(origin, transcriptionId, "canvas", page.getId()));
        canvas.addImage(createImageContent(page));
        canvas.setOtherContent(createOtherContentList(transcriptionId, page, layerId));
        return canvas;
    }

    private ImageContent createImageContent(Page page) {
        ImageContent imageContent = new ImageContent(createImageService(page));
        imageContent.setFormat(MimeType.MIME_IMAGE_JPEG);
        imageContent.setWidth(page.getBoundingBox().getLowerRightX());
        imageContent.setHeight(page.getBoundingBox().getLowerRightY());
        return imageContent;
    }

    private ImageService createImageService(Page page) {
        return new ImageService(iiifUriResolver.getPublicImageServiceUrl(page), ImageApiProfile.LEVEL_TWO);
    }

    private List<AnnotationList> createOtherContentList(long transcriptionId, Page page, Long layerId) {
        List<AnnotationList> otherContentList = new ArrayList<>();
        for (PageLayer pageLayer : page.getPageLayers()) {
            if (layerId == null || Objects.equals(pageLayer.getLayer().getId(), layerId)) {
                otherContentList.add(createOtherContent(transcriptionId, pageLayer));
            }
        }
        return otherContentList;
    }

    private AnnotationList createOtherContent(long transcriptionId, PageLayer pageLayer) {
        return new AnnotationList(iiifUriResolver.getPageLayerUrl(origin, transcriptionId, pageLayer.getPage().getId(), pageLayer.getLayer().getId()));
    }

    @SneakyThrows
    public String getAnnotationList(long transcriptionId, long pageId, long layerId) {
        origin = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        transcriptionService.requireTranscription(transcriptionId);
        transcriptionPermissionService.checkReadAccess(transcriptionId);

        Page page = pageService.getPage(transcriptionId, pageId, ViewMode.READ_ONLY);
        if (page != null) {
            PageLayer pageLayer = page.getPageLayers().stream().filter(x -> x.getLayer().getId() == layerId).findFirst().orElse(null);
            if (pageLayer != null) {
                String canvasId = iiifUriResolver.getComponentUrl(origin, transcriptionId, "canvas", page.getId());
                AnnotationList annotationList = new AnnotationList(iiifUriResolver.getPageLayerUrl(origin, transcriptionId, pageId, layerId));
                extractLayerNameAndDescription(pageLayer, annotationList);
                annotationList.setResources(createResources(transcriptionId, page, pageLayer, canvasId));
                return new IiifObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(annotationList);
            }
        }

        return "";
    }

    private void extractLayerNameAndDescription(PageLayer pageLayer, AnnotationList annotationList) {
        Layer layer = pageLayer.getLayer();
        if (layer != null) {
            if (layer.getName() != null) annotationList.setLabel(new PropertyValue(layer.getName()));
            if (layer.getDescription() != null)
                annotationList.setDescription(new PropertyValue(layer.getDescription()));
        }
    }

    private List<Annotation> createResources(long transcriptionId, Page page, PageLayer pageLayer, String canvasId) {
        List<Annotation> annotationList = new ArrayList<>();
        for (Verse verse : page.getVerses()) {
            if (verse.getPageLayer() == null || !verse.getPageLayer().equals(pageLayer)) continue;
            annotationList.add(createAnnotation(transcriptionId, verse, canvasId));
        }
        return annotationList;
    }

    public Annotation createAnnotation(long transcriptionId, Verse verse, String canvasId) {
        String id = iiifUriResolver.getComponentUrl(origin, transcriptionId, "annotation", verse.getId());
        //TODO change serializer to accept different motivations in same way as painting
        //TODO or leave painting
        Annotation annotation = new Annotation(id, Motivation.PAINTING);
        annotation.setResource(new ContentAsText(verse.getPlainContent()));
        annotation.setOn(new Canvas(combineCanvasIdWithBoundingBox(canvasId, verse.getBoundingBox())));
        return annotation;
    }

    private String combineCanvasIdWithBoundingBox(String canvasId, BoundingBox boundingBox) {
        int x = boundingBox.getUpperLeftX();
        int y = boundingBox.getUpperLeftY();
        int w = boundingBox.getLowerRightX() - x;
        int h = boundingBox.getLowerRightY() - y;
        return String.format("%s#xywh=%d,%d,%d,%d", canvasId, x, y, w, h);
    }
}

package pl.psnc.vtl.portal.services.iiifpresentation;

import info.freelibrary.iiif.presentation.v3.*;
import info.freelibrary.iiif.presentation.v3.properties.Label;
import info.freelibrary.iiif.presentation.v3.properties.PartOf;
import info.freelibrary.iiif.presentation.v3.services.ImageService3;
import info.freelibrary.iiif.presentation.v3.services.OtherService3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.psnc.vtl.portal.dto.iiifpresentation.CommentingAnnotation;
import pl.psnc.vtl.portal.dto.iiifpresentation.TaggingAnnotation;
import pl.psnc.vtl.portal.model.indexes.Index;
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
public class IIIFPresentation3Service {

    private final TranscriptionService transcriptionService;

    private final TranscriptionPermissionService transcriptionPermissionService;

    private final PageListService pageListService;

    private final PageService pageService;

    private final IIIFUriResolver iiifUriResolver;

    private String origin;

    public String getTranscriptionManifest(Long transcriptionId, Long layerId) {
        origin = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        Transcription transcription = transcriptionService.requireTranscription(transcriptionId);
        transcriptionPermissionService.checkReadAccess(transcriptionId);

        Manifest manifest = createManifest(transcriptionId, transcription);
        manifest.setServices(createSearchService(transcriptionId, layerId));
        manifest.setCanvases(createCanvases(transcriptionId, layerId));

        return manifest.toString();
    }

    private Manifest createManifest(Long transcriptionId, Transcription transcription) {
        //MANIFEST = TRANSCRIPTION
        String manifestID = iiifUriResolver.getManifestUrl(origin, transcriptionId);
        Label label = new Label("pl", transcription.getTitleLabel());
        return new Manifest(manifestID, label);
    }

    private List<Canvas> createCanvases(Long transcriptionId, Long layerId) {
        //CANVAS = PAGE
        List<Canvas> canvasList = new ArrayList<>();
        for (Page page : pageListService.getPages(transcriptionId)) {
            canvasList.add(createCanvas(transcriptionId, page, layerId));
        }

        return canvasList;
    }

    private Canvas createCanvas(Long transcriptionId, Page page, Long layerId) {
        Canvas canvas = new Canvas(iiifUriResolver.getComponentUrl(origin, transcriptionId, "canvas", page.getId()));
        canvas.setWidthHeight(page.getBoundingBox().getLowerRightX(), page.getBoundingBox().getLowerRightY());
        canvas.setPaintingPages(createPaintingAnnotationPage(transcriptionId, page, canvas));
        canvas.setOtherAnnotations(createAnnotationPages(transcriptionId, page, layerId, canvas));
        return canvas;
    }

    private List<AnnotationPage<? extends Annotation<?>>> createAnnotationPages(Long transcriptionId, Page page, Long layerId, Canvas canvas) {
        //ANNOTATION PAGE = LAYER
        List<AnnotationPage<? extends Annotation<?>>> annotationPages = new ArrayList<>();
        for (PageLayer pageLayer : page.getPageLayers()) {
            if (layerId == null || Objects.equals(pageLayer.getLayer().getId(), layerId)) {
                AnnotationPage<? extends Annotation<?>> annotationPage = createCommentingAnnotationPage(transcriptionId, page, canvas, pageLayer);
                annotationPages.add(annotationPage);
                annotationPage = createTaggingAnnotationPage(transcriptionId, page, canvas, pageLayer);
                annotationPages.add(annotationPage);
            }
        }
        return annotationPages;
    }

    private AnnotationPage<? extends Annotation<?>> createCommentingAnnotationPage(Long transcriptionId, Page page, Canvas canvas, PageLayer pageLayer) {
        //ANNOTATION PAGE = LAYER
        AnnotationPage<CommentingAnnotation> layerAnnotationPage = new AnnotationPage<>(iiifUriResolver.getPageLayerUrl(origin, transcriptionId, pageLayer.getPage().getId(), pageLayer.getLayer().getId()));
        extractLayerNameAndDescription(pageLayer, layerAnnotationPage);
        layerAnnotationPage.setPartOfs(new PartOf(new AnnotationCollection("id", "label").getID(), "type"));

        List<CommentingAnnotation> commentingAnnotations = createCommentingAnnotations(transcriptionId, page, canvas, pageLayer);
        if (commentingAnnotations.size() > 0) {
            layerAnnotationPage.addAnnotations(commentingAnnotations);
        }

        return layerAnnotationPage;
    }

    private AnnotationPage<? extends Annotation<?>> createTaggingAnnotationPage(Long transcriptionId, Page page, Canvas canvas, PageLayer pageLayer) {
        //ANNOTATION PAGE = LAYER
        AnnotationPage<TaggingAnnotation> layerAnnotationPage = new AnnotationPage<>(iiifUriResolver.getPageLayerUrl(origin, transcriptionId, pageLayer.getPage().getId(), pageLayer.getLayer().getId()) + "/tags");
        extractLayerNameAndDescription(pageLayer, layerAnnotationPage);
        layerAnnotationPage.setPartOfs(new PartOf(new AnnotationCollection("id", "label").getID(), "type"));

        List<TaggingAnnotation> taggingAnnotations = createTaggingAnnotations(transcriptionId, page, canvas, pageLayer);
        if (taggingAnnotations.size() > 0) {
            layerAnnotationPage.addAnnotations(taggingAnnotations);
        }

        return layerAnnotationPage;
    }

    private List<CommentingAnnotation> createCommentingAnnotations(Long transcriptionId, Page page, Canvas canvas, PageLayer pageLayer) {
        //ANNOTATION = VERSE
        List<CommentingAnnotation> commentingAnnotationList = new ArrayList<>();
        for (Verse verse : page.getVerses()) {
            if (verse.getPageLayer() == null || !verse.getPageLayer().equals(pageLayer)) continue;
            commentingAnnotationList.add(createCommentingAnnotation(transcriptionId, canvas, verse));
        }
        return commentingAnnotationList;
    }

    private CommentingAnnotation createCommentingAnnotation(Long transcriptionId, Canvas canvas, Verse verse) {
        //ANNOTATION = VERSE
        CommentingAnnotation commentingAnnotation = new CommentingAnnotation(iiifUriResolver.getComponentUrl(origin, transcriptionId, "annotation", verse.getId()), canvas);
        TextualBody textualBody = new TextualBody();
        textualBody.setLanguage("pl-PL");
        textualBody.setValue(verse.getPlainContent());
        textualBody.setFormat(MediaType.TEXT_PLAIN);
        commentingAnnotation.setBodies(textualBody);

        if (verse.getBoundingBox() != null) {
            commentingAnnotation.setTarget(combineCanvasIdWithBoundingBox(canvas, verse.getBoundingBox()));
        }
        return commentingAnnotation;
    }

    private List<TaggingAnnotation> createTaggingAnnotations(Long transcriptionId, Page page, Canvas canvas, PageLayer pageLayer) {
        //ANNOTATION = Index
        List<TaggingAnnotation> taggingAnnotationList = new ArrayList<>();
        for (Verse verse : page.getVerses()) {
            if (verse.getPageLayer() == null || !verse.getPageLayer().equals(pageLayer)) continue;
            if (verse.getIndexes() == null) continue;

            for (Index index : verse.getIndexes()) {
                taggingAnnotationList.add(createTaggingAnnotation(transcriptionId, canvas, verse, index));
            }
        }
        return taggingAnnotationList;
    }

    private TaggingAnnotation createTaggingAnnotation(Long transcriptionId, Canvas canvas, Verse verse, Index index) {
        //ANNOTATION = INDEX
        TaggingAnnotation taggingAnnotation = new TaggingAnnotation(iiifUriResolver.getComponentUrl(origin, transcriptionId, "annotation", index.getId()), canvas);
        TextualBody textualBody = new TextualBody();
        textualBody.setLanguage("pl-PL");
        textualBody.setValue(index.getName());
        textualBody.setFormat(MediaType.TEXT_PLAIN);
        taggingAnnotation.setBodies(textualBody);

        if (verse.getBoundingBox() != null) {
            taggingAnnotation.setTarget(combineCanvasIdWithBoundingBox(canvas, verse.getBoundingBox()));
        }
        return taggingAnnotation;
    }

    private void extractLayerNameAndDescription(PageLayer pageLayer, AnnotationPage<? extends Annotation<?>> layerAnnotationPage) {
        Layer layer = pageLayer.getLayer();
        if (layer != null) {
            if (layer.getName() != null) layerAnnotationPage.setLabel(layer.getName());
            if (layer.getDescription() != null) layerAnnotationPage.setSummary(layer.getDescription());
        }
    }

    private AnnotationPage<PaintingAnnotation> createPaintingAnnotationPage(Long transcriptionId, Page page, Canvas canvas) {
        //PAINTING ANNOTATION PAGE = CONTAINER FOR IMAGES
        AnnotationPage<PaintingAnnotation> paintingAnnotationPage = new AnnotationPage<>(iiifUriResolver.getComponentUrl(origin, transcriptionId, "painting-annotation-page", page.getId()));
        paintingAnnotationPage.addAnnotations(createPaintingAnnotation(transcriptionId, page, canvas));
        return paintingAnnotationPage;
    }

    private PaintingAnnotation createPaintingAnnotation(Long transcriptionId, Page page, Canvas canvas) {
        //PAINTING ANNOTATION = CONTAINER FOR IMAGE
        PaintingAnnotation paintingAnnotation = new PaintingAnnotation(iiifUriResolver.getComponentUrl(origin, transcriptionId, "painting-annotation", page.getId()), canvas);
        paintingAnnotation.setBodies(createImageContent(transcriptionId, page, canvas));
        paintingAnnotation.setTarget(canvas.getID());
        return paintingAnnotation;
    }

    private ImageContent createImageContent(Long transcriptionId, Page page, Canvas canvas) {
        //IMAGE CONTENT = IIIF IMAGE
        String imageUrl = iiifUriResolver.createPublicImageUrl(transcriptionId, iiifUriResolver.getLocalPyramidFilename(page));
        ImageContent imageContent = new ImageContent(imageUrl); // link to the image in iiif-image format
        imageContent.setWidthHeight(canvas.getWidth(), canvas.getHeight());
        imageContent.setServices(createImageService3(page));
        return imageContent;
    }

    private ImageService3 createImageService3(Page page) {
        return new ImageService3(iiifUriResolver.getPublicImageServiceUrl(page)); //FIXME create proper method
    }

    private OtherService3 createSearchService(long transcriptionId, Long layerId) {
        OtherService3 searchService = new OtherService3();
        searchService.setID(iiifUriResolver.getSearchServiceUrl(origin, transcriptionId, layerId));
        searchService.setType("SearchService1");
        searchService.setProfile("http://iiif.io/api/search/1/search");
        return searchService;
    }

    private String combineCanvasIdWithBoundingBox(Canvas canvas, BoundingBox boundingBox) {
        int x = boundingBox.getUpperLeftX();
        int y = boundingBox.getUpperLeftY();
        int w = boundingBox.getLowerRightX() - x;
        int h = boundingBox.getLowerRightY() - y;
        return String.format("%s#xywh=%d,%d,%d,%d", canvas.getID().toString(), x, y, w, h);
    }

}

package pl.psnc.vtl.portal.dto.iiifpresentation;

import info.freelibrary.iiif.presentation.v3.Annotation;
import info.freelibrary.iiif.presentation.v3.CanvasResource;

import java.net.URI;

public class CommentingAnnotation extends Annotation<CommentingAnnotation> {

    static final String MOTIVATION = "commenting";

    /**
     * Creates a commenting annotation from the supplied ID and canvas resource.
     *
     * @param <C> A type of canvas
     * @param aID An ID
     * @param aCanvas A canvas to target
     */
    public <C extends CanvasResource<C>> CommentingAnnotation(final URI aID, final CanvasResource<C> aCanvas) {
        super(aID, aCanvas);
        myMotivation = MOTIVATION;
    }

    /**
     * Creates a commenting annotation from the supplied ID and canvas resource.
     *
     * @param <C> A type of canvas
     * @param aID An ID in string form
     * @param aCanvas A canvas to target
     */
    public <C extends CanvasResource<C>> CommentingAnnotation(final String aID, final CanvasResource<C> aCanvas) {
        this(URI.create(aID), aCanvas);
    }

}

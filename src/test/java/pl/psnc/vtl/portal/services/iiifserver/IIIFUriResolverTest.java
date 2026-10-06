package pl.psnc.vtl.portal.services.iiifserver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import pl.psnc.vtl.portal.repositories.imports.DocumentsLoaderRepository;

@RunWith(MockitoJUnitRunner.class)
public class IIIFUriResolverTest {

    private static final Integer THUMBNAIL_WIDTH = 300;

    private static final Integer THUMBNAIL_HEIGHT = 300;

    private static final String IIIF_IMAGE_URL = "http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0001/full/full/0/default.jpg";

    @Mock
    private DocumentsLoaderRepository documentsLoaderRepository;

    private IIIFUriResolver iiifUriResolver;

    @Before
    public void init() {
        Mockito.when(documentsLoaderRepository.loadDocumentStringFromResource(Mockito.anyString()))
                .thenReturn("{\"@context\": \"http://iiif.io/api/image/2/context.json\"}");

        iiifUriResolver = new IIIFUriResolver(documentsLoaderRepository);
        ReflectionTestUtils.setField(iiifUriResolver, "thumbnailWidth", THUMBNAIL_WIDTH);
        ReflectionTestUtils.setField(iiifUriResolver, "thumbnailHeight", THUMBNAIL_HEIGHT);
        iiifUriResolver.init();
    }

    @Test
    public void shouldReturnCorrectThumbnailUrlForPortrait() {
        String thumbnailUrl = iiifUriResolver.getThumbnailUrl(IIIF_IMAGE_URL, 2345, 3567, false);

        Assert.assertEquals("http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0001/pct:0,0,100.0,65.7/!300,300/0/default.jpg", thumbnailUrl);
    }

    @Test
    public void shouldReturnCorrectThumbnailUrlForLandscape() {
        String thumbnailUrl = iiifUriResolver.getThumbnailUrl(IIIF_IMAGE_URL, 3567, 2345, false);

        Assert.assertEquals("http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0001/pct:0,0,65.7,100.0/!300,300/0/default.jpg", thumbnailUrl);
    }
}
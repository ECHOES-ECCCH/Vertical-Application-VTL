package pl.psnc.vtl.portal.services.imports.iiif;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import pl.psnc.vtl.portal.ResourceFileContentLoader;
import pl.psnc.vtl.portal.services.iiifserver.IIIFUriResolver;
import pl.psnc.vtl.portal.services.imports.LoadedFile;
import pl.psnc.vtl.portal.services.imports.Link;
import pl.psnc.vtl.portal.services.imports.PublicationLinks;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class IIIFLinkMapperStrategyTest {

    IIIFLinkMapperStrategy mapper = new IIIFLinkMapperStrategy();
    private String MANIFEST = "ManifestWithoutCoordinates.json";
    @Mock
    private IIIFUriResolver iiifUriResolver;

    @Test
    public void shouldMapToTranscriptionCore() {
        ReflectionTestUtils.setField(mapper, "iiifUriResolver", iiifUriResolver);
        ReflectionTestUtils.setField(iiifUriResolver, "thumbnailSizeExpression", "/!300,300");
        Mockito.when(iiifUriResolver.getThumbnailUrl(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyBoolean())).thenCallRealMethod();
        List<Link> links = List.of(
                new Link("http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0001/full/full/0/default.jpg",
                        3874, 5693,
                        "http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0001/pct:0,0,100.0,68.0/!300,300/0/default.jpg",
                        "http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0001"),
                new Link("http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0002/full/full/0/default.jpg",
                        3874, 5692, "http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0002/pct:0,0,100.0,68.1/!300,300/0/default.jpg",
                        "http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0002"),
                new Link("http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0003/full/full/0/default.jpg",
                        3874, 5692, "http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0003/pct:0,0,100.0,68.1/!300,300/0/default.jpg",
                        "http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0003"),
                new Link("http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0004/full/full/0/default.jpg",
                        3875, 5692, "http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0004/pct:0,0,100.0,68.1/!300,300/0/default.jpg",
                        "http://glam.uni.wroc.pl/iiif/image-api/GSL_GSL_8033_II_50206_0004")
        );


        PublicationLinks publicationLinks = new PublicationLinks(links, true);
        String iiifString = ResourceFileContentLoader.loadFileContentAsString(MANIFEST);

        LoadedFile dto = new LoadedFile("url", iiifString);
        assertEquals(publicationLinks, mapper.getTranscriptionLinks(dto));

    }
}

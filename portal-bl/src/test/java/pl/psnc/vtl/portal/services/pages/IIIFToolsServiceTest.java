package pl.psnc.vtl.portal.services.pages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.repositories.iiiftools.DeleteTranscriptionImagesEmailService;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsRepository;
import pl.psnc.vtl.portal.services.users.UserService;
import pl.psnc.vtl.portal.utils.IIIFUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RunWith(MockitoJUnitRunner.class)
public class IIIFToolsServiceTest {

    private static final String INFO_JSON = "info.json";

    private static final String INFO_CORRUPTED_JSON = "info-corrupted.json";

    IIIFToolsRepository iiifToolsRepository = new IIIFToolsRepository();

    @Mock
    private DeleteTranscriptionImagesEmailService deleteTranscriptionImagesEmailService;

    @Mock
    private UserService userService;

    WireMockServer wireMockServer = new WireMockServer();

    ObjectMapper mapper = new ObjectMapper();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(6666);

    @Before
    public void init() {
        ReflectionTestUtils.setField(iiifToolsRepository, "iiifToolsUrl", "http://localhost:6666/");
    }

    @Test
    public void shouldReturnBoundingBox() throws IOException {
        String testFile = loadInfoJson(INFO_JSON);

        BoundingBox boundingBox = IIIFUtils.getBoundingBoxFromInfoJson(testFile);

        Assert.assertNotNull(boundingBox);
        Assert.assertNotNull(boundingBox.getUpperLeftX());
        Assert.assertNotNull(boundingBox.getUpperLeftY());
        Assert.assertNotNull(boundingBox.getLowerRightX());
        Assert.assertNotNull(boundingBox.getLowerRightY());
        Assert.assertEquals(0, boundingBox.getUpperLeftX().intValue());
        Assert.assertEquals(0, boundingBox.getUpperLeftY().intValue());
        Assert.assertEquals(2596, boundingBox.getLowerRightX().intValue());
        Assert.assertEquals(3760, boundingBox.getLowerRightY().intValue());
    }

    @Test
    public void shouldReturnEmptyBoundingBoxWhenInfoJsonCorrupted() throws IOException {
        String testFile = loadInfoJson(INFO_CORRUPTED_JSON);

        BoundingBox boundingBox = IIIFUtils.getBoundingBoxFromInfoJson(testFile);
        assertEmpty(boundingBox);
    }

    private void assertEmpty(BoundingBox boundingBox) {
        Assert.assertNotNull(boundingBox);
        Assert.assertNull(boundingBox.getUpperLeftX());
        Assert.assertNull(boundingBox.getUpperLeftY());
        Assert.assertNull(boundingBox.getLowerRightX());
        Assert.assertNull(boundingBox.getLowerRightY());
    }

    @Test
    public void shouldReturnEmptyBoundingBoxWhenInfoJsonEmpty() throws IOException {
        BoundingBox boundingBox = IIIFUtils.getBoundingBoxFromInfoJson("");
        assertEmpty(boundingBox);
    }

    @Test
    public void shouldReturnEmptyBoundingBoxWhenInfoJsonNull() throws IOException {
        BoundingBox boundingBox = IIIFUtils.getBoundingBoxFromInfoJson(null);
        assertEmpty(boundingBox);
    }

    private String loadInfoJson(String infoJson) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        return new String(Files.readAllBytes(Paths.get(classLoader.getResource(infoJson).getFile())));
    }

}
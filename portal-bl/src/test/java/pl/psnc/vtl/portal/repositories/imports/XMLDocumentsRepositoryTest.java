package pl.psnc.vtl.portal.repositories.imports;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import pl.psnc.vtl.portal.controllers.ErrorResponseMessage;
import pl.psnc.vtl.portal.controllers.RequestFieldsValidationException;
import pl.psnc.vtl.portal.services.imports.URLErrorMessages;

import java.net.MalformedURLException;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

public class XMLDocumentsRepositoryTest {

    private final DocumentsLoaderRepository repository = new DocumentsLoaderRepository();
    {
        repository.init();
    }

    WireMockServer wireMockServer =  new WireMockServer();
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(6666);

    @Test
    public void shouldInformWhenUnresponsiveUrl() throws MalformedURLException {
        String urlString = "http://localhost:6666/example";
        URL url = new URL(urlString);
        stubFor(get(urlMatching(url.getPath()))
                .willReturn(aResponse()
                        .withStatus(408)));
        try{
            repository.loadDocumentStringFromResource(urlString);
            fail();
        } catch (RequestFieldsValidationException e) {
            ErrorResponseMessage messages = e.getErrorResponseMessage();
            assertTrue(messages.getMessages().containsKey("resourceUrl"));
            assertEquals(URLErrorMessages.UNRESPONSIVE.getErrorMessage(), messages.getMessages().get("resourceUrl"));
        }

    }

    @Test
    public void shouldInformWhenSecuredUrl() throws MalformedURLException {
        String urlString = "http://localhost:6666/example";
        URL url = new URL(urlString);
        stubFor(get(urlMatching(url.getPath()))
                .willReturn(aResponse()
                        .withStatus(401)));
        try{
            repository.loadDocumentStringFromResource(urlString);
            fail();
        } catch (RequestFieldsValidationException e) {
            ErrorResponseMessage messages = e.getErrorResponseMessage();
            assertTrue(messages.getMessages().containsKey("resourceUrl"));
            assertEquals(URLErrorMessages.SECURED.getErrorMessage(), messages.getMessages().get("resourceUrl"));
        }
    }

    @Test
    public void shouldInformWhenMalformedUrl(){
        String uri = "incorrectUrl";
        try{
            repository.loadDocumentStringFromResource(uri);
            fail(); //Test should fail if no exception is thrown
        } catch (RequestFieldsValidationException e) {
            ErrorResponseMessage messages = e.getErrorResponseMessage();
            assertTrue(messages.getMessages().containsKey("resourceUrl"));
            assertEquals(URLErrorMessages.MALFORMED.getErrorMessage(), messages.getMessages().get("resourceUrl"));
        }

    }

    @Test
    public void shouldHandlePermanentRedirect() throws MalformedURLException {

        String urlString = "http://localhost:6666/resourceUrl";
        String redirectUrlString = "http://localhost:6666/redirectUrl";
        String body = "body";

        URL resourceUrl = new URL(urlString);
        URL redirectUrl = new URL(redirectUrlString);

        stubFor(get(urlMatching(resourceUrl.getPath()))
                .willReturn(permanentRedirect(redirectUrl.getPath())));
        stubFor(get(urlMatching(resourceUrl.getPath()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
        String response = repository.loadDocumentStringFromResource(urlString);
        assertEquals(body, response);
    }


    @Test
    public void shouldHandleTemporaryRedirectOnHttps() throws MalformedURLException {

        String urlString = "http://localhost:6666/resourceUrl";
        String redirectUrlString = "https://localhost:6666/redirectUrl";
        String body = "body";

        URL resourceUrl = new URL(urlString);
        URL redirectUrl = new URL(redirectUrlString);

        stubFor(get(urlMatching(resourceUrl.getPath()))
                .willReturn(temporaryRedirect(redirectUrl.getPath())));
        stubFor(get(urlMatching(resourceUrl.getPath()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
        String response = repository.loadDocumentStringFromResource(urlString);
        assertEquals(body, response);
    }

    @Test
    public void shouldHandleSeeOtherRedirect() throws MalformedURLException {

        String urlString = "http://localhost:6666/resourceUrl";
        String redirectUrlString = "http://localhost:6666/redirectUrl";
        String body = "body";

        URL resourceUrl = new URL(urlString);
        URL redirectUrl = new URL(redirectUrlString);

        stubFor(get(urlMatching(resourceUrl.getPath()))
                .willReturn(seeOther(redirectUrl.getPath())));
        stubFor(get(urlMatching(resourceUrl.getPath()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
        String response = repository.loadDocumentStringFromResource(urlString);
        assertEquals(body, response);
    }
}
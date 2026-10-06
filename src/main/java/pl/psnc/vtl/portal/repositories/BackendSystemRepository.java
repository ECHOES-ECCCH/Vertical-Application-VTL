package pl.psnc.vtl.portal.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.SSLHandshakeException;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class BackendSystemRepository {

    protected HttpClient client = HttpClient.newBuilder()
            .version(Version.HTTP_1_1)
            .followRedirects(Redirect.NORMAL)
            .build();
    protected ObjectMapper objectMapper = new ObjectMapper();


    protected String sendAndHandleResponse(HttpRequest request, String actionMessage)
            throws BackendSystemException {
        HttpResponse<String> response = null;
        try {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                if (isUnrecognizedNameHandshakeError(e))
                    return downaloadWithCurl(request);
                throw e;
            }
        } catch (InterruptedException e) {
            log.debug("Interrupted while " + actionMessage, e);
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException e) {
            handleException(e, actionMessage);
            return null;
        }
        handleErrorResponse(response, actionMessage);
        return response.body();
    }

    private boolean isUnrecognizedNameHandshakeError(IOException e) {
        return e.getCause() instanceof SSLHandshakeException && e.getCause().getMessage().contains("unrecognized_name");
    }

    private String downaloadWithCurl(HttpRequest request) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>(List.of(
                "curl", "--silent", "--show-error", "--fail", "--location", "--max-redirs", "5", "--retry", "5",
                "--connect-timeout", "6", "--max-time", "30"));
        command.add("-X");
        command.add(request.method());
        request.headers().map().forEach((name, value) -> {
            command.add("-H");
            command.add(name + ": " + value);
        });
        command.add(request.uri().toString());

        Process process = new ProcessBuilder(command).start();
        String output;
        try (InputStream is = process.getInputStream()) {
            output = IOUtils.toString(is, "UTF-8");
        }
        String error;
        try (InputStream is = process.getErrorStream()) {
            error = IOUtils.toString(is, "UTF-8");
        }
        if (!error.isEmpty()) {
            log.warn("Curl error for " + request.uri() + " : " + (error.length() > 1000
                    ? error.substring(0, 1000) + "[... + " + (error.length() - 1000) + "]" : error));
        }
        int exitValue = process.waitFor();
        if (exitValue != 0) {
            throw new IOException("Curl exited with code " + exitValue);
        }
        return output;
    }

    protected abstract void handleException(Exception ex, String actionMessage)
            throws BackendSystemException;


    protected abstract void handleErrorResponse(HttpResponse<String> response, String actionMessage)
        throws BackendSystemException;


    protected <T> T readResponseValue(String response, String actionMessage)
            throws BackendSystemException {
        TypeReference<T> typeRef = new TypeReference<>() {
            // empty
        };
        try {
            return objectMapper.readValue(response, typeRef);
        } catch (IOException e) {
            throw handleDeserializationException(e, actionMessage);
        }
    }


    protected abstract BackendSystemException handleDeserializationException(IOException e, String actionMessage);


    protected String writeRequestValue(Object value, String actionMessage)
            throws BackendSystemException {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (IOException e) {
            throw handleSerializationException(e, actionMessage);
        }
    }


    protected abstract BackendSystemException handleSerializationException(IOException e, String actionMessage);

}

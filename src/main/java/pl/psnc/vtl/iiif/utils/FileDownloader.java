package pl.psnc.vtl.iiif.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Component
@Slf4j
public class FileDownloader {
    @Value("${vtl.iiif-tools.timeout.read}")
    private int readTimeout;

    @Value("${vtl.iiif-tools.max-redirects}")
    private int maxRedirects;

    @Value("${vtl.iiif-tools.timeout.connection}")
    private int connectionTimeout;

    @Value("${vtl.iiif-tools.max-retries}")
    private int maxRetries;

    public void downloadFile(URL url, File targetFilePath) throws IOException {
        if (targetFilePath.exists())
            throw new IOException("Target file already exists: " + targetFilePath.getAbsolutePath());
        targetFilePath.getParentFile().mkdirs();

        String[] command = { "curl", "--silent", "--show-error", "--fail",
            "--location", "--max-redirs", "" + maxRedirects, "--retry", "" + maxRetries,
            "--connect-timeout", "" + (connectionTimeout / 1000), "--max-time", "" + (readTimeout / 1000),
            "--output", targetFilePath.getAbsolutePath(),
            url.toString() };
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output;
        try (InputStream is = process.getInputStream()) {
            output = org.apache.commons.io.IOUtils.toString(is, "UTF-8");
        }
        if (!output.isEmpty()) {
            log.warn("Curl output for " + url + " : " + (output.length() > 1000
                    ? output.substring(0, 1000) + "[... + " + (output.length() - 1000) + "]" : output));
        }
        try {
            int exitValue = process.waitFor();
            if (exitValue != 0 || !targetFilePath.exists()) {
                throw new IOException("Curl exited with code " + exitValue);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }
}

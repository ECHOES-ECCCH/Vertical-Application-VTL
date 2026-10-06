package pl.psnc.vtl.iiif.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class CommandExecutor {

    @Value("${vtl.iiif-tools.timeout.process}")
    private Integer processTimeout;

    public String runCommand(List<String> command) throws IOException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

        CompletableFuture<StringBuilder> future = new CompletableFuture<>();
        Thread execThread = new Thread(() -> {
            log.debug("executing command: " + String.join(" ", command));
            StringBuilder output = new StringBuilder();
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = input.readLine()) != null) {
                    checkNewOutputLine(line);
                    output.append(line).append(System.lineSeparator());
                }
                process.waitFor();
            } catch (Exception e) {
                future.completeExceptionally(e);
                return;
            }
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                future.completeExceptionally(new IOException("Command exited with code " + exitValue));
            } else {
                future.complete(output);
            }
        }, "cmdExec-" + command.hashCode());
        execThread.setDaemon(true);
        execThread.start();

        try {
            StringBuilder output = future.get(processTimeout, TimeUnit.MILLISECONDS);
            return output.toString();
        } catch (TimeoutException e) {
            throw new IOException("Command timed out: " + String.join(" ", command));
        } catch (ExecutionException e) {
            throw new IOException("Unexpected error for command " + String.join(" ", command), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Command interrupted: " + String.join(" ", command), e);
        } finally {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
        }
    }

    private void checkNewOutputLine(String line) {
        log.debug(line);

        // this handles "unexpected" exception throwed by imagemagick
        // when trying to convert djvu file with dependencies
        if (line.toLowerCase().contains("unexpected")) {
            throw new RuntimeException("Invalid process output: " + line);
        }
    }
}

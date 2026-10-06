package pl.psnc.vtl.iiif.services.image;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@NoArgsConstructor
public class ProcessBuilderService {


    public Process getProcess(String command)
            throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command.split(" "));
        builder.redirectErrorStream(true);
        Process process = builder.start();
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            log.error("Error while waiting for process '" + command + "'", e);
            throw new IOException("Error while waiting for process '" + command + "'", e);
        }
        if (exitCode != 0) {
            log.error("Process '" + command + "' finished with exit code " + exitCode);
            throw new IOException("Process '" + command + "' finished with exit code " + exitCode);
        }
        return process;
    }
}

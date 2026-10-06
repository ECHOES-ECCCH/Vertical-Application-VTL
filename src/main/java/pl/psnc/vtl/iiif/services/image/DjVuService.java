package pl.psnc.vtl.iiif.services.image;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.iiif.converters.DjVuInfo;
import pl.psnc.vtl.iiif.utils.CommandExecutor;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DjVuService {
    public static final String DJVU = "djvu";
    public static final String DJV = "djv";
    public static final String IFF = "iff";
    public static final String DJBZ = "djbz";
    public static final String THUMB = "thumb";

    @Getter
    private static final List<String> allDjvuExtensions = List.of(DJVU, DJV, IFF, DJBZ, THUMB);
    private static final List<String> nonContentDjvuExtensions = List.of(IFF, DJBZ, THUMB);

    @Value("${vtl.iiif-tools.scripts}")
    private String scriptsFolder;
    private static final String DJVUDUMP_SCRIPT_FILE = "/djvudump.sh";

    private final CommandExecutor executor;

    public boolean endsWithDjVuExtension(String sourceFilePath) {
        return hasExtension(sourceFilePath, allDjvuExtensions);
    }

    private boolean hasExtension(String sourceFilePath, @NonNull List<String> extensions) {
        if (sourceFilePath != null) {
            String extension = FilenameUtils.getExtension(sourceFilePath);
            return extensions.stream().anyMatch(e -> e.equals(extension));
        }
        return false;
    }

    public DjVuInfo determineDjVuInfo(String sourceFilePath) throws IOException {
        log.debug("DjVu file to process... determining type of DjVu file");
        if (hasExtension(sourceFilePath, nonContentDjvuExtensions)) {
            log.debug("File does not have any page content (one of iif, djbz or thumb)");
            return new DjVuInfo(false, false, false, false);
        }
        log.debug("Checking type of the file using djvudumb");
        String results = executor.runCommand(List.of(scriptsFolder + DJVUDUMP_SCRIPT_FILE, sourceFilePath));
        return DjVuInfo.parse(results);
    }
}

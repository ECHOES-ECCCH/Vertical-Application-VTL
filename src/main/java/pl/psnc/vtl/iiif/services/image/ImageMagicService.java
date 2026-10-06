package pl.psnc.vtl.iiif.services.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.iiif.converters.DjVuInfo;
import pl.psnc.vtl.iiif.utils.CommandExecutor;
import pl.psnc.vtl.iiif.utils.StoragePathUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageMagicService {

    private static final String DIMENSIONS_SCRIPT =  "image_dimensions.sh"; //"/usr/bin/identify %s";

    private static final String PAGES_PDF_SCRIPT = "pdf_pages.sh";

    private static final String PAGES_IMAGE_SCRIPT = "image_pages.sh";
    private static final String PAGES_DJVU_SCRIPT = "djvu_pages.sh";

    private final StoragePathUtils storagePathUtils;

    @Value("${vtl.iiif-tools.storage}")
    private String rootFolder;

    @Value("${vtl.iiif-tools.scripts}")
    private String scriptsFolder;

    private final CommandExecutor executor;
    private final DjVuService djVuService;

    private String getPagesPdfCommand(){
        return  scriptsFolder + "/" + PAGES_PDF_SCRIPT;
    }

    private String getPagesImageCommand(){
        return  scriptsFolder + "/" + PAGES_IMAGE_SCRIPT;
    }

    private String getNumberOfPagesCommand(String sourceFilePath){

        String fileExtension = FilenameUtils.getExtension(FilenameUtils.getName(sourceFilePath));

        String command = getPagesImageCommand();
        if ("pdf".equals(fileExtension))
            command =  getPagesPdfCommand();
        if (djVuService.endsWithDjVuExtension(sourceFilePath)) {
            command = getDjVuCommand();
        }

        return  command;
    }

    private String getDjVuCommand() {
        return  scriptsFolder + "/" + PAGES_DJVU_SCRIPT;
    }

    public Integer getPagesNumber(long transcriptionId, String filename)
            throws IOException {
        String sourceFilePath = storagePathUtils.getOriginalFilepath(transcriptionId, filename);
        if (djVuService.endsWithDjVuExtension(sourceFilePath)) {
            // do not process files which do not represent page as they represent 0 pages
            // and tools handling conversion do not support well unexpected situations
            DjVuInfo djVuInfo;
            try {
                djVuInfo = djVuService.determineDjVuInfo(sourceFilePath);
            } catch (Exception e) {
                log.error("Cannot read number of pages for file " + sourceFilePath + ", see: ", e);
                throw new IOException(e.getMessage());
            }
            if (!djVuInfo.isHasContent() || djVuInfo.isIndirectComposite()) {
                return 0;
            }
        }
        String command = getNumberOfPagesCommand(sourceFilePath);
        File file = new File(sourceFilePath);
        if(!file.exists()) {
            throw new FileNotFoundException("Cannot find file with given filename");
        }

        try {
            String results = executor.runCommand(Arrays.asList(command, sourceFilePath));
            return Integer.parseInt(results.trim());
        } catch (IOException e) {
            log.error("Cannot read number of pages for file " + sourceFilePath + ", see: ", e);
            throw new IOException(e.getMessage());
        }
    }

}

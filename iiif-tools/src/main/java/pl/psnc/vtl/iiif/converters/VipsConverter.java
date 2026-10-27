package pl.psnc.vtl.iiif.converters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.iiif.services.image.DjVuService;
import pl.psnc.vtl.iiif.utils.CommandExecutor;
import pl.psnc.vtl.iiif.utils.ConversionSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pl.psnc.vtl.iiif.services.image.DjVuService.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class VipsConverter extends BaseConverter {
    private static final List<String> ALLOWED_FORMATS = Arrays.asList("tif", "tiff", "jpg", "jpeg", "png", DJV,
            DJVU, IFF, DJBZ, THUMB);
    private static final String DDJVU_CONVERSION_MODE = "ddjvu";
    @Value("${vtl.iiif-tools.scripts}")
    private String scriptsFolder;

    private final CommandExecutor executor;
    private final DjVuService djVuService;

    private static final String SCRIPT_FILE = "/vips_to_pyramid_tiff.sh";
    private static final String CONVERT_TO_PDF_SCRIPT_FILE = "/convert_to_pdf.sh";

    private static final String VIPS_COMMAND = "vips im_vips2tiff %s %s:lzw,tile:%dx%d,pyramid";

    @Value("${vtl.iiif-tools.tile-size}")
    private Integer tileSize;

    @Override
    protected List<String> doConversion(ConversionSettings conversionSettings)
            throws ConversionException {
        try {
            String vipsConversionScript = scriptsFolder + SCRIPT_FILE;
            String command = String.format(vipsConversionScript, conversionSettings.getSourceFilePath(),
                    conversionSettings.getTargetDirectory(), tileSize);

            List<String> commandParts;

            if (djVuService.endsWithDjVuExtension(conversionSettings.getSourceFilePath())) {
                DjVuInfo djVuInfo = djVuService.determineDjVuInfo(conversionSettings.getSourceFilePath());
                if (!djVuInfo.isHasContent() || djVuInfo.isIndirectComposite()) {
                    return new ArrayList<>();
                }
                if (djVuInfo.isIndirectSinglePage()) {
                    // use ddjvu as only this tool supports indirect single pages (convert does not support that)
                    commandParts = Arrays.asList(command, conversionSettings.getSourceFilePath(),
                            conversionSettings.getTargetDirectory().getAbsolutePath(), DDJVU_CONVERSION_MODE);
                } else {
                    commandParts = Arrays.asList(command, conversionSettings.getSourceFilePath(),
                            conversionSettings.getTargetDirectory().getAbsolutePath());
                }
            } else {
                commandParts = Arrays.asList(command, conversionSettings.getSourceFilePath(),
                        conversionSettings.getTargetDirectory().getAbsolutePath());
            }

            String results = executor.runCommand(commandParts);

            //read and validate results of conversion process
            ConversionResult conversionResult = new ConversionResult(results);
            conversionResult.validate();

            return conversionResult.getFilesNames();
        } catch (IOException e) {
            log.error("Conversion failed for file: " + conversionSettings.getSourceFilePath() + " from transcription: "
                    + conversionSettings.getTranscriptionId(), e);
            throw new ConversionException(e.getMessage());
        }
    }

    public void convertDjvuToPdf(String filePath) {
        try {
            String vipsConversionScript = scriptsFolder + CONVERT_TO_PDF_SCRIPT_FILE;
            String path = FilenameUtils.removeExtension(filePath);
            String extension = FilenameUtils.getExtension(filePath);
            executor.runCommand(Arrays.asList(vipsConversionScript, path, extension));
        } catch (IOException e) {
            log.error("Conversion failed for file: " + filePath, e);
            throw new ConversionException(e.getMessage());
        }
    }

    @Override
    public List<String> allowedFormats() {
        return ALLOWED_FORMATS;
    }
}

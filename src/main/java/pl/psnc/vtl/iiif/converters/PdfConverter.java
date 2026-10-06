package pl.psnc.vtl.iiif.converters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.iiif.utils.CommandExecutor;
import pl.psnc.vtl.iiif.utils.ConversionSettings;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PdfConverter extends BaseConverter {

    @Value("${vtl.iiif-tools.scripts}")
    private String scriptsFolder;

    @Value("${vtl.iiif-tools.pdf-dpi}")
    private Integer pdfDpi;

    private final CommandExecutor executor;

    private static final String SCRIPT_FILE = "/pdf_to_pyramid_tiff.sh";


    @Override
    protected List<String> doConversion(ConversionSettings conversionSettings)
            throws ConversionException {
        try {
            String pdfConversionScript = scriptsFolder + SCRIPT_FILE;
            String results = executor.runCommand(
                    Arrays.asList(pdfConversionScript, conversionSettings.getSourceFilePath(),
                            conversionSettings.getTargetDirectory().getAbsolutePath(), pdfDpi.toString()));

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


    @Override
    public List<String> allowedFormats() {
        return Arrays.asList("pdf");
    }


}
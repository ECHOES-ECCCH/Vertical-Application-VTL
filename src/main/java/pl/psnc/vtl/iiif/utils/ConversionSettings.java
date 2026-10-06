package pl.psnc.vtl.iiif.utils;

import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import pl.psnc.vtl.iiif.converters.ConversionException;

import java.io.File;

@Data
public class ConversionSettings {

    //basic convertion info
    private  long transcriptionId;
    private  String sourceFilename;

    //paths to files
    private String sourceFilePath;
    private String targetFilePath;

    private File targetDirectory;
    private String targetFilename;


    //conversion parameters (optional)
    private Integer tileSize;
    private Integer pdfDpi;


    private ConversionSettings(){
    }

    public void haveProperPaths()
            throws ConversionException {
        if (sourceFilePath != null){
            File sourceFile = new File(sourceFilePath);
            if (!sourceFile.exists())
                throw new ConversionException("Source file not exist! Path: " + sourceFilePath);
        } else
            throw new ConversionException("Source file not exist - source file path is empty!");

        if (!targetDirectory.exists()) {
            if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
                throw new ConversionException(
                        "Directory '" + targetDirectory.toString() + "' could not be created");
            }
        }
    }


    public long getTranscriptionId() {
        return transcriptionId;
    }


    public String getSourceFilename() {
        return sourceFilename;
    }


    public String getSourceFilePath() {
        return sourceFilePath;
    }


    public String getTargetFilename() {
        return targetFilename;
    }


    public String getTargetFilePath() {
        return targetFilePath;
    }


    public File getTargetDirectory() {
        return targetDirectory;
    }
    //    public ConvertionPathSettings(long transcriptionId, String filename, StoragePathUtils storagePathUtils){
//        this.transcriptionId = transcriptionId;
//        this.filename = filename;
//        this.sourceFilePath = storagePathUtils.getOriginalFilepath(transcriptionId, filename);
//        this.targetFilename = FilenameUtils.getBaseName(filename) + ".tiff";
//        this.targetFilePath = storagePathUtils.getPyramidFilepath(transcriptionId, targetFilename);
//        this.targetDir = new File(targetFilePath).getParentFile();
//    }


    public static final class ConvertionPathSettingsBuilder {

        private  long transcriptionId;
        private  String sourceFilename;
        private String sourceFilePath;
        private String targetFilename;
        private String targetFilePath;
        private File targetDirectory;


        private ConvertionPathSettingsBuilder() {
        }

        public static ConvertionPathSettingsBuilder builder() {
            return new ConvertionPathSettingsBuilder();
        }


        void  validate(ConversionSettings conversionSettings){
            if (conversionSettings.sourceFilePath == null || conversionSettings.sourceFilePath.isEmpty())
                throw new IllegalStateException("Not set source file for conversion!");

            if (conversionSettings.targetFilePath == null || conversionSettings.targetFilePath.isEmpty())
                throw  new IllegalStateException("Not set target file for conversion!");
        }

        public ConvertionPathSettingsBuilder withTranscriptionId(long transcriptionId) {
            this.transcriptionId = transcriptionId;
            return this;
        }


        public ConvertionPathSettingsBuilder withSourceFilePath(String sourceFilePath) {
            this.sourceFilePath = sourceFilePath;
            this.sourceFilename = FilenameUtils.getName(sourceFilePath);
            return this;
        }


        public ConvertionPathSettingsBuilder withTargetFilePath(String targetFilePath) {
            this.targetFilePath = targetFilePath;
            this.targetFilename = FilenameUtils.getName(targetFilename);
            this.targetDirectory = new File(targetFilePath).getParentFile();
            return this;
        }


        public ConversionSettings build() {
            ConversionSettings conversionSettings = new ConversionSettings();
            conversionSettings.transcriptionId = transcriptionId;
            conversionSettings.sourceFilename = sourceFilename;
            conversionSettings.sourceFilePath = sourceFilePath;
            conversionSettings.targetFilename =targetFilename;
            conversionSettings.targetFilePath = targetFilePath;
            conversionSettings.targetDirectory = targetDirectory;
            validate(conversionSettings);
            return conversionSettings;
        }
    }

    public static ConvertionPathSettingsBuilder builder(){
        return new ConvertionPathSettingsBuilder();
    }
}

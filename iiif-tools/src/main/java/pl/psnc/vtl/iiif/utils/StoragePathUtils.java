package pl.psnc.vtl.iiif.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StoragePathUtils {

    private static final String ORIGINAL_DIRECTORY = "/originals";

    private static final String PYRAMID_DIRECTORY = "/pyramids";

    private static final String EBOOK_DIRECTORY = "/ebooks";

    private static final String ORIGINAL_DIRPATH = ORIGINAL_DIRECTORY + "/t%d";

    private static final String PYRAMID_DIRPATH = PYRAMID_DIRECTORY + "/t%d";

    private static final String EBOOK_DIRPATH = EBOOK_DIRECTORY + "/t%d";

    private static final String ORIGINAL_FILEPATH = ORIGINAL_DIRPATH + "/%s";

    private static final String PYRAMID_FILEPATH = PYRAMID_DIRPATH + "/%s";


    @Value("${vtl.iiif-tools.storage}")
    private String rootFolder;

    public String getOriginalRootDirectory() {
        return rootFolder + ORIGINAL_DIRPATH;
    }

    public String getPyramidRootDirectory() {
        return rootFolder + PYRAMID_DIRECTORY;
    }

    public String getEbookRootDirectory() {
        return rootFolder + EBOOK_DIRECTORY;
    }

    public String getOriginalDirectory(long transcriptionId) {
        return String.format(rootFolder + ORIGINAL_DIRPATH, transcriptionId);
    }

    public String getPyramidDirectory(long transcriptionId) {
        return String.format(rootFolder + PYRAMID_DIRPATH, transcriptionId);
    }

    public String getEbookDirectory(long transcriptionId) {
        return String.format(rootFolder + EBOOK_DIRPATH, transcriptionId);
    }

    public String getOriginalFilepath(long transcriptionId, String filename){
        return String.format(rootFolder + ORIGINAL_FILEPATH, transcriptionId, filename);
    }

    public String getPyramidFilepath(long transcriptionId, String filename){
        return String.format(rootFolder + PYRAMID_FILEPATH, transcriptionId, filename);
    }

}

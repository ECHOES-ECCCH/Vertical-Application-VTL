package pl.psnc.vtl.iiif.eBooks;

import java.nio.file.Path;

final class EBookLocation {
    private static String eBOOKS_FOLDER = "ebooks";

    static Path eBookPath(EBookFormat format, long transcriptionId, String hash) {
        String extension = format.toString().toLowerCase();
        return Path.of(eBOOKS_FOLDER, "t" + transcriptionId, extension, hash + "." + extension);
    }

    static String eBooksFolderPhysicalPath(String rootFolder){
        return Path.of(rootFolder, eBOOKS_FOLDER).toString();
    }
}

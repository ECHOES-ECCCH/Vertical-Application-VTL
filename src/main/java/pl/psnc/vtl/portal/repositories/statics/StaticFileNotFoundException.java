package pl.psnc.vtl.portal.repositories.statics;

public class StaticFileNotFoundException extends RuntimeException {

    public StaticFileNotFoundException(String fileName) {
        super("Static file " + fileName + " not found!");
    }
}

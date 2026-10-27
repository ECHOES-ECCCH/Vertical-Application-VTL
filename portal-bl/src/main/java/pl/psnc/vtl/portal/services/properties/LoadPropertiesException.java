package pl.psnc.vtl.portal.services.properties;

public class LoadPropertiesException extends RuntimeException {

    public LoadPropertiesException(String message, Exception e) {
        super(message, e);
    }
}

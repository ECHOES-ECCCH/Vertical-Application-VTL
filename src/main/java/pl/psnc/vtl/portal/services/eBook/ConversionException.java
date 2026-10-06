package pl.psnc.vtl.portal.services.eBook;

public class ConversionException extends RuntimeException {
    public ConversionException(String hash) {
        super("Cannot convert book with hash: " + hash);
    }
}

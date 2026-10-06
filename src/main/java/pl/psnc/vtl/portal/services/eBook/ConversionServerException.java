package pl.psnc.vtl.portal.services.eBook;

public class ConversionServerException extends RuntimeException {

    public ConversionServerException(int statusCode) {
        super("Cannot convert ebook - error in server, status code = " + statusCode);
    }
}

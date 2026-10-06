package pl.psnc.vtl.portal.services.eBook;

public class NoTaskException extends RuntimeException {
    public NoTaskException(String hash) {
        super("no task set for hash " + hash);
    }
}

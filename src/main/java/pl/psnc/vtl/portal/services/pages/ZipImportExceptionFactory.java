package pl.psnc.vtl.portal.services.pages;

public class ZipImportExceptionFactory {

    public static PageContentImportException unsupportableFileTypeException(String fileName) {
        return new PageContentImportException("Unsupported file type " + fileName, PageContentImportErrorMessages.UNSUPPORTED_FILE_TYPE.getErrorMessage());
    }

    public static PageContentImportException unrecognizablePage(String fileName) {
        return new PageContentImportException("Unrecognizable file type " + fileName, PageContentImportErrorMessages.UNRECOGNIZABLE_PAGE.getErrorMessage(), fileName);
    }

}

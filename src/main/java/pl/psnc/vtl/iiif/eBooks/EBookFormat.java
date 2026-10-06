package pl.psnc.vtl.iiif.eBooks;

public enum EBookFormat {
    MOBI("mobi"),
    EPUB("epub"),
    PDF("pdf");

    final String location;

    EBookFormat(String location) {
        this.location = location;
    }
}

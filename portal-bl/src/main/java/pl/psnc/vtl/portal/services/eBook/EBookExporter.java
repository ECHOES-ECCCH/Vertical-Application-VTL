package pl.psnc.vtl.portal.services.eBook;

@FunctionalInterface
interface EBookExporter {

    /**
     * @param edto
     * @param hash ebook hash
     */
    void exportEbook(EbookDto edto, String hash);
}

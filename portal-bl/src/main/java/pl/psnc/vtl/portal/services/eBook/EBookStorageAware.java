package pl.psnc.vtl.portal.services.eBook;

import pl.psnc.vtl.portal.repositories.iiiftools.ExportedEBook;

public interface EBookStorageAware {
    ExportedEBook getEBookLocation(String hashCode, EbookDto ebookDto);
}

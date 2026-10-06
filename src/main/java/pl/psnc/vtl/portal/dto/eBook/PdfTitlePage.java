package pl.psnc.vtl.portal.dto.eBook;

import lombok.Data;

/**
 * Pdf title page object.
 */
@Data
public class PdfTitlePage {

    /**
     * Pdf main page title.
     */
    private String title;

    /**
     * Pdf main page description.
     */
    private String description;

    /**
     * Pdf main page keywords.
     */
    private String keywords;

    /**
     * Pdf main page contact.
     */
    private String contact;
}

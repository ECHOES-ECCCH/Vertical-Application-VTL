package pl.psnc.vtl.portal.services.eBook;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class EbookDto {
    private final Ebook fileFormat;
    private final long transcriptionId;

    private Long layerId;

    private boolean createTitlePage;

    private String indexLanguage;
}

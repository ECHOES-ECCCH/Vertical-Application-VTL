package pl.psnc.vtl.portal.dto.imports;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.imports.FileType;

/**
 * Import transcription dto object.
 */
@Data
@NoArgsConstructor
public class ImportTranscriptionDTO {

    /**
     * External resource url.
     */
    private String resourceUrl;

    /**
     * External resource file type.
     */
    private FileType fileType;

    /**
     * Optional metadata url if metadata should be taken from different source than
     * page contents.
     */
    private String metadataUrl;

    /**
     * File type for {@link #metadataUrl}, required if {@link #metadataUrl} is set.
     */
    private FileType metadataType;

    /**
     * Id of external object form dLibra and such transcription being imported from
     * dLibra for external edit.
     */
    private String externalId;

    /**
     * For backward compatibility. If true, assume externalId the same as
     * reosourceUrl.
     */
    private boolean externalEdit;

    public ImportTranscriptionDTO(String resourceUrl, FileType fileType, String externalId) {
        this.resourceUrl = resourceUrl;
        this.fileType = fileType;
        this.externalId = externalId;
    }
}

package pl.psnc.vtl.iiif.model;

import lombok.Data;

import java.util.Map;

/**
 * Download files response object.
 */
@Data
public class DownloadFilesResponse {

    /**
     * Map containing given urls with saved filenames.
     */
    Map<String, String> urlsToFilenames;

    /**
     * Quota used by saved files.
     */
    long quota;
}

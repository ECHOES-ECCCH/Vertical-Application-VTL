package pl.psnc.vtl.iiif.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Remove file response object.
 */
@Data
@AllArgsConstructor
public class RemoveFileResponse {

    /**
     * Amount of quota freed by removal of the file.
     */
    long quota;
}

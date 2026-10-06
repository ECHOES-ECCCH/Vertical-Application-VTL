package pl.psnc.vtl.portal.model.domains;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.psnc.vtl.portal.model.imports.FileType;

/**
 * Domain object.
 */
@Data
@AllArgsConstructor
public class Domain {

    /**
     * Value.
     */
    private String value;

    /**
     * Type.
     */
    private FileType type;

}

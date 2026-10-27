package pl.psnc.vtl.portal.services.imports;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URI;

@Data
@AllArgsConstructor
public class LoadedFile {
    private String resourceUrl;

    private String fileContent;
}

package pl.psnc.vtl.iiif.converters;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HtmlContent {

    private final String fileHeader;

    private final byte[] content;

}

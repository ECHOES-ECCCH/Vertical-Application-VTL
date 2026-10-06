package pl.psnc.vtl.portal.dto.pages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Page dto object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDto {

    /**
     * Page id.
     */
    private Long id;

    /**
     * Page number in transcription.
     */
    private int pageNo;

}

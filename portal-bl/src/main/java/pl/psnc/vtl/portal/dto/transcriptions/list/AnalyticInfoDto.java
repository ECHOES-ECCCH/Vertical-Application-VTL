package pl.psnc.vtl.portal.dto.transcriptions.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Analytic info dto object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticInfoDto {

    /**
     * Source title.
     */
    private String title;
}

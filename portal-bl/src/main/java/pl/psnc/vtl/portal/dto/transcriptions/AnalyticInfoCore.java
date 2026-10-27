package pl.psnc.vtl.portal.dto.transcriptions;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Analytic info core object.
 */
@Data
@NoArgsConstructor
public class AnalyticInfoCore {

    /**
     * Source title.
     */
    private String title;

    /**
     * Source actors list.
     */
    private List<ActorCore> actors;

    /**
     * Source language id.
     */
    private LanguageId language;

}

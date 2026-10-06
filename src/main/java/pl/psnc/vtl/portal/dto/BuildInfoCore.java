package pl.psnc.vtl.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.psnc.vtl.portal.conf.BuildInfo;

/**
 * Helper class providing backend image commit id and build date.
 */
@Data
@AllArgsConstructor
public class BuildInfoCore {
    /**
     * Commit id of currently running server image.
     */
    private String commitId;

    /**
     * Build date of currently running server image.
     */
    private Long buildDate;

    public BuildInfoCore() {
        this.commitId = BuildInfo.instance.getCommitId();
        this.buildDate = BuildInfo.instance.getBuildDate();

    }


}

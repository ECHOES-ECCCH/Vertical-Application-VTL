package pl.psnc.vtl.portal.model.verses.projections;

import java.time.LocalDateTime;

public interface VersesStatsForPage {
    Long getPageId();
    int getPageNo();
    LocalDateTime getLastModificationDate();
    String getLastModifiedBy();
    long getNoneCount();
    long getTranscribedCount();
    long getIncorrectCount();
    long getVerifiedCount();

    default long getAllCount() {
        return getNoneCount() + getTranscribedCount() + getIncorrectCount() + getVerifiedCount();
    }
}

package pl.psnc.vtl.portal.model.verses.projections;

public interface VersesStatsForLayer {
    // can be null if somehow verse does not have reference to a PageLayer
    Long getLayerId();
    long getNoneCount();
    long getTranscribedCount();
    long getIncorrectCount();
    long getVerifiedCount();

    default long getAllCount() {
        return getNoneCount() + getTranscribedCount() + getIncorrectCount() + getVerifiedCount();
    }
}

package pl.psnc.vtl.portal.dto.pages;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.conf.datetime.ApiDateTimeFormatter;
import pl.psnc.vtl.portal.model.verses.projections.VersesStatsForPage;

import java.time.LocalDateTime;

/**
 * Page statistics object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageStatistics {

    /**
     * Page id.
     */
    private long pageId;

    /**
     * Page number in transcription.
     */
    private int pageNo;

    /**
     * Verses in file.
     */
    private long versesInFile;

    /**
     * Number of transcribed verses.
     */
    private long versesTranscribed;

    /**
     * Number of verified verses.
     */
    private long versesMarkedAsVerified;

    /**
     * Number of incorrect verses.
     */
    private long versesMarkedAsIncorrect;

    /**
     * User that lastly modified this page.
     */
    private String lastModifier;

    /**
     * Last modification date.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime lastModificationDate;

    public static PageStatistics of(VersesStatsForPage stats) {
        return PageStatistics.builder().pageId(stats.getPageId()).pageNo(stats.getPageNo())
                .versesInFile(stats.getAllCount()).versesTranscribed(stats.getTranscribedCount())
                .versesMarkedAsVerified(stats.getVerifiedCount()).versesMarkedAsIncorrect(stats.getIncorrectCount())
                .lastModifier(stats.getLastModifiedBy()).lastModificationDate(stats.getLastModificationDate()).build();
    }
}

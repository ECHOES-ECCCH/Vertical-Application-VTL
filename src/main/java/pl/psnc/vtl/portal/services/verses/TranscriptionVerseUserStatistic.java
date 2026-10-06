package pl.psnc.vtl.portal.services.verses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TranscriptionVerseUserStatistic {
    private final Long numberOfChangedVerseStatuses;
    private final Long numberOfVerifiedVerses;
    private final String email;
    private final Long numberOfInsertedChars;
}

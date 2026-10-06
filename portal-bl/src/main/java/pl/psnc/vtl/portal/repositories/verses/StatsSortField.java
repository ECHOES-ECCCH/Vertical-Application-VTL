package pl.psnc.vtl.portal.repositories.verses;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum StatsSortField {
    PAGE_NO("pageNo"),
    IN_TRANSCRIPTION("noneCount"),
    ACCEPTED("verifiedCount"),
    REJECTED("incorrectCount"),
    TO_ACCEPT("transcribedCount");

    @Getter
    private final String sortFieldName;
}

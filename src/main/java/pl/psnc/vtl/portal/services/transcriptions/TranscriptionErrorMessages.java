package pl.psnc.vtl.portal.services.transcriptions;

import lombok.Getter;

@Getter
public enum TranscriptionErrorMessages {

    TRANSCRIPTION_USER_ALREADY_EXISTS("transcriptionUserAlreadyExists"),
    NO_USER_WITH_GIVEN_EMAIL("noUserWithGivenEmail"),
    TRANSCRIPTION_GROUP_ALREADY_EXISTS("transcriptionGroupAlreadyExists"),
    GROUP_NOT_FOUND("groupNotFound"),
    AT_LEAST_ONE_TITLE_IS_REQUIRED("atLeastOneTitleIsRequired"),
    MALFORMED_URL("malformedUrl"),
    URL_TOO_LONG("urlTooLong"),
    FIELD_REQUIRED("fieldRequired"),
    EMPTY_METADATA("emptyMetadata");
    private final String message;

    TranscriptionErrorMessages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}

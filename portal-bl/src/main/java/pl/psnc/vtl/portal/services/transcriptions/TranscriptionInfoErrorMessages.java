package pl.psnc.vtl.portal.services.transcriptions;

import lombok.Getter;

@Getter
public enum TranscriptionInfoErrorMessages {
    INVALID_CONTACT_EMAIL("transcriptionInfo.invalidContactEmail");


    private final String message;
    TranscriptionInfoErrorMessages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}

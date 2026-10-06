package pl.psnc.vtl.ocr.api.error;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum ErrorCode {
    OCR_RECOGNITION_FAILURE("Failed to perform OCR recognition on the image (transcription: %d, page: %s, region: %s)"),
    OCR_LANG_NOT_SUPPORTED("OCR language not supported: %s"),
    TRANSCRIPTION_PAGE_INVALID("Invalid transcription page: filename = %s, page no = %d"),
    TRANSCRIPTION_IMAGE_FAILED_TO_LOAD("Failed to load transcription image: filename=%s, page=%d"),
    TRANSCRIPTION_IMAGE_NOT_FOUND("Transcription image not found: transcription=%s, image=%s");

    private final String messageFormat;

    public String getMessage(Object... msgArgs) {
        return String.format(messageFormat, msgArgs);
    }
}

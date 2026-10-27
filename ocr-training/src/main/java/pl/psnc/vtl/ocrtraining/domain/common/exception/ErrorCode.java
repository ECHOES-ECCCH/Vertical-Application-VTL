package pl.psnc.vtl.ocrtraining.domain.common.exception;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum ErrorCode {
    IIIF_IMAGE_DOWNLOAD_FAILED("Failed to download the image from IIIF server"),
    IIIF_MALFORMED_URL("Invalid format of IIIF image URL: %s"),
    TRAINING_IMAGE_SAVE_FAILED("Failed to save the training files (image file: %s, text file: %s)"),
    OCR_TRAINING_ALREADY_IN_PROGRESS("OCR training is already in progress. Only one training task can be performed at the same time."),
    TASK_SCHEDULER_INIT_FAILED("Failed to initialize task scheduler"),
    TASK_SCHEDULING_FAILED("Failed to schedule task: %s (mode = %s)"),
    API_OCR_CORRECTION_INVALID_NUMBER("Invalid number of OCR corrections. Should be at least 1 and 100 at most"),
    API_OCR_CORRECTION_UNSUPPORTED_LANG("Unsupported OCR languages: %s");

    private final String msgPattern;

    public String getMessage(Object... msgArgs) {
        return String.format(msgPattern, msgArgs);
    }
}

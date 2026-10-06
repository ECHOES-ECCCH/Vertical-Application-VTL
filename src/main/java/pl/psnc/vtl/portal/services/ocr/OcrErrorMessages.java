package pl.psnc.vtl.portal.services.ocr;

import lombok.Getter;

@Getter
public enum OcrErrorMessages {
	PAGE_NOT_FOUND("transcription.recognition.error.page.not.found"),
	PAGE_RANGE_INVALID("transcription.recognition.error.page.range.invalid");

	private final String errorMessage;

	OcrErrorMessages(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}

package pl.psnc.vtl.portal.model.verses;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Learning status of student verse.
 */
public enum LearningStatus {
	NONE,

	CORRECT,

	PARTIAL_CORRECT,

	INCORRECT;

	@JsonValue
	public String getValue() {
		return name().toLowerCase();
	}

	@Override
	public String toString() {
		return getValue();
	}

}

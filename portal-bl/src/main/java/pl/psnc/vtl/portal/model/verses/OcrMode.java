package pl.psnc.vtl.portal.model.verses;

/**
 * Mode of the OCR operation.
 */
public enum OcrMode {
    /** Replace existing verses */
    OVERRIDE,
    /** Update existing empty verses */
    UPDATE,
    /** Only OCR empty pages */
    EMPTYONLY
}

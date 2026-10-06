package pl.psnc.vtl.portal.services.verses;

import pl.psnc.vtl.portal.services.I18nException;

public class UndoRedoException extends I18nException {

    private static final String MESSAGE_PREFIX = "undoredo.error.";

    public UndoRedoException(String message, String messageCode) {
        super(message, MESSAGE_PREFIX + messageCode);
    }
}

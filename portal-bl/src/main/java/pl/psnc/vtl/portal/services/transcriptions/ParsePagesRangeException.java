package pl.psnc.vtl.portal.services.transcriptions;

import pl.psnc.vtl.portal.services.I18nException;

public class ParsePagesRangeException extends I18nException {

	public ParsePagesRangeException(String range, String messageCode) {
		super("Supplied range of pages '" + range + "' is incorrect.", messageCode);
	}
}

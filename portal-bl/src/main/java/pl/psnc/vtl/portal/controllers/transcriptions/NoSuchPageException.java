package pl.psnc.vtl.portal.controllers.transcriptions;

import pl.psnc.vtl.portal.services.I18nException;

public class NoSuchPageException extends I18nException {
	public NoSuchPageException(String pages, String messageCode) {
		super("Page(s) number "+ pages + " do not exist!", messageCode);
	}
}

package pl.psnc.vtl.portal.conf.logging;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.MDC;

import static pl.psnc.vtl.portal.conf.logging.Slf4jMDCFilter.DEFAULT_MDC_ACTION_KEY;

public enum LogAction {

    LOGIN("Login"),
    MODIFY_DATA("Modify data"),
    REMOVE_DATA("Remove data"),
    CREATE_TRANSCRIPTION("Create transcription"),
    MODIFY_TRANSCRIPTION("Modify transcription"),
    REMOVE_TRANSCRIPTION("Remove transcription");

    @Getter private final String name;

    LogAction(String name) {
        this.name = name;
    }

    public static void setMDCAction(LogAction action) {
        MDC.put(DEFAULT_MDC_ACTION_KEY, action.getName());
    }

    public static void clearMDCAction() {
        MDC.remove(DEFAULT_MDC_ACTION_KEY);
    }

    public void info(Logger log, String msg, Object... arguments) {
        setMDCAction(this);
        log.info(msg, arguments);
        clearMDCAction();
    }

    public void debug(Logger log, String msg, Object... arguments) {
        setMDCAction(this);
        log.debug(msg, arguments);
        clearMDCAction();
    }

}

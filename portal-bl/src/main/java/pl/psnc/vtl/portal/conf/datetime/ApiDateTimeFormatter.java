package pl.psnc.vtl.portal.conf.datetime;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class ApiDateTimeFormatter {

    public final String dateTimePattern = "yyyy-MM-dd HH:mm";

    public DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);

    public String formatDateTime(LocalDateTime date) {
        return date.format(dateTimeFormatter);
    }

}

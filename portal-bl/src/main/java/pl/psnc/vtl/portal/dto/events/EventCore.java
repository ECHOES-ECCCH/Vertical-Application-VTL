package pl.psnc.vtl.portal.dto.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import pl.psnc.vtl.portal.conf.datetime.ApiDateTimeFormatter;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventCore {
    private String name;

    private String description;

    private String openingInfo;

    private List<String> organizerEmails;

    private boolean announced;

    private boolean openForEnrollment;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime endDate;
}

package pl.psnc.vtl.portal.dto.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.psnc.vtl.portal.model.events.Event;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaginatedEvents {

    private long hits;

    private int page;

    private int perpage;

    private int pages;

    private List<Event> events;

}

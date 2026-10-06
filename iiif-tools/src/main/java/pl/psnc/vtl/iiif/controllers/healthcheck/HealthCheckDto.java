package pl.psnc.vtl.iiif.controllers.healthcheck;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Health check dto object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckDto {

    /**
     * Number of active threads.
     */
    private long threads = Thread.activeCount();

    /**
     * Number of active tasks.
     */
    private int activeTasks = 0;

    /**
     * Is busy.
     */
    private boolean isBusy = false;
}

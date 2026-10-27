package pl.psnc.vtl.ocr.api.healthcheck;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckDto {
    /**
     * active threads
     */
    private long threads = Thread.activeCount();
    /**
     * number of active ocr task
     */
    private int activeOCRTasks = 0;
    private boolean isBusy=false;
}

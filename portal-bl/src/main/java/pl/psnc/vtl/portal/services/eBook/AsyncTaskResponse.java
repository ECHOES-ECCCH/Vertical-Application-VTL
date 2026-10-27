package pl.psnc.vtl.portal.services.eBook;

import lombok.Data;

@Data
public class AsyncTaskResponse {
    private final Status status;
    private final String details;
    private volatile int progress;

    public AsyncTaskResponse(Status status, String details) {
        this.status = status;
        this.details = details;
        this.progress = status == Status.READY ? 100 : 0;
    }
}

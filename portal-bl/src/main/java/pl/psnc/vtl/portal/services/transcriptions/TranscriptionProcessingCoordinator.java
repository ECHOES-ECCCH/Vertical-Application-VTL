package pl.psnc.vtl.portal.services.transcriptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.services.I18nException;
import pl.psnc.vtl.portal.services.eBook.AsyncTaskResponse;
import pl.psnc.vtl.portal.services.eBook.Status;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Slf4j
public class TranscriptionProcessingCoordinator {

    private static AsyncTaskResponse NOT_EXISTS = new AsyncTaskResponse(Status.NOT_EXISTS, null);

    private Map<Long, AsyncTaskResponse> processingInfo = new ConcurrentHashMap<>();

    public void startProcessing(long transcritpionId, String operation) {
        processingInfo.compute(transcritpionId, (k, prevAtr) -> {
            if (prevAtr != null && prevAtr.getStatus() == Status.PROCESSING) {
                throw new TranscriptionProcessingInProgressException("Operation in progress: " + prevAtr.getDetails());
            }
            return new AsyncTaskResponse(Status.PROCESSING, operation);
        });
    }

    public void progress(long transcriptionId, String operation, int progress) {
        AsyncTaskResponse atr = processingInfo.get(transcriptionId);
        if (atr != null && operation.equals(atr.getDetails()) && atr.getStatus() == Status.PROCESSING) {
            atr.setProgress(progress);
        } else {
            log.warn("Tried to set progress on {} for transcription {} but state was {}",
                    transcriptionId, operation, atr);
        }
    }

    public void processingFinished(long transcriptionId, String operation, Exception exception) {
        processingInfo.compute(transcriptionId, (k, atr) -> {
            if (atr == null || atr.getStatus() != Status.PROCESSING || !operation.equals(atr.getDetails())) {
                throw new RuntimeException("Tried to stop " + operation + " for transcription " + transcriptionId
                        + ", but state was: " + atr);
            }
            if (exception == null) {
                return new AsyncTaskResponse(Status.READY, operation);
            } else {
                String messageCode = exception instanceof I18nException
                        ? ((I18nException) exception).getMessageCode() : "unknownerror";
                return new AsyncTaskResponse(Status.FAILED, messageCode);
            }
        });
    }

    public AsyncTaskResponse getProcessing(long transcriptionId) {
        return processingInfo.getOrDefault(transcriptionId, NOT_EXISTS);
    }

    public void acknowledgeStatus(long transcriptionId) {
        processingInfo.compute(transcriptionId, (k, atr) -> {
            if (atr != null && atr.getStatus() != Status.PROCESSING)
                return null;
            return atr;
        });
    }
}

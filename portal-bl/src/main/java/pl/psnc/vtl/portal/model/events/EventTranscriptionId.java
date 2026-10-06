package pl.psnc.vtl.portal.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventTranscriptionId implements Serializable {
    private long event;
    private long transcription;
}

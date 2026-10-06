package pl.psnc.vtl.portal.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

import javax.persistence.*;

@Entity
@IdClass(EventTranscriptionId.class)
@Table(name = "event_transcriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTranscription {
    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private Event event;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    private Transcription transcription;
}

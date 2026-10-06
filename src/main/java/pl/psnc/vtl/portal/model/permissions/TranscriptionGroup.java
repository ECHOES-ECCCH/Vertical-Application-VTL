package pl.psnc.vtl.portal.model.permissions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.users.Group;

import javax.persistence.*;

/**
 * Transcription - Group relation
 */
@Entity
@IdClass(TranscriptionGroupId.class)
@Table(name = "transcription_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranscriptionGroup {

    /**
     * Transcription that given group belongs to.
     */
    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name="transcription_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Transcription transcription;

    /**
     * Group being part of given transcription.
     */
    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name="group_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Group group;

}

package pl.psnc.vtl.portal.model.transcriptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import pl.psnc.vtl.portal.conf.datetime.ApiDateTimeFormatter;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUserId;
import pl.psnc.vtl.portal.model.users.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Contains information of last transcription that user made changes in.
 */
@Entity
@Table(name = "user_last_transcriptions")
@IdClass(TranscriptionUserId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLastTranscription {

    /**
     * User being checked.
     */
    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    /**
     * Lastly edited transcription by specified user.
     */
    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "transcription_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Transcription transcription;

    /**
     * Date when last modification occurred.
     */
    @UpdateTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime lastModificationDate;

}

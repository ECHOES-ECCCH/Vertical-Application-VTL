package pl.psnc.vtl.portal.model.transcriptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUserId;
import pl.psnc.vtl.portal.model.users.User;

import javax.persistence.*;

/**
 * Contains information of last page that user made changes in.
 */
@Entity
@IdClass(TranscriptionUserId.class)
@Table(name = "transcription_user_last_pages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionUserLastPage {

    /**
     * Transcription that contains lastly edited page.
     */
    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "transcription_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Transcription transcription;

    /**
     * User being checked.
     */
    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    /**
     * Last page that was edited.
     */
    @Basic
    @Column(nullable = false)
    private Long lastPage;

}

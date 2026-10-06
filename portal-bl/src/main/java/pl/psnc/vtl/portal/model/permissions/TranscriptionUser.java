package pl.psnc.vtl.portal.model.permissions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.model.users.UserRole;

import javax.persistence.*;

//TODO: duplicate user-transcription mapping  with table transcription_members => @Transcription.members
/**
 * Transcription - User connection.
 * User can have only 1 role.
*/
@Entity
@IdClass(TranscriptionUserId.class)
@Table(name = "transcription_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranscriptionUser {

    /**
     * Transcription that given user is member of.
     */
    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "transcription_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Transcription transcription;

    /**
     * User that is member of given transcription.
     */
    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    /**
     * Users role in transcription.
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "user_role_code", referencedColumnName = "code")
    private UserRole role;

}

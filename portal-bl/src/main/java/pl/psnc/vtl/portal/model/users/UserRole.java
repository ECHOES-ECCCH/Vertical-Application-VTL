package pl.psnc.vtl.portal.model.users;

import lombok.*;

import javax.persistence.*;

/**
 * User role in the transcription.
 */
@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    /**
     * Code
     */
    @Id
    private String code;

    /**
     * Ord
     */
    @Basic
    @Column(nullable = false)
    private Integer ord;

    /**
     * Pl
     */
    @Column
    private String pl;

    /**
     * En
     */
    @Column
    private String en;

}

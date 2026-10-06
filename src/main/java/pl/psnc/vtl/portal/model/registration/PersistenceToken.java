package pl.psnc.vtl.portal.model.registration;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.users.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Persistence token object.
 */
@Entity
@Table(name = "persistence_tokens")
@Data
@NoArgsConstructor
public class PersistenceToken {

    /**
     * Token value.
     */
    @Id
    private String token;

    /**
     * User that token points to.
     */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumn(nullable = false)
    private User user;

    /**
     * Expiration date od the token.
     */
    @Basic
    @Column(nullable = true)
    private LocalDateTime expiryDate;

    /**
     * Type of the token.
     */
    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;
}

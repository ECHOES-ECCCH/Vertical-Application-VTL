package pl.psnc.vtl.portal.security.adfs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.users.User;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "adfsUsers")
public class ADFSUser {

    @Id
    String uuid;
    @Column(nullable = false)
    String email;

    @OneToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumn
    User user;

}

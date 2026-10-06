package pl.psnc.vtl.portal.model.transcriptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Metadata bibliographic type of the transcription.
 */
@Entity
@Table(name = "bibl_types")
@Getter
@Setter
@NoArgsConstructor
public class BiblType {

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

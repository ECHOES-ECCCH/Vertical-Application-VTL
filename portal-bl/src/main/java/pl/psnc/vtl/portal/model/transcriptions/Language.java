package pl.psnc.vtl.portal.model.transcriptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Metadata language.
 */
@Entity
@Table(name = "languages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Language {

    /**
     * Code
     */
    @Id
    private String code;

    /**
     * Ord
     */
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

    @Column
    private Boolean forTranscriptionMetadata;

}

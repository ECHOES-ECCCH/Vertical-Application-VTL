package pl.psnc.vtl.portal.model.verses;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Parameter;
import pl.psnc.vtl.portal.conf.datetime.ApiDateTimeFormatter;
import pl.psnc.vtl.portal.model.users.User;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;

import java.time.LocalDateTime;

/**
 * Verse comment.
 */
@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_generator")
    @GenericGenerator(name = "comment_generator",
            strategy = "pl.psnc.vtl.portal.repositories.persistence.RestorableSequenceGenerator",
            parameters = { @Parameter(name = "sequence_name", value = "comments_id_seq") })
    private Long id;

    /**
     * Verse that this comment points to.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Verse verse;

    /**
     * Content of the comment.
     */
    @Basic
    @Column(nullable = false, length = 2048)
    private String content;

    /**
     * Creator of the comment.
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private User creator;

    /**
     * Creation date of the comment.
     */
    @Basic
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private LocalDateTime dateCreated;

}

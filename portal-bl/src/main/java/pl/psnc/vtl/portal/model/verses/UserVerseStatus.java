//package pl.psnc.vtl.portal.model.verses;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import org.hibernate.annotations.ColumnDefault;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.OnDelete;
//import org.hibernate.annotations.OnDeleteAction;
//import pl.psnc.vtl.portal.conf.datetime.ApiDateTimeFormatter;
//import pl.psnc.vtl.portal.model.users.User;
//import pl.psnc.vtl.portal.services.layers.CustomVerseListSerializer;
//
//import javax.persistence.*;
//import java.io.Serializable;
//import java.time.LocalDateTime;
//import java.util.Date;
//
//@Entity
////@Table(name = "user_verse_status", uniqueConstraints = {
////        @UniqueConstraint(columnNames = {"user_id", "verse_id"} ) })
//
//@Table(name = "user_verse_status")
//@NoArgsConstructor
//@Getter
//@Setter
//public class UserVerseStatus {
//
//
//    public UserVerseStatus(User user, Verse verse) {
//        this.user = user;
//        this.verse = verse;
//        this.updateDate =   LocalDateTime.now();
//        this.id=new UserVerseStatus.Id(user.getId(),verse.getId());
//    }
//
//    @Embeddable
//    public static class Id implements Serializable {
//        @Column(name = "user_id")
//        private Long userId;
//
//        @Column(name = "verse_id")
//        private Long verseId;
//
//        public Id() {
//        }
//
//        public Id(Long userId, Long verseId) {
//            this.userId = userId;
//            this.verseId = verseId;
//        }
//
//
//        public boolean equals(Object o) {
//            if (o instanceof UserVerseStatus.Id) {
//                UserVerseStatus.Id that = (UserVerseStatus.Id) o;
//                return this.userId.equals(that.userId)
//                        && this.verseId.equals(that.verseId);
//            }
//            return false;
//        }
//    }
//
//    @EmbeddedId
//    @JsonIgnore
//    private UserVerseStatus.Id id = new UserVerseStatus.Id();
//
//
//    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
//    @JoinColumn(
//            name = "verse_id",
//            insertable = false, updatable = false
//    )
//    @JsonSerialize(using = CustomVerseListSerializer.class)
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    private Verse verse;
//
//    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
//    @JoinColumn(
//            name = "user_id",
//            insertable = false, updatable = false
//    )
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    private User user;
//
//    @Basic
//    @Column(nullable = false)
//    @ColumnDefault("0")
//    private Integer correctCount = 0;
//
//    @Basic
//    @Column(nullable = false)
//    @ColumnDefault("0")
//    private Integer rejectCount = 0;
//
//
//    @Basic
//    @Column(nullable = true)    //TODO: make nonnullable
//    @CreationTimestamp
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
//    private LocalDateTime updateDate;
//
//}

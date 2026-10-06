package pl.psnc.vtl.portal.dto.events;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.psnc.vtl.portal.model.users.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class EventTeamCore {

    private final Long id;
    private final String name;
    private List<Member> members;

    private Map<Long, List<Long>> transcriptionPages;

    private int characterCount;
    private int wordCount;
    private int verseCount;

    public EventTeamCore(Long id, String name) {
        this.id = id;
        this.name = name;
        this.members = new ArrayList<>();
        this.transcriptionPages = new LinkedHashMap<>();
    }

    @Data
    @NoArgsConstructor
    public static class Member {
        private String name;
        private Long userId;
        private String email;

        public Member(User user, boolean includeEmail) {
            this.name = user.getName();
            this.userId = user.getId();
            this.email = includeEmail ? user.getEmail() : null;
        }
    }
}

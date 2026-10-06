package pl.psnc.vtl.portal.dto.registration;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Builder
@Getter
public class UserRegistrationErrorResponse {

    @Singular
    private Map<String, String> messages;

    public boolean isEmpty() {
        return messages.isEmpty();
    }
}

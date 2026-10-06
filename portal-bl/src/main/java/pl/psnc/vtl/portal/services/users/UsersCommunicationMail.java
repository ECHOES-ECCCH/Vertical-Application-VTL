package pl.psnc.vtl.portal.services.users;

import lombok.AccessLevel;
import lombok.Value;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;

@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
public class UsersCommunicationMail {
    @NotNull Long receiverId;
    @NotNull String subject;
    @NotNull String message;
    @NotNull String captcha;
}

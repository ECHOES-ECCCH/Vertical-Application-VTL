package pl.psnc.vtl.portal.dto.registration;

import lombok.Data;
import pl.psnc.vtl.portal.controllers.ErrorMessage;

@Data
public class UserRegisterErrorMessages {

    private ErrorMessage username;

    private ErrorMessage password;

    private ErrorMessage repeatedPassword;

    private ErrorMessage email;

    private ErrorMessage acceptedRegulations;



}

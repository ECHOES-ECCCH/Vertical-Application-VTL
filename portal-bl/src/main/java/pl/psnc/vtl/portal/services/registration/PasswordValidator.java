package pl.psnc.vtl.portal.services.registration;

import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.regex.Pattern;

@UtilityClass
public class PasswordValidator {


    private Pattern PASSWORD_PATTERN = Pattern
            .compile("^(?=.*[a-z])(?=.*[A-Z])((?=.*[@#$%^&+=])|(?=.*[0-9]))(?=\\S+$).{8,}$");


    /**
     * Validates that contains at least one capital letter, one small letter, one digit or special sign and min 8 chars.
     */
    public boolean isValidPassword(String password){
        return  PASSWORD_PATTERN.matcher(password).matches();
    }


    public boolean isValidPasswordMatches(String password, String repeatedPassword){
        return password.equals(repeatedPassword);
    }


    public boolean isValidPasswordHash(String currentPassword, String passwordHash){
        return BCrypt.checkpw(currentPassword, passwordHash);
    }

}

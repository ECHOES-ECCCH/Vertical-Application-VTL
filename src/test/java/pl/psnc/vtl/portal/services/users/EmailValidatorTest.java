package pl.psnc.vtl.portal.services.users;

import org.junit.Test;

import static org.junit.Assert.*;

public class EmailValidatorTest {

    @Test
    public void isValidEmail() {
        //
        assertTrue(EmailValidator.isValidEmail("",false));
        assertTrue(EmailValidator.isValidEmail(null,false));
        assertFalse(EmailValidator.isValidEmail("",true));
        assertFalse(EmailValidator.isValidEmail(null,true));
        //test defaults
        assertFalse(EmailValidator.isValidEmail(""));
        assertFalse(EmailValidator.isValidEmail(null));
        //
        assertFalse(EmailValidator.isValidEmail("test",false));
        assertFalse(EmailValidator.isValidEmail("test",true));
        assertFalse(EmailValidator.isValidEmail("test@@o2.pl",false));
        assertFalse(EmailValidator.isValidEmail("test@@gmail.com",true));
        //
        assertTrue(EmailValidator.isValidEmail("test@o2.pl",false));
        assertTrue(EmailValidator.isValidEmail("test@gmail.com",true));
    }
}
package pl.psnc.vtl.portal.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginCore {

    @NotBlank
    private String email;

    @NotBlank
    private String password;

}

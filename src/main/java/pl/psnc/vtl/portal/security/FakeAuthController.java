package pl.psnc.vtl.portal.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.security.LoginCore;

//this is fake controller

@RestController
@RequestMapping("/api")
@Tag(name ="Sign-in")
class FakeAuthController {
    @Operation(summary = "Sign into the system")
    @PostMapping("/auth/sign-in")
    void signIn(@RequestBody LoginCore core, @RequestParam(value = "remember-me", required = false) String rememberMe){}

    @Operation(summary = "Sign into the system using federated sign-in mechanism")
	@PostMapping("/auth/fed-sign-in")
	void fedSignIn(@RequestParam String email, @RequestParam(value = "remember-me", required = false) String rememberMe){}

    @Operation(summary = "Sign into the system using oauth2; redirect_url must correspond to clients url;" +
            " at least one oauth2 client(google/facebook must be specified)")
    @GetMapping("/oauth2/authorization/{client}")
    void oauth2(@PathVariable String client,
                @RequestParam(value = "redirect_url") String redirect_url){}

}

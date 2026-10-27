package pl.psnc.vtl.portal.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;
import pl.psnc.vtl.portal.security.CustomOAuth2UserService;


@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@RestController
public class Oauth2Controller {

    @GetMapping(path = "/success")
    public ResponseEntity<String> success() {
        return ResponseEntity.ok("success");
    }

    @GetMapping(path = "/accessToken")
    public ResponseEntity<String> accessToken() {
        OAuth2AuthenticationToken auth =
                (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        OAuth2AccessToken token = auth.getPrincipal().getAttribute(CustomOAuth2UserService.USER_ATTRIBUTE_TOKEN);
        return ResponseEntity.ok(token.getTokenValue());
    }
}

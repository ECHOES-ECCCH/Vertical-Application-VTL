package pl.psnc.vtl.portal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import pl.psnc.vtl.portal.conf.logging.LogAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            AuthenticationException e)
            throws IOException {

        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());

        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", LocalDateTime.now());
        data.put("exception", e.getMessage());

        objectMapper.findAndRegisterModules();
        httpServletResponse.getOutputStream().println(objectMapper.writeValueAsString(data));

        String userUsername = httpServletRequest.getAttribute("username").toString();

        LogAction.setMDCAction(LogAction.LOGIN);
        log.info(String.format("FAILURE - normal login for %s, login date: %s, client IP: %s",  userUsername,
                LocalDateTime.now(), httpServletRequest.getRemoteAddr()));
        LogAction.clearMDCAction();
    }

}

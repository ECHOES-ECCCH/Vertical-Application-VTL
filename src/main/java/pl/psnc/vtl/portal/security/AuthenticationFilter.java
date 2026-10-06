package pl.psnc.vtl.portal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private ObjectMapper mapper = new ObjectMapper();
    public AuthenticationFilter() {
        super();
    }

    private LoginCore loginDto;

    private boolean postOnly = true;


    @Override
    protected String obtainPassword(HttpServletRequest request) {
        return getLoginDTO(request).getPassword();
    }

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        return getLoginDTO(request).getEmail();
    }

    /*
    loginCore will be set to null after second read so that multiple authentication can be performed,
    due to limitation of httpServlet wrapper and possibility to call getReader only ones per request
    sort of caching was introduced
     */
    private LoginCore getLoginDTO(HttpServletRequest request) {
        if (loginDto == null)
            return loadDTOFromRequest(request);
        LoginCore loginCore = this.loginDto;
        this.loginDto = null;
        return loginCore;
    }

    @SneakyThrows
    private LoginCore loadDTOFromRequest(HttpServletRequest request) {
        RequestWrapper requestWrapper = new RequestWrapper(request);
        this.loginDto = mapper.readValue(requestWrapper.getBody(), LoginCore.class);
        return this.loginDto;
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            String username = this.obtainUsername(request);
            String password = this.obtainPassword(request);
            if (username == null) {
                username = "";
            }

            if (password == null) {
                password = "";
            }
            request.setAttribute("username", username);
            username = username.trim();
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
            this.setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        }
    }

    @Override
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

}

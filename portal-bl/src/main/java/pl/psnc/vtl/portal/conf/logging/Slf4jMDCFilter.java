package pl.psnc.vtl.portal.conf.logging;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
@Slf4j
public class Slf4jMDCFilter extends OncePerRequestFilter {

    public static final String DEFAULT_RESPONSE_TOKEN_HEADER = "RequestId";
    public static final String DEFAULT_MDC_UUID_TOKEN_KEY = "BAD.RequestId";
    private static final String DEFAULT_MDC_USERNAME_KEY = "BAD.Username";
    private static final String DEFAULT_MDC_IP_KEY = "BAD.IP";
    public static final String DEFAULT_MDC_ACTION_KEY = "BAD.Action";

    private final LoggedInUserHolder loggedInUserHolder;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws java.io.IOException, ServletException {

        try {
            addRequestId(request);
            addUsernameAndIP(request);

            log.info("path: {}, method: {}", request.getRequestURI(), request.getMethod());
            response.setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY));

            chain.doFilter(request, response);
        } finally {
            log.debug("statusCode {}, path: {}, method: {}", response.getStatus(), request.getRequestURI(), request.getMethod());
            MDC.clear();
        }
    }

    private void addRequestId(HttpServletRequest request) {
        MDC.put(DEFAULT_MDC_UUID_TOKEN_KEY, UUID.randomUUID().toString().toUpperCase().replace("-", ""));
    }

    private void addUsernameAndIP(HttpServletRequest request) {
        if (loggedInUserHolder.getLoggedInUser() != null) {
            MDC.put(DEFAULT_MDC_USERNAME_KEY, loggedInUserHolder.getLoggedInUser().getEmail());
        }

        if (request != null) {
            MDC.put(DEFAULT_MDC_IP_KEY, request.getRemoteAddr());
        }
    }
}

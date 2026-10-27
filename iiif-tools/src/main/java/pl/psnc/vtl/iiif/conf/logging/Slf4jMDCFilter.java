package pl.psnc.vtl.iiif.conf.logging;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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

    private static final String DEFAULT_RESPONSE_TOKEN_HEADER = "RequestId";
    private static final String DEFAULT_MDC_UUID_TOKEN_KEY = "BAD.RequestId";

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws java.io.IOException, ServletException {

        try {
            addRequestId(request);

            log.info("path: {}, method: {}, query: {}", request.getRequestURI(), request.getMethod(), request.getQueryString());
            response.setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY));

            chain.doFilter(request, response);
        } finally {
            log.debug("statusCode {}, path: {}, method: {}, query: {}", response.getStatus(), request.getRequestURI(), request.getMethod(), request.getQueryString());
            MDC.clear();
        }
    }

    private void addRequestId(HttpServletRequest request) {
        String xRequestId = request.getHeader(DEFAULT_RESPONSE_TOKEN_HEADER);
        if (xRequestId == null) {
            MDC.put(DEFAULT_MDC_UUID_TOKEN_KEY, UUID.randomUUID().toString().toUpperCase().replace("-", ""));
        } else {
            MDC.put(DEFAULT_MDC_UUID_TOKEN_KEY, xRequestId);
        }
    }
}

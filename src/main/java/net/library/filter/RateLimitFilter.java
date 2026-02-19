package net.library.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.library.service.RateLimitService;
import net.library.util.Utils;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static net.library.exception.ErrorId.TOO_MANY_REQUESTS_ID;
import static net.library.exception.ErrorMessage.TOO_MAY_REQUESTS;
import static net.library.util.HttpUtil.*;

@Component
@Order(2)
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {
    private final RateLimitService rateLimitService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) request;

        if ((GLOBAL_BASE_URI + "auth/login").equals(httpRequest.getRequestURI())
                && POST.equals(httpRequest.getMethod())) {
            var clientId = Utils.getClientIdentifier(httpRequest);

            if (!rateLimitService.isAllowed(clientId)) {
                var httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(TOO_MANY_REQUESTS_ID);
                httpResponse.setContentType(APPLICATION_JSON);
                httpResponse.getWriter().write(Utils.getJsonErrorBody(MDC.get(CORRELATION_ID_HEADER_NAME), TOO_MANY_REQUESTS_ID, TOO_MAY_REQUESTS));
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
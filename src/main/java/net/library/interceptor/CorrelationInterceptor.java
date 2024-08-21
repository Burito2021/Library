package net.library.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.library.exception.Constants;
import net.library.exception.MdcUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import static net.library.exception.Constants.*;
import static net.library.exception.MdcUtils.getOrMakeCid;

public class CorrelationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@Nullable final HttpServletRequest request,
                             @Nullable final HttpServletResponse response,
                             @Nullable final Object handler) {
        final var cid = getOrMakeCid(() -> request.getHeader(CORRELATION_ID_HEADER_NAME));
        MdcUtils.initMdcCid(cid);
        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final Object handler,
                                final Exception ex) {
        MdcUtils.clearMdcCid();
    }
}

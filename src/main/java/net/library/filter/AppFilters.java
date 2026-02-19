package net.library.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.library.util.MdcUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static net.library.util.HttpUtil.CORRELATION_ID_HEADER_NAME;
import static net.library.util.MdcUtils.getOrMakeCid;

@Component
@Order(1)
public class AppFilters implements jakarta.servlet.Filter {

    @Override
    public void doFilter(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse, jakarta.servlet.FilterChain chain) throws IOException, jakarta.servlet.ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        final var cid = getOrMakeCid(() -> httpRequest.getHeader(CORRELATION_ID_HEADER_NAME));
        MdcUtils.initMdcCid(cid);

        httpResponse.setHeader(CORRELATION_ID_HEADER_NAME, cid);

        try {
            chain.doFilter(servletRequest, servletResponse);
        } finally {
            MdcUtils.clearMdcCid();
        }
    }
}

package com.chainauth.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticationRateLimitFilterTests {
    @Test void limitsRepeatedAuthenticationRequestsByAddressAndRoute() throws Exception {
        AuthenticationRateLimitFilter filter=new AuthenticationRateLimitFilter(1,60_000);
        AtomicInteger passed=new AtomicInteger();
        FilterChain chain=(request,response)->passed.incrementAndGet();
        MockHttpServletRequest first=new MockHttpServletRequest("POST","/api/auth/login");first.setRemoteAddr("192.0.2.10");
        MockHttpServletResponse firstResponse=new MockHttpServletResponse();filter.doFilter(first,firstResponse,chain);
        MockHttpServletRequest second=new MockHttpServletRequest("POST","/api/auth/login");second.setRemoteAddr("192.0.2.10");
        MockHttpServletResponse secondResponse=new MockHttpServletResponse();filter.doFilter(second,secondResponse,chain);
        assertEquals(1,passed.get());assertEquals(429,secondResponse.getStatus());assertNotNull(secondResponse.getHeader("Retry-After"));
    }
}

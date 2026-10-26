package com.cyberlearnix.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldPropagateIncomingTraceId() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("X-Trace-Id")).thenReturn("incoming-trace-999");

        filter.doFilter(request, response, chain);

        verify(request).setAttribute("X-Trace-Id", "incoming-trace-999");
        verify(response).setHeader("X-Trace-Id", "incoming-trace-999");
        verify(chain).doFilter(request, response);
        assertThat(MDC.get("traceId")).isNull(); // MDC is cleaned up in finally block
    }

    @Test
    void shouldGenerateNewTraceIdWhenMissing() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("X-Trace-Id")).thenReturn(null);

        filter.doFilter(request, response, chain);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(request).setAttribute(eq("X-Trace-Id"), captor.capture());
        verify(response).setHeader(eq("X-Trace-Id"), eq(captor.getValue()));
        verify(chain).doFilter(request, response);
        assertThat(captor.getValue()).isNotEmpty();
    }
}

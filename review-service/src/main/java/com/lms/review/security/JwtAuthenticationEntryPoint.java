package com.lms.review.security;

import com.cyberlearnix.error.ApiSecurityErrorWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ApiSecurityErrorWriter.write(request, response, HttpServletResponse.SC_UNAUTHORIZED,
                "UNAUTHORIZED", "Authentication required");
    }
}

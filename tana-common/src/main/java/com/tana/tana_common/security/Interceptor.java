package com.tana.tana_common.security;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tana.tana_common.constant.CommonConstants;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.functions.userdetails.UserDetailsImpl;
import com.tana.tana_common.functions.userdetails.UserDetailsServiceImpl;
import com.tana.tana_common.util.CommonUtils;
import com.tana.tana_common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.BufferedReader;
import java.util.*;
import java.time.Duration;
import com.github.benmanes.caffeine.cache.Ticker;

@Component
public class Interceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private CommonUtils commonUtils;

    private final SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(
        100, Duration.ofMinutes(1), 10_000, Ticker.systemTicker());
    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String client = request.getRemoteAddr();
        if (!rateLimiter.tryAcquire(client)) {
            response.setStatus(CustomCodeErrors.TOO_MANY_REQUESTS.getCode());
            response.getWriter().write(CustomCodeErrors.TOO_MANY_REQUESTS.getMessage());
            return false;
        }

        final String authHeader = request.getHeader("Authorization");
        String tokenString = CommonConstants.EMPTY_STRING;
        boolean publicAuthRequest = request.getRequestURI().startsWith("/api/auth/");
        if (!publicAuthRequest && authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenString = authHeader.substring(7); // remove "Bearer "
        }
        if (!ObjectUtils.isEmpty(tokenString)) {
            final String username = jwtUtil.extractUsername(tokenString);
            UserDetailsImpl userDetails =
                    (UserDetailsImpl) userDetailsServiceImpl.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("multipart")) {
            return true;
        }

        List<MethodParameter> requestAttributeParameters = new ArrayList<>();
        for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
            if (methodParameter.hasParameterAnnotation(RequestAttribute.class)) {
                requestAttributeParameters.add(methodParameter);
            }
        }

        if (requestAttributeParameters.isEmpty()) {
            return true;
        }

        // Read request body
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        String body = requestBody.toString().trim();

        for (MethodParameter methodParameter : requestAttributeParameters) {
            Class<?> paramType = methodParameter.getParameterType();

            Object rawObject;

            if (body.isEmpty()) {
                    // ✅ handle empty body safely
                rawObject = paramType.getDeclaredConstructor().newInstance();
            } else {
                    // ✅ normal JSON parsing
                rawObject = objectMapper.readValue(body, paramType);
            }

                // ✅ validate
            Object validatedObject = validateAndCast(rawObject);

                // ✅ inject
            request.setAttribute("validated", validatedObject);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           org.springframework.web.servlet.ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
    }

    @SuppressWarnings("unchecked")
    private <T> T validateAndCast(Object obj) {
        return (T) commonUtils.validateObjectData(obj);
    }
}

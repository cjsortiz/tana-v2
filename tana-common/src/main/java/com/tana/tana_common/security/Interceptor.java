package com.tana.tana_common.security;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.BufferedReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Interceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private CommonUtils commonUtils;

    private final Map<String, List<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private final int MAX_REQUESTS_PER_MINUTE = 100;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String client = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        requestTimestamps.putIfAbsent(client, new ArrayList<>());
        List<Long> timestamps = requestTimestamps.get(client);

        // Remove requests older than 1 minute
        timestamps.removeIf(time -> time + 60_000 < now);

        if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(CustomCodeErrors.TOO_MANY_REQUESTS.getCode());
            response.getWriter().write(CustomCodeErrors.TOO_MANY_REQUESTS.getMessage());
            return false;
        }

        timestamps.add(now);

        final String authHeader = request.getHeader("Authorization");
        String tokenString = CommonConstants.EMPTY_STRING;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
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

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("multipart")) {
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
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {

            if (methodParameter.hasParameterAnnotation(RequestAttribute.class)) {

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
        }
            return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           org.springframework.web.servlet.ModelAndView modelAndView) throws Exception {
        System.out.println("After handling the request");
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        System.out.println("After request completion");
    }

    @SuppressWarnings("unchecked")
    private <T> T validateAndCast(Object obj) {
        return (T) commonUtils.validateObjectData(obj);
    }
}

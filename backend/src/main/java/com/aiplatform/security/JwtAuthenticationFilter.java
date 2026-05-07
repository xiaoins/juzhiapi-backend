package com.aiplatform.security;

import com.aiplatform.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 支持两种认证方式:
 * 1. Bearer Token (JWT) - 用户登录后使用
 * 2. API Key - 外部工具调用 /v1/* 接口时使用
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();

        // OpenAI 兼容接口使用 API Key 认证
        if (requestPath.startsWith("/v1/")) {
            authenticateWithApiKey(request);
        } else {
            authenticateWithJwt(request);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * JWT Token 认证
     */
    private void authenticateWithJwt(HttpServletRequest request) {
        String token = extractToken(request);
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    /**
     * API Key 认证 (用于 /v1/ 接口)
     */
    private void authenticateWithApiKey(HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (!StringUtils.hasText(token)) return;

        // 尝试通过 API Key 认证
        Long userId = apiKeyService.validateAndGetUserId(token);
        if (userId != null) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    /**
     * 提取 Bearer Token
     */
    private String extractBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 提取 Token (兼容多种方式)
     */
    private String extractToken(HttpServletRequest request) {
        return extractBearerToken(request);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 跳过公开接口的过滤(由SecurityConfig控制)
        return path.startsWith("/api/auth/")
                || path.equals("/health")
                || path.equals("/ready")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/");
    }
}

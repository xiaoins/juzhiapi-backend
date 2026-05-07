package com.aiplatform.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查
 */
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    /**
     * 存活检查 (Liveness)
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        return result;
    }

    /**
     * 就绪检查 (Readiness) - 检查数据库和Redis连接
     */
    @GetMapping("/ready")
    public Map<String, Object> ready() {
        Map<String, Object> checks = new HashMap<>();

        // 检查数据库
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            checks.put("database", Map.of("status", "ok"));
        } catch (Exception e) {
            checks.put("database", Map.of("status", "error", "message", e.getMessage()));
        }

        // 检查 Redis
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            checks.put("redis", Map.of("status", "ok"));
        } catch (Exception e) {
            checks.put("redis", Map.of("status", "error", "message", e.getMessage()));
        }

        boolean allOk = checks.values().stream()
                .allMatch(m -> ((Map<?, ?>) m).get("status").equals("ok"));

        Map<String, Object> result = new HashMap<>();
        result.put("status", allOk ? "ok" : "degraded");
        result.put("checks", checks);
        return result;
    }
}

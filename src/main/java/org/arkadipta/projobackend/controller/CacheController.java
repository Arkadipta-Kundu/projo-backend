package org.arkadipta.projobackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getCacheHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            String pong = connection.ping();
            connection.close();

            health.put("redis", "UP");
            health.put("ping", pong);
            health.put("cacheNames", cacheManager.getCacheNames());

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("redis", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }

    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        try {
            cacheManager.getCacheNames()
                    .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "All caches cleared successfully");
            response.put("clearedCaches", cacheManager.getCacheNames());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to clear caches: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/clear/{cacheName}")
    public ResponseEntity<Map<String, Object>> clearSpecificCache(@PathVariable String cacheName) {
        try {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cache '" + cacheName + "' cleared successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to clear cache '" + cacheName + "': " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            stats.put("availableCaches", cacheManager.getCacheNames());
            stats.put("cacheCount", cacheManager.getCacheNames().size());
            stats.put("status", "operational");

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            stats.put("status", "error");
            stats.put("error", e.getMessage());
            return ResponseEntity.status(500).body(stats);
        }
    }
}

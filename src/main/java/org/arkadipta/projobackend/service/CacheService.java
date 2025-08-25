package org.arkadipta.projobackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CacheService {

    @Autowired(required = false)
    private CacheManager cacheManager;

    @CacheEvict(value = { "dashboard_stats", "upcoming_tasks", "recent_activity" }, allEntries = true)
    public void evictDashboardCaches() {
        // This method will clear dashboard-related caches when called
    }

    @CacheEvict(value = { "projects", "project", "project_task_count",
            "project_completed_task_count" }, allEntries = true)
    public void evictProjectCaches() {
        // This method will clear project-related caches when called
    }

    @CacheEvict(value = { "tasks_kanban", "tasks_gantt", "tasks_calendar", "task" }, allEntries = true)
    public void evictTaskCaches() {
        // This method will clear task-related caches when called
    }

    @CacheEvict(value = "user_details", allEntries = true)
    public void evictUserCaches() {
        // This method will clear user-related caches when called
    }

    public void evictAllCaches() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
    }

    public void evictSpecificCache(String cacheName, String key) {
        Objects.requireNonNull(cacheManager.getCache(cacheName)).evict(key);
    }
}

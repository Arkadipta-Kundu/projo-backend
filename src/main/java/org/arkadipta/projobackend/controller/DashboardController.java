package org.arkadipta.projobackend.controller;

import org.arkadipta.projobackend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173", "http://localhost:5174",
        "http://127.0.0.1:5174" })
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = dashboardService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/upcoming-tasks")
    public ResponseEntity<List<Map<String, Object>>> getUpcomingTasks(
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<Map<String, Object>> upcomingTasks = dashboardService.getUpcomingTasks(days);
            return ResponseEntity.ok(upcomingTasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> recentActivity = dashboardService.getRecentActivity(limit);
            return ResponseEntity.ok(recentActivity);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

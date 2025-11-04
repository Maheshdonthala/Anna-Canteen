package com.jsp.canteen_management_system.controller;

import com.jsp.canteen_management_system.dto.FoodLogRequest;
import com.jsp.canteen_management_system.dto.FoodLogStats;
import com.jsp.canteen_management_system.model.FoodLog;
import com.jsp.canteen_management_system.model.DailyFoodSummary;
import com.jsp.canteen_management_system.service.FoodLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/canteens/{canteenId}/foodlogs")
@RequiredArgsConstructor
public class FoodLogController {

    private final FoodLogService foodLogService;

    @PostMapping
    public ResponseEntity<FoodLog> saveFoodLog(@PathVariable String canteenId, @RequestBody FoodLogRequest request) {
        return ResponseEntity.ok(foodLogService.saveFoodLog(canteenId, request));
    }

    @GetMapping
    public ResponseEntity<List<FoodLog>> getFoodLogs(@PathVariable String canteenId, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate queryDate = (date == null) ? LocalDate.now() : date;
        return ResponseEntity.ok(foodLogService.getFoodLogsByCanteenAndDate(canteenId, queryDate));
    }

    @GetMapping("/stats")
    public ResponseEntity<FoodLogStats> getDailyStats(@PathVariable String canteenId) {
        return ResponseEntity.ok(foodLogService.getDailyStats(canteenId));
    }

    @GetMapping("/summary")
    public ResponseEntity<DailyFoodSummary> getDailySummary(@PathVariable String canteenId,
                                                            @RequestParam(required = false)
                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate d = (date == null) ? LocalDate.now() : date;
        // getDailyStats already ensures upsert, so call it to keep in sync, then read summary
        foodLogService.getDailyStats(canteenId);
        return ResponseEntity.of(foodLogService
                .getDailySummary(canteenId, d));
    }
}

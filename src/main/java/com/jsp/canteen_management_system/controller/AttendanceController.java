package com.jsp.canteen_management_system.controller;

import com.jsp.canteen_management_system.model.Attendance;
import com.jsp.canteen_management_system.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/canteen/{canteenId}/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    static class AttendanceRequest {
        public String workerId;
        public String status; // PRESENT/ABSENT
    }

    @PostMapping
    public Attendance mark(@PathVariable String canteenId, @RequestBody AttendanceRequest req) {
        return attendanceService.markAttendance(canteenId, req.workerId, req.status);
    }

    @GetMapping
    public List<Attendance> getAttendance(@PathVariable String canteenId) {
        return attendanceService.getAttendanceByCanteen(canteenId);
    }
}

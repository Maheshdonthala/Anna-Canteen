package com.jsp.canteen_management_system.service;

import com.jsp.canteen_management_system.dto.DashboardStats;
import com.jsp.canteen_management_system.model.Canteen;
import com.jsp.canteen_management_system.model.FoodLog;
import com.jsp.canteen_management_system.repository.AttendanceRepository;
import com.jsp.canteen_management_system.repository.FoodLogRepository;
import com.jsp.canteen_management_system.repository.SalaryRepository;
import com.jsp.canteen_management_system.repository.CanteenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private FoodLogRepository foodLogRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private CanteenRepository canteenRepository;

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        stats.setTotalSalesToday(calculateTodaySales());
        stats.setWorkersPresent(countWorkersPresentToday());
        stats.setSalariesPending(countPendingSalaries());

        return stats;
    }

    private double calculateTodaySales() {
        LocalDate today = LocalDate.now();
        List<FoodLog> logs = foodLogRepository.findAll().stream()
                .filter(log -> log.getDate() != null && log.getDate().equals(today))
                .toList();

        double total = 0.0;
        for (FoodLog log : logs) {
            Canteen canteen = canteenRepository.findById(log.getCanteenId()).orElse(null);
            if (canteen != null) {
                total += log.getPlatesSold() * canteen.getDefaultPlatePrice();
            }
        }
        return total;
    }

    private long countWorkersPresentToday() {
        LocalDate today = LocalDate.now();
        return attendanceRepository.countByDateAndStatus(today, "PRESENT");
    }

    private long countPendingSalaries() {
        return salaryRepository.countByStatus("PENDING");
    }
}

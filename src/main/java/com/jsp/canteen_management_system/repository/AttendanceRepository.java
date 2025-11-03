package com.jsp.canteen_management_system.repository;

import com.jsp.canteen_management_system.model.Attendance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {
    // You can add custom query methods here later
    long countByDateAndStatus(java.time.LocalDate date, String status);
    List<Attendance> findByCanteenIdOrderByDateDesc(String canteenId);
}

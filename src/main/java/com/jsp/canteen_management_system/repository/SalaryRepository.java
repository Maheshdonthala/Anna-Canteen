package com.jsp.canteen_management_system.repository;

import com.jsp.canteen_management_system.model.Salary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRepository extends MongoRepository<Salary, String> {
    long countByStatus(String status);
    List<Salary> findByCanteenIdAndStatusIgnoreCase(String canteenId, String status);
    Optional<Salary> findByCanteenIdAndWorkerIdAndMonthAndYear(String canteenId, String workerId, int month, int year);
}

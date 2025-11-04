package com.jsp.canteen_management_system.repository;

import com.jsp.canteen_management_system.model.DailyFoodSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyFoodSummaryRepository extends MongoRepository<DailyFoodSummary, String> {
    Optional<DailyFoodSummary> findByCanteenIdAndDate(String canteenId, LocalDate date);
}

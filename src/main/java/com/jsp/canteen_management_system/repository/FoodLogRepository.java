package com.jsp.canteen_management_system.repository;

import com.jsp.canteen_management_system.enums.MealType;
import com.jsp.canteen_management_system.model.FoodLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FoodLogRepository extends MongoRepository<FoodLog, String> {
    Optional<FoodLog> findByCanteenIdAndDateAndMealType(String canteenId, LocalDate date, MealType mealType);
    List<FoodLog> findByCanteenIdAndDate(String canteenId, LocalDate date);
}

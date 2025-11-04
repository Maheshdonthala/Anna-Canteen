package com.jsp.canteen_management_system.service;

import com.jsp.canteen_management_system.dto.FoodLogRequest;
import com.jsp.canteen_management_system.dto.FoodLogStats;
import com.jsp.canteen_management_system.model.Canteen;
import com.jsp.canteen_management_system.model.FoodLog;
import com.jsp.canteen_management_system.repository.CanteenRepository;
import com.jsp.canteen_management_system.repository.DailyFoodSummaryRepository;
import com.jsp.canteen_management_system.model.DailyFoodSummary;
import com.jsp.canteen_management_system.repository.FoodLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodLogService {

    private static final Logger logger = LoggerFactory.getLogger(FoodLogService.class);
    private final FoodLogRepository foodLogRepository;
    private final CanteenRepository canteenRepository;
    private final DailyFoodSummaryRepository dailyFoodSummaryRepository;

    public FoodLog saveFoodLog(String canteenId, FoodLogRequest request) {
        canteenRepository.findById(canteenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Canteen not found"));

        LocalDate today = LocalDate.now();
        FoodLog foodLog = foodLogRepository.findByCanteenIdAndDateAndMealType(canteenId, today, request.getMealType())
                .orElse(new FoodLog());

        foodLog.setCanteenId(canteenId);
        foodLog.setDate(today);
        foodLog.setMealType(request.getMealType());
        if (request.getPlatesProduced() > 0) {
            foodLog.setPlatesProduced(foodLog.getPlatesProduced() + request.getPlatesProduced());
        }
        if (request.getPlatesSold() > 0) {
            foodLog.setPlatesSold(foodLog.getPlatesSold() + request.getPlatesSold());
        }

        FoodLog saved = foodLogRepository.save(foodLog);

        // After saving the meal-wise log, recompute and upsert the daily summary
        upsertDailySummary(canteenId, today);
        return saved;
    }

    public List<FoodLog> getFoodLogsByCanteenAndDate(String canteenId, LocalDate date) {
        return foodLogRepository.findByCanteenIdAndDate(canteenId, date);
    }

    public FoodLogStats getDailyStats(String canteenId) {
        logger.info("Calculating daily stats for canteenId: {}", canteenId);
        LocalDate today = LocalDate.now();
        
        List<FoodLog> logs = foodLogRepository.findByCanteenIdAndDate(canteenId, today);
        logger.info("Found {} food logs for today.", logs.size());

        int totalProduced = logs.stream()
                .mapToInt(FoodLog::getPlatesProduced)
                .sum();
        logger.info("Total plates produced: {}", totalProduced);

        int totalSold = logs.stream()
                .mapToInt(FoodLog::getPlatesSold)
                .sum();
        logger.info("Total plates sold: {}", totalSold);

        Canteen canteen = canteenRepository.findById(canteenId).orElse(null);
        double pricePerPlate = (canteen != null) ? canteen.getDefaultPlatePrice() : 0.0;

        double totalSalesValue = totalSold * pricePerPlate;
        logger.info("Total sales value: {}", totalSalesValue);

        Map<com.jsp.canteen_management_system.enums.MealType, Integer> platesProducedByMeal = logs.stream()
                .collect(Collectors.groupingBy(FoodLog::getMealType, Collectors.summingInt(FoodLog::getPlatesProduced)));
        
        Map<com.jsp.canteen_management_system.enums.MealType, Integer> platesSoldByMeal = logs.stream()
                .collect(Collectors.groupingBy(FoodLog::getMealType, Collectors.summingInt(FoodLog::getPlatesSold)));

        // Also persist/update the daily summary for today to ensure it's stored
        upsertDailySummary(canteenId, today);
        return new FoodLogStats(totalProduced, totalSold, totalSalesValue, platesProducedByMeal, platesSoldByMeal);
    }

    private void upsertDailySummary(String canteenId, LocalDate date) {
        // Recompute totals for the given date
        List<FoodLog> logs = foodLogRepository.findByCanteenIdAndDate(canteenId, date);
        int totalProduced = logs.stream().mapToInt(FoodLog::getPlatesProduced).sum();
        int totalSold = logs.stream().mapToInt(FoodLog::getPlatesSold).sum();

        double pricePerPlate = canteenRepository.findById(canteenId)
                .map(Canteen::getDefaultPlatePrice)
                .orElse(0.0);
        double totalRevenue = totalSold * pricePerPlate;

        DailyFoodSummary summary = dailyFoodSummaryRepository
                .findByCanteenIdAndDate(canteenId, date)
                .orElse(new DailyFoodSummary());
        summary.setCanteenId(canteenId);
        summary.setDate(date);
        summary.setTotalPlatesProduced(totalProduced);
        summary.setTotalPlatesSold(totalSold);
        summary.setTotalRevenue(totalRevenue);
        dailyFoodSummaryRepository.save(summary);
    }

        public java.util.Optional<DailyFoodSummary> getDailySummary(String canteenId, LocalDate date) {
                return dailyFoodSummaryRepository.findByCanteenIdAndDate(canteenId, date);
        }
}

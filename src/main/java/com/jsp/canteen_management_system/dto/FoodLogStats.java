package com.jsp.canteen_management_system.dto;

import com.jsp.canteen_management_system.enums.MealType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodLogStats {
    private int totalPlatesProduced;
    private int totalPlatesSold;
    private double totalSales;
    private Map<MealType, Integer> platesProducedByMeal;
    private Map<MealType, Integer> platesSoldByMeal;
}

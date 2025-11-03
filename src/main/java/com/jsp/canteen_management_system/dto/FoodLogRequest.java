package com.jsp.canteen_management_system.dto;

import com.jsp.canteen_management_system.enums.MealType;
import lombok.Data;

@Data
public class FoodLogRequest {
    private MealType mealType;
    private int platesProduced;
    private int platesSold;
}

package com.jsp.canteen_management_system.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.jsp.canteen_management_system.enums.MealType;

import java.time.LocalDate;

@Document(collection = "food_logs")
@Data
public class FoodLog {
    @Id
    private String id;
    private String canteenId;
    private LocalDate date;
    private MealType mealType;
    private int platesProduced = 0;
    private int platesSold = 0;
}

package com.jsp.canteen_management_system.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

/**
 * Persisted daily totals per canteen.
 * Collection name intentionally matches user's existing collection: "food logs".
 */
@Document(collection = "food logs")
@CompoundIndex(name = "idx_canteen_date", def = "{ 'canteenId': 1, 'date': 1 }", unique = true)
@Data
public class DailyFoodSummary {
    @Id
    private String id;
    private String canteenId;
    private LocalDate date;
    private int totalPlatesProduced;
    private int totalPlatesSold;
    private double totalRevenue;
}

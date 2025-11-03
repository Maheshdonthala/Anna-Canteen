package com.jsp.canteen_management_system.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "salaries")
@Data
public class Salary {
    @Id
    private String id;
    private String canteenId;
    private String workerId;
    private String workerName; // For display purposes
    private int month;
    private int year;
    private String status; // e.g., "PAID", "PENDING"
}

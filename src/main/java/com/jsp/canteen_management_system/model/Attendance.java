package com.jsp.canteen_management_system.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;


@Document(collection = "attendance")
@Data
public class Attendance {
    @Id
    private String id;
    private String canteenId;
    private String workerId;
    private String workerName;
    private LocalDate date;
    private String status; // e.g., "PRESENT", "ABSENT"
}

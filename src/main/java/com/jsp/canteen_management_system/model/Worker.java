package com.jsp.canteen_management_system.model;

import com.jsp.canteen_management_system.enums.WorkerRole;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "workers")
@Data
public class Worker {
    @Id
    private String id;
    private String canteenId;
    private String name;
    private WorkerRole role; 
}

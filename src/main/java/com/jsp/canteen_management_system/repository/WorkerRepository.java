package com.jsp.canteen_management_system.repository;

import com.jsp.canteen_management_system.model.Worker;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkerRepository extends MongoRepository<Worker, String> {
    List<Worker> findByCanteenId(String canteenId);
    List<Worker> findByCanteenIdIsNull();
}

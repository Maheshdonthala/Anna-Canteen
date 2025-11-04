package com.jsp.canteen_management_system.service;

import com.jsp.canteen_management_system.model.Worker;

import java.util.List;
import java.util.Optional;

public interface WorkerService {
    Worker saveWorker(String canteenId, Worker worker);
    List<Worker> findAllWorkersByCanteenId(String canteenId);
    Optional<Worker> findWorkerById(String canteenId, String workerId);
    Worker updateWorker(String canteenId, String workerId, Worker workerDetails);
    void deleteWorker(String canteenId, String workerId);

    /**
     * Assign any legacy workers (with null canteenId) to the specified canteen.
     * Returns the number of workers updated.
     */
    int assignLegacyWorkersToCanteen(String canteenId);
}

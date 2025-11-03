package com.jsp.canteen_management_system.service;

import com.jsp.canteen_management_system.model.Worker;
import com.jsp.canteen_management_system.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkerServiceImpl implements WorkerService {

    private final WorkerRepository workerRepository;

    @Override
    public Worker saveWorker(String canteenId, Worker worker) {
        worker.setCanteenId(canteenId);
        return workerRepository.save(worker);
    }

    @Override
    public List<Worker> findAllWorkersByCanteenId(String canteenId) {
        return workerRepository.findByCanteenId(canteenId);
    }

    @Override
    public Optional<Worker> findWorkerById(String canteenId, String workerId) {
        return workerRepository.findById(workerId)
                .filter(worker -> worker.getCanteenId().equals(canteenId));
    }

    @Override
    public Worker updateWorker(String canteenId, String workerId, Worker workerDetails) {
        return findWorkerById(canteenId, workerId)
                .map(worker -> {
                    worker.setName(workerDetails.getName());
                    worker.setRole(workerDetails.getRole());
                    return workerRepository.save(worker);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));
    }

    @Override
    public void deleteWorker(String canteenId, String workerId) {
        if (findWorkerById(canteenId, workerId).isPresent()) {
            workerRepository.deleteById(workerId);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found");
        }
    }
}

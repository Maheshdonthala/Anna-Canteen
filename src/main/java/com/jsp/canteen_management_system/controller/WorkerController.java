package com.jsp.canteen_management_system.controller;

import com.jsp.canteen_management_system.model.Worker;
import com.jsp.canteen_management_system.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/canteen/{canteenId}/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @GetMapping
    public List<Worker> list(@PathVariable String canteenId) {
        return workerService.findAllWorkersByCanteenId(canteenId);
    }

    @GetMapping("/{id}")
    public Worker get(@PathVariable String canteenId, @PathVariable String id) {
        return workerService.findWorkerById(canteenId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found in this canteen"));
    }

    @PostMapping
    public Worker create(@PathVariable String canteenId, @RequestBody Worker worker) {
        return workerService.saveWorker(canteenId, worker);
    }

    @PutMapping("/{id}")
    public Worker update(@PathVariable String canteenId, @PathVariable String id, @RequestBody Worker payload) {
        return workerService.updateWorker(canteenId, id, payload);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String canteenId, @PathVariable String id) {
        workerService.deleteWorker(canteenId, id);
    }

    // Admin utility: assign any legacy workers (with null canteenId) to this canteen
    @PostMapping("/assign-legacy")
    public int assignLegacy(@PathVariable String canteenId) {
        return workerService.assignLegacyWorkersToCanteen(canteenId);
    }
}

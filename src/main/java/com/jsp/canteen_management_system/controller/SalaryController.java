package com.jsp.canteen_management_system.controller;

import com.jsp.canteen_management_system.dto.SalaryUpdateRequest;
import com.jsp.canteen_management_system.model.Salary;
import com.jsp.canteen_management_system.repository.SalaryRepository;
import com.jsp.canteen_management_system.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/canteen/{canteenId}/salaries")
public class SalaryController {

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @GetMapping("/status/{status}")
    public List<Salary> listByStatus(@PathVariable String canteenId, @PathVariable String status) {
        return salaryRepository.findByCanteenIdAndStatusIgnoreCase(canteenId, status);
    }

    @PostMapping("/mark")
    public Salary markSalary(@PathVariable String canteenId, @RequestBody SalaryUpdateRequest request) {
        // Find an existing salary for this worker for the current month/year, or create a new one.
        LocalDate now = LocalDate.now();
        Salary salary = salaryRepository.findByCanteenIdAndWorkerIdAndMonthAndYear(canteenId, request.getWorkerId(), now.getMonthValue(), now.getYear())
                .orElse(new Salary());

        if (salary.getId() == null) {
            workerRepository.findById(request.getWorkerId())
                    .filter(worker -> worker.getCanteenId().equals(canteenId))
                    .ifPresentOrElse(worker -> {
                        salary.setCanteenId(canteenId);
                        salary.setWorkerId(worker.getId());
                        salary.setWorkerName(worker.getName());
                        salary.setMonth(now.getMonthValue());
                        salary.setYear(now.getYear());
                    }, () -> {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Worker not found in this canteen");
                    });
        }

        salary.setStatus(request.getStatus());
        return salaryRepository.save(salary);
    }
}

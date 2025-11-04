package com.jsp.canteen_management_system.controller;

import com.jsp.canteen_management_system.model.Canteen;
import com.jsp.canteen_management_system.service.CanteenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/canteens")
public class CanteenController {

    @Autowired
    private CanteenService canteenService;

    // GET request to /api/canteens
    @GetMapping
    public List<Canteen> getAllCanteens() {
        return canteenService.getAllCanteens();
    }

    // POST request to /api/canteens
    @PostMapping
    public Canteen createCanteen(@RequestBody Canteen canteen) {
        return canteenService.addCanteen(canteen);
    }

    // GET single canteen by id
    @GetMapping("/{id}")
    public Canteen getCanteenById(@PathVariable String id) {
        return canteenService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Canteen not found"));
    }

    // PUT canteen by id (update)
    @PutMapping("/{id}")
    public Canteen updateCanteen(@PathVariable String id, @RequestBody Canteen payload) {
        return canteenService.updateCanteen(id, payload);
    }

    // DELETE canteen by id
    @DeleteMapping("/{id}")
    public void deleteCanteen(@PathVariable String id) {
        canteenService.deleteCanteen(id);
    }
}

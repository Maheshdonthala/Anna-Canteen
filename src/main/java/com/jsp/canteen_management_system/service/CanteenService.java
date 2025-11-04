package com.jsp.canteen_management_system.service;

import com.jsp.canteen_management_system.model.Canteen;
import com.jsp.canteen_management_system.repository.CanteenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CanteenService {

    @Autowired
    private CanteenRepository canteenRepository;

    // Method to get all canteens
    public List<Canteen> getAllCanteens() {
        return canteenRepository.findAll();
    }

    // Method to add a new canteen
    public Canteen addCanteen(Canteen canteen) {
        return canteenRepository.save(canteen);
    }

    // Method to get a canteen by id
    public Optional<Canteen> getById(String id) {
        return canteenRepository.findById(id);
    }

    // Method to update a canteen by id
    public Canteen updateCanteen(String id, Canteen payload) {
        return canteenRepository.findById(id)
                .map(existing -> {
                    if (payload.getName() != null) existing.setName(payload.getName());
                    if (payload.getLocation() != null) existing.setLocation(payload.getLocation());
                    // defaultPlatePrice may be 0.0 intentionally, so we update when payload has a value
                    existing.setDefaultPlatePrice(payload.getDefaultPlatePrice());
                    return canteenRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Canteen not found"));
    }

    // Method to delete a canteen by id
    public void deleteCanteen(String id) {
        canteenRepository.deleteById(id);
    }
}

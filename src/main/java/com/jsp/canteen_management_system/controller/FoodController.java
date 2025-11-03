package com.jsp.canteen_management_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FoodController {

    @GetMapping("/canteen/{canteenId}/foodlog")
    public String foodLogPage(@PathVariable String canteenId) {
        // Redirect old /foodlog path to the canonical /food path
        return "redirect:/canteen/" + canteenId + "/food";
    }
}


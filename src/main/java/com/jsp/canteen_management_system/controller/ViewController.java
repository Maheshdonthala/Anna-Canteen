package com.jsp.canteen_management_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/", "/index"})
    public String index() {
        // If user is authenticated, show the canteens list; otherwise show landing
        // The view can check authentication itself, but we prefer redirecting here.
        return "index"; // resolved by Thymeleaf
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // login.html template
    }

    @GetMapping("/canteens")
    public String canteens() {
        return "canteens";
    }

    @GetMapping("/canteen/{id}")
    public String canteen() {
        return "canteen";
    }

    @GetMapping("/canteen/{id}/food")
    public String food() {
        return "foodlog";
    }

    @GetMapping("/canteen/{id}/attendance")
    public String attendance() {
        return "attendance";
    }

    @GetMapping("/canteen/{id}/salary")
    public String salary() {
        return "salary";
    }
}

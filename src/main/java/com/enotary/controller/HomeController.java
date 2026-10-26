package com.enotary.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appName", "Containerized E-Notary Appointment System");
        model.addAttribute("developerName", "Aryan Racha");
        model.addAttribute("rollNo", "23102C0055");
        return "index";
    }
}

package com.enotary.controller;

import com.enotary.model.Notary;
import com.enotary.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for retrieving certified notaries public.
 */
@RestController
@RequestMapping("/api/notaries")
public class NotaryRestController {

    private final UserService userService;

    public NotaryRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Notary>> getAllNotaries() {
        return ResponseEntity.ok(userService.getAllNotaries());
    }
}

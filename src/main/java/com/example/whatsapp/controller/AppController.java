package com.example.whatsapp.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {

    @GetMapping
    public String homePage(){
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return "username " + username;
    }
}

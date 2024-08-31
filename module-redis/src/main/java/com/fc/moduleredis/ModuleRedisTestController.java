package com.fc.moduleredis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModuleRedisTestController {

    @GetMapping("/")
    public String main(String[] args) {
        System.out.println("Hello World!");
        return "Hello World!";
    }

}

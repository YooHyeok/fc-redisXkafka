package com.fc.moduleredis;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModuleRedisTestController {

    @GetMapping("/redis")
    public String main(String[] args) {
        System.out.println("Hello Redis!");
        return "Hello Redis!";
    }

}

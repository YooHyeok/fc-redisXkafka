package com.fc.modulekafka;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModuleKafkaTestController {

    @GetMapping("/kafka")
    public String main(String[] args) {
        System.out.println("Hello Kafka!");
        return "Hello Kafka!";
    }

}

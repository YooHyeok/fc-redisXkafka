package com.fc.moduleapplication.controller;

import com.fc.moduleapplication.service.LowestPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 가장 낮은 price(가격) 검색 api 컨트롤러
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class LowestPriceController {

    private final LowestPriceService lowestPriceService;
    /**
     * 데이터 조회
     * 요청 URL: http://localhost:8080/getZSETValue?key=FPG0001
     */
    @GetMapping("/getZSETValue")
    public Set getZsetValue(String key) {
        return lowestPriceService.getZsetValue(key);
    }
}

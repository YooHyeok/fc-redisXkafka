package com.fc.moduleapplication.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class LowestPriceServiceImpl implements LowestPriceService {

    @Override
    public Set getZsetValue(String key) {
        return Collections.emptySet();
    }
}

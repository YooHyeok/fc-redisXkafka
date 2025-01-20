package com.fc.moduleapplication.service;

import com.fc.moduleapplication.vo.Product;

import java.util.Set;

public interface LowestPriceService {
    Set getZsetValue(String key);

    int setNewProduct(Product product);
}

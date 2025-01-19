package com.fc.moduleapplication.vo;

import lombok.Data;
import java.util.List;

/**
 * 프로덕트 그룹 아이디 기준 하나의 프로덕트 그룹에는 여러개의 프로덕트가 있다.
 */
@Data
public class ProductGroup {
    private String prodGrpId; // ex) FPG0001
    private List<Product> productList; // [{"21fa823e-edaa-4d81-8203-ab70b29812cc": 25000}, {}, ...]
}

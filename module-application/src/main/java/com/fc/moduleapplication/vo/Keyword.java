package com.fc.moduleapplication.vo;

import lombok.Data;

import java.util.List;

@Data
public class Keyword {

    private String keyword; // ex) 유아용품 - 하기스 기저귀(FPG0001), A사 딸랑이(FPG0002)
    private List<ProductGroup> productGroupList; // ex) {"FPG0001": [{"21fa823e-edaa-4d81-8203-ab70b29812cc": 25000}, {}, ...] "FPG0002": [{,,,}], ...}

}

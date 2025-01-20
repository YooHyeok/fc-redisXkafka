package com.fc.moduleapplication.service;

import com.fc.moduleapplication.vo.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LowestPriceServiceImpl implements LowestPriceService {

    private final RedisTemplate redisTemplate;

    /**
     * <h1>Redis(ZSET-ZRANGE) 상품그룹,범위 기준 상품 정보 목록 조회</h1>
     * <pre>
     *      Redis ZSET(sorted set) ZRANGE 조회
     *      productGroupId(key) 기준 0~9까지 범위에 해당하는 10개의 Product(member)를 Price(score)와 함께 조회한다.
     *      Redis 조회 메소드: rangeWithScores(상품그룹Id(key), 범위시작인덱스, 범위끝인덱스)
     *      Redis 조회 명령: zrange {상품그룹Id(key)} {범위시작인덱스} {범위끝인덱스} withscores
     * </pre>
     * @param key
     * @return 상품 정보 목록
     */
    @Override
    public Set getZsetValue(String key) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.rangeWithScores(key, 0, 9); // 범위 기준 Scores(price) 조회: ZRANGE key 0 9 withscores (key, 시작범위, 끝범위)
    }

    /**
     * <h1>Redis(ZSET-ZADD,ZRANK) 추가 및 추가된 값 순위 조회</h1>
     * <pre>
     *      Redis ZSET(sorted set) ZADD 추가 및 ZRANK 조회
     *        1. productGroupId(key), productId(member), price(score) 데이터를 추가한다.
     *        2. productGroupId(key) 에서 productId(member)에 대한 순위를 조회한다.
     *      Redis ZSET 추가
     *        - Redis 추가 메소드: add(상품그룹Id(key), 상품Id(member), 가격(score))
     *        - Redis 추가 명령: zadd {상품그룹Id(key)} {가격(score)} {상품Id(member)}
     *      Redis ZSET 순위 조회
     *        - Redis 순위 조회 메소드: rank(상품그룹Id(key), 상품Id(member))
     *        - Redis 순위 조회 명령: zrank {상품그룹Id(key)} {가격(score)}}
     * </pre>
     * @param product
     * @return 순위값
     */
    @Override
    public int setNewProduct(Product product) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add(product.getProductGroupId(), product.getProductId(), product.getPrice()); // key, value, score 순서로 product 정보를 추가한다.
        return zSetOperations.rank(product.getProductGroupId(), product.getProductId()).intValue(); // 추가한 상품 랭킹 조회
    }
}

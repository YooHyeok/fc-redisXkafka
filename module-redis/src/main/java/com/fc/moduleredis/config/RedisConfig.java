package com.fc.moduleredis.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 레디스 관련 설정 클래스 파일.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {
    private final RedisProperties redisProperties;

    /**
     * Redis 빈 등록
     * Lettuce와 Zedis중 성능이 월등히 우수한 Lettuce 사용
     * @return
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("[레디스 빈 등록] redis-host: {}", redisProperties.getHost());
        log.info("[레디스 빈 등록] redis-port: {}", redisProperties.getPort());
        return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
    }

    /**
     * 레디스 템플릿 빈 등록
     * <pre>
     * 템플릿을 활용하여 접근하면 매번 세팅을 하는것이 아닌 이미 세팅되어있는 정보와 옵션으로 레디스에 접근하게 된다.
     * 1. 커넥션 설정: LettuceConnectionFactory를 사용한 빈 사용
     * 2. 직렬화 설정 : Redis에 저장되는 데이터의 직렬화 방식을 정의하기 위함
     *   - Redis는 본질적으로 이진(Binary)데이터를 저장하는 시스템으로, 저장 및 조회 형태는 바이트 배열
     *   - 직렬화: Java 객체를 바이트 스트림으로 변환하여 Redis에 저장할 수 있는 형태
     *   - 역직렬화: Redis에 저장된 바이트 스트림을 다시 Java 객체로 변환
     * </pre>
     * @return RedisTemplate
     * @Generic Redis - Key(String), Value(Object)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        /* 커넥션 설정 */
        redisTemplate.setConnectionFactory(redisConnectionFactory()); // LettuceConnectionFactory를 사용한 빈 사용
        /* 직렬화 설정 */
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

}

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

}

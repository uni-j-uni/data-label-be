/* 
 * Copyright (c) hjyoon@signtelecom.com 
 */
package com.signtelecom.datalabelbe.global.config;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.RequiredArgsConstructor;

/**
 * Redis 설정
 *
 * <p>연결 정보와 RedisTemplate 직렬화 방식 정의
 */
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

  /** application.yaml 의 spring.redis.* 설정을 자동으로 바인딩한 객체 */
  private final RedisProperties redisProperties;

  /**
   * Redis 연결 팩토리 설정
   *
   * <p>Standalone, Lettuce 클라이언트 사용. 비밀번호는 있을 때만 설정
   */
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {

    // Redis 서버 기본 설정 (host, port)
    RedisStandaloneConfiguration redisConfig =
        new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());

    // Redis 비밀번호가 설정된 경우에만 적용
    if (redisProperties.getPassword() != null && !redisProperties.getPassword().isBlank()) {
      redisConfig.setPassword(redisProperties.getPassword());
    }

    // Lettuce 기반 커넥션 팩토리 반환
    return new LettuceConnectionFactory(redisConfig);
  }

  /**
   * RedisTemplate 설정
   *
   * <p>Key는 String, Value는 JSON(GenericJackson2JsonRedisSerializer)으로 직렬화
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {

    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

    // Spring이 관리하는 RedisConnectionFactory 주입
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    // 일반 Key
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    // Hash Key
    redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    // Value를 JSON 형태로 저장 (객체 직렬화)
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    // Hash Value도 JSON 직렬화
    redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    // 설정 완료 후 초기화
    redisTemplate.afterPropertiesSet();

    return redisTemplate;
  }
}

package com.ecommerce.product.config;
import com.ecommerce.product.model.Product;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public com.github.benmanes.caffeine.cache.Cache<String, Product> caffeineCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

@Bean
public RedisTemplate<String, Product> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Product> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    Jackson2JsonRedisSerializer<Product> serializer =
      new Jackson2JsonRedisSerializer<>( Product.class);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule()); // for LocalDateTime
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    serializer.setObjectMapper(mapper);

    template.setValueSerializer(serializer);
    template.setKeySerializer(new StringRedisSerializer());

    template.afterPropertiesSet();
    return template;
}
}
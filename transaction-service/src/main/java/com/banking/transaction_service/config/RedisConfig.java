package com.banking.transaction_service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplateConfig(ReactiveRedisConnectionFactory connectionFactory) {
        PolymorphicTypeValidator validator = BasicPolymorphicTypeValidator
                .builder()
                .allowIfBaseType(Object.class)
                .build();

        ObjectMapper mapper = JsonMapper.builder()
                .activateDefaultTyping(validator, DefaultTyping.NON_FINAL)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();

        JacksonJsonRedisSerializer<Object> serializer = new JacksonJsonRedisSerializer<>(mapper, Object.class);

        RedisSerializationContext<String, Object> context = RedisSerializationContext
                .<String, Object>newSerializationContext(new StringRedisSerializer())
                .hashValue(serializer)
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}

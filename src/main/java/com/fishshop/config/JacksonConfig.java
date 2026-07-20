package com.fishshop.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson JSON 配置。
 *
 * <p>SSM 项目中由 SpringMVC 使用 ObjectMapper 输出 JSON，这里补充 Java 8 时间类型支持。</p>
 */
@Configuration
public class JacksonConfig {

    /**
     * 注册 JavaTimeModule 后，LocalDateTime 会按可读字符串输出，而不是时间戳数组。
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}

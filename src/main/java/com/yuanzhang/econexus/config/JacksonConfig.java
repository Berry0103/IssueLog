package com.yuanzhang.econexus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // 1. 导入 JavaTimeModule
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class JacksonConfig {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 2. 注册 Hibernate 6 模块，处理延迟加载代理
        objectMapper.registerModule(new Hibernate6Module());

        // 3. 注册 JSR310 模块，处理 Java 8+ 日期时间类型
        objectMapper.registerModule(new JavaTimeModule());

        // (可选) 如果你想自定义日期时间的序列化格式，例如：
        // objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}
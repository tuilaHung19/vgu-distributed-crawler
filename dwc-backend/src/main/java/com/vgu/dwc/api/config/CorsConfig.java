package com.vgu.dwc.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Báo cho Spring Boot biết đây là file cấu hình hệ thống
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Áp dụng cho mọi đường dẫn API
        		.allowedOriginPatterns("*") // SỬA Ở ĐÂY: Cho phép tất cả các cổng (5500, 3000, 80...) được phép gọi API
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Cho phép các hành động này, thêm "OPTIONS" vì trình duyệt hay gửi request thăm dò trước khi gọi API thật
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

/*
 * bắt buộc phải dùng chữ .allowedOriginPatterns("*") thay vì .allowedOrigins("*")
 */

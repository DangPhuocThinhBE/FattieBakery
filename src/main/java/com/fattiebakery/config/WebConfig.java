package com.fattiebakery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Cấu hình để xem ảnh đại diện đã upload
        // Đường dẫn ảo: /uploads/** sẽ trỏ vào thư mục vật lý: uploads/
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");

        // 2. Cấu hình mặc định cho các file tĩnh trong src/main/resources/static
        // Giúp các file CSS, JS, Images hệ thống không bị lỗi 404
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
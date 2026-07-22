package com.fattiebakery.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "gtf3svkg",
                "api_key", "496388342976137",
                "api_secret", "Nv_oRokaCkXYUYtEUIIyBR0Ib-M"
        ));
    }
}
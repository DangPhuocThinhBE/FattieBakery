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
                "cloud_name", "dn1mn23bz",
                "api_key", "973273354495734",
                "api_secret", "Yt_0lpA2mHEtOjLNTwPN8RHbgpM"
        ));
    }
}
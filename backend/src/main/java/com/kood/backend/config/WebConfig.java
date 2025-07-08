package com.kood.backend.config;

import java.io.File;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${media.storage.location}")
    private String mediaStorageLocation;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Map /media/** URL requests to the file system path '../../media/'
        // 'file:' prefix is crucial for accessing local file system paths
        // .toAbsolutePath() is good practice to ensure consistency
        registry.addResourceHandler("/media/public/**")
                .addResourceLocations(
                        "file:" + Paths.get(mediaStorageLocation).normalize().toAbsolutePath().toString()
                                + File.separator);
    }
}

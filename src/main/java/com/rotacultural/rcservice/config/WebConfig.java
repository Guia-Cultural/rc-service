package com.rotacultural.rcservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Mídias em /uploads/** são servidas com controle de acesso por MediaController
}

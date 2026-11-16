package com.rotacultural.rcservice.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile("prod")
public class ProductionSafetyConfig {
    @Bean
    static BeanFactoryPostProcessor productionSafety(Environment environment) {
        return beanFactory -> validate(environment);
    }

    static void validate(Environment env) {
        if (!"azure".equals(env.getProperty("app.storage.provider"))
                || !"validate".equals(env.getProperty("spring.jpa.hibernate.ddl-auto"))
                || env.getProperty("app.seed.demo-users", Boolean.class, false)
                || env.getProperty("app.seed.demo-places", Boolean.class, false)) {
            throw new IllegalStateException("Produção exige Azure Blob, schema validate e contas/dados demo desativados");
        }
        String url = env.getProperty("spring.datasource.url", "");
        if (!url.matches(".*[?&]sslMode=VERIFY_IDENTITY(&.*)?")) {
            throw new IllegalStateException("Produção exige DB_URL com sslMode=VERIFY_IDENTITY");
        }
    }
}

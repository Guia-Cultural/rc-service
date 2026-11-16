package com.rotacultural.rcservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionSafetyTests {
    private MockEnvironment validEnvironment() {
        return new MockEnvironment().withProperty("app.storage.provider", "azure")
                .withProperty("spring.jpa.hibernate.ddl-auto", "validate")
                .withProperty("spring.datasource.url", "jdbc:mysql://example/db?sslMode=VERIFY_IDENTITY");
    }

    @Test
    void productionMustRejectUnsafeOverrides() {
        assertThatCode(() -> ProductionSafetyConfig.validate(validEnvironment())).doesNotThrowAnyException();
        for (String key : new String[]{"app.seed.demo-users", "app.seed.demo-places"}) {
            assertThatThrownBy(() -> ProductionSafetyConfig.validate(validEnvironment().withProperty(key, "true")))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThatThrownBy(() -> ProductionSafetyConfig.validate(validEnvironment().withProperty("app.storage.provider", "local")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ProductionSafetyConfig.validate(validEnvironment().withProperty("spring.jpa.hibernate.ddl-auto", "update")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ProductionSafetyConfig.validate(validEnvironment().withProperty("spring.datasource.url", "jdbc:mysql://example/db?useSSL=true")))
                .isInstanceOf(IllegalStateException.class);
    }
}

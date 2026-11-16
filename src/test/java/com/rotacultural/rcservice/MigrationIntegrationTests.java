package com.rotacultural.rcservice;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("migration")
class MigrationIntegrationTests {
    @Autowired private Flyway flyway;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void mysqlMigrationsShouldValidateHibernateAndPreserveDataOnRestart() {
        // O contexto só inicia se o schema SQL for compatível com todas as entidades.
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");
        String marker = "Auditoria de migração";
        jdbc.update("INSERT INTO categories (name) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = ?)",
                marker, marker);
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        flyway.validate();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM categories WHERE name = ?", Long.class, marker))
                .isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users", Long.class)).isZero();
    }
}

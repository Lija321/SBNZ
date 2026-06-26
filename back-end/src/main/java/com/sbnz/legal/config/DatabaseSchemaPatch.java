package com.sbnz.legal.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaPatch implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaPatch(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE audit_record DROP CONSTRAINT IF EXISTS audit_record_record_type_check");
        jdbcTemplate.execute("""
                ALTER TABLE audit_record ADD CONSTRAINT audit_record_record_type_check
                CHECK (record_type IN (
                    'RULE_FIRED',
                    'STATUS_CHANGED',
                    'TASK_CREATED',
                    'CASE_UPDATED',
                    'CASE_DELETED'
                ))
                """);
    }
}

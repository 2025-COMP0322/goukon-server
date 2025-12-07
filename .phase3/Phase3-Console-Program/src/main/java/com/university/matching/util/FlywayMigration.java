package com.university.matching.util;

import com.university.matching.config.DatabaseConfig;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flyway를 사용한 데이터베이스 마이그레이션 유틸리티
 */
public class FlywayMigration {
    private static final Logger logger = LoggerFactory.getLogger(FlywayMigration.class);

    /**
     * 데이터베이스 마이그레이션을 실행합니다
     */
    public static void migrate() {
        DatabaseConfig config = DatabaseConfig.getInstance();

        logger.info("Starting database migration...");

        Flyway flyway = Flyway.configure()
                .dataSource(config.getUrl(), config.getUsername(), config.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .cleanDisabled(false)
                .load();

        try {
            int migrationsApplied = flyway.migrate().migrationsExecuted;
            logger.info("Database migration completed. {} migrations applied", migrationsApplied);
        } catch (Exception e) {
            logger.error("Database migration failed", e);
            throw new RuntimeException("Failed to migrate database", e);
        }
    }

    /**
     * 데이터베이스 마이그레이션 정보를 출력합니다
     */
    public static void info() {
        DatabaseConfig config = DatabaseConfig.getInstance();

        Flyway flyway = Flyway.configure()
                .dataSource(config.getUrl(), config.getUsername(), config.getPassword())
                .locations("filesystem:src/main/resources/db/migration")
                .load();

        flyway.info().all();
    }
}

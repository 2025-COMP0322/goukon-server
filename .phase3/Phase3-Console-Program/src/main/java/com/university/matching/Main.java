package com.university.matching;

import com.university.matching.config.DatabaseConfig;
import com.university.matching.ui.ConsoleUI;
import com.university.matching.util.FlywayMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 과팅 매칭 시스템 메인 애플리케이션
 * Phase 3 Console Program
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("University Matching System - Phase 3");
        logger.info("========================================");

        try {
            // 1. 데이터베이스 연결 테스트
            logger.info("\n[1] Database Connection Test");
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            boolean connectionSuccess = dbConfig.testConnection();

            if (!connectionSuccess) {
                logger.error("Database connection failed. Exiting...");
                System.err.println("ERROR: Failed to connect to database. Please check your configuration.");
                System.exit(1);
            }

            // 2. Flyway 마이그레이션 실행
            logger.info("\n[2] Running Database Migration");
            FlywayMigration.migrate();

            logger.info("\n========================================");
            logger.info("System initialized successfully!");
            logger.info("========================================\n");

            // 3. 콘솔 UI 시작
            logger.info("[3] Starting Console UI");
            ConsoleUI consoleUI = new ConsoleUI();
            consoleUI.start();

            logger.info("\n========================================");
            logger.info("Application terminated normally");
            logger.info("========================================");

        } catch (Exception e) {
            logger.error("Application startup failed", e);
            System.err.println("\nERROR: Application failed to start");
            System.err.println("Reason: " + e.getMessage());
            System.err.println("\nPlease check the logs for more details.");
            e.printStackTrace();
            System.exit(1);
        }
    }
}

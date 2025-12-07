package com.university.matching.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/**
 * 데이터베이스 연결을 관리하는 설정 클래스
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static DatabaseConfig instance;

    private String url;
    private String username;
    private String password;
    private String driver;

    private DatabaseConfig() {
        loadConfiguration();
        initializeDriver();
    }

    /**
     * Singleton 인스턴스를 반환합니다
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /**
     * application.yml 파일을 로드합니다
     */
    private void loadConfiguration() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.yml")) {
            if (input == null) {
                logger.error("Unable to find application.yml");
                throw new RuntimeException("application.yml not found");
            }

            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(input);

            @SuppressWarnings("unchecked")
            Map<String, Object> dbConfig = (Map<String, Object>) config.get("db");

            url = (String) dbConfig.get("url");
            username = (String) dbConfig.get("username");
            password = (String) dbConfig.get("password");
            driver = (String) dbConfig.get("driver");

            logger.info("Database configuration loaded successfully");
            logger.info("DB URL: {}", url);
            logger.info("DB User: {}", username);
        } catch (IOException e) {
            logger.error("Error loading configuration", e);
            throw new RuntimeException("Failed to load database configuration", e);
        } catch (Exception e) {
            logger.error("Error parsing YAML configuration", e);
            throw new RuntimeException("Failed to parse database configuration", e);
        }
    }

    /**
     * JDBC 드라이버를 초기화합니다
     */
    private void initializeDriver() {
        try {
            Class.forName(driver);
            logger.info("JDBC Driver loaded: {}", driver);
        } catch (ClassNotFoundException e) {
            logger.error("JDBC Driver not found: {}", driver, e);
            throw new RuntimeException("Failed to load JDBC driver", e);
        }
    }

    /**
     * 데이터베이스 연결을 생성하고 반환합니다
     */
    public Connection getConnection() throws SQLException {
        logger.debug("Creating database connection...");
        Connection conn = DriverManager.getConnection(url, username, password);
        logger.debug("Database connection created successfully");
        return conn;
    }

    /**
     * 데이터베이스 연결을 테스트합니다
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            logger.info("Database connection test successful");
            logger.info("Database: {}", conn.getMetaData().getDatabaseProductName());
            logger.info("Version: {}", conn.getMetaData().getDatabaseProductVersion());
            return true;
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }

    // Getters
    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

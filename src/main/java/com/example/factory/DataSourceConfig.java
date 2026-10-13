package com.example.factory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:}")
    private String configuredUrl;

    @Value("${spring.datasource.username:root}")
    private String configuredUsername;

    @Value("${spring.datasource.password:root}")
    private String configuredPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Resolve URL from environment variables
        String url = System.getenv("DB_URL");
        if (url == null || url.trim().isEmpty()) {
            url = System.getenv("SPRING_DATASOURCE_URL");
        }
        if (url == null || url.trim().isEmpty()) {
            url = System.getenv("DATABASE_URL");
        }
        if (url == null || url.trim().isEmpty()) {
            url = configuredUrl;
        }

        String username = System.getenv("DB_USERNAME");
        if (username == null || username.trim().isEmpty()) {
            username = System.getenv("SPRING_DATASOURCE_USERNAME");
        }
        if (username == null || username.trim().isEmpty()) {
            username = configuredUsername;
        }

        String password = System.getenv("DB_PASSWORD");
        if (password == null || password.trim().isEmpty()) {
            password = System.getenv("SPRING_DATASOURCE_PASSWORD");
        }
        if (password == null || password.trim().isEmpty()) {
            password = configuredPassword;
        }

        // Test if a cloud or local MySQL URL is configured
        boolean isMySQLConfigured = url != null && (url.contains("mysql") || url.contains("3306") || url.contains("aivencloud") || url.contains("railway"));

        // If on cloud (Render) without a valid external MySQL database configured, or default localhost in cloud
        boolean isRunningOnCloudWithoutExternalDb = isCloudEnvironment() && (url == null || url.contains("localhost"));

        if (isRunningOnCloudWithoutExternalDb) {
            log.warn("================================================================================");
            log.warn("No external MySQL database configured in Render environment variables!");
            log.warn("Switching seamlessly to embedded in-memory database to prevent crash.");
            log.warn("To use your cloud MySQL database, set DB_URL, DB_USERNAME, and DB_PASSWORD in Render.");
            log.warn("================================================================================");
            return createH2DataSource();
        }

        // Clean & format MySQL URL if needed (e.g. if plain mysql:// was pasted instead of jdbc:mysql://)
        String jdbcUrl = url;
        if (jdbcUrl != null && !jdbcUrl.startsWith("jdbc:") && jdbcUrl.contains("://")) {
            jdbcUrl = "jdbc:" + jdbcUrl;
        }

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setConnectionTimeout(30000); // 30s timeout for cloud databases
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            log.info("Connecting to MySQL database at: {}", sanitizeUrl(jdbcUrl));
            HikariDataSource ds = new HikariDataSource(config);
            // Verify connection
            ds.getConnection().close();
            log.info("Successfully connected to MySQL database!");
            return ds;
        } catch (Exception e) {
            log.error("Failed to connect to configured MySQL: {}. Falling back to internal database so Render deployment succeeds.", e.getMessage());
            return createH2DataSource();
        }
    }

    private DataSource createH2DataSource() {
        HikariConfig h2Config = new HikariConfig();
        h2Config.setJdbcUrl("jdbc:h2:mem:student_analyzer;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL");
        h2Config.setDriverClassName("org.h2.Driver");
        h2Config.setUsername("sa");
        h2Config.setPassword("");
        h2Config.setMaximumPoolSize(5);
        return new HikariDataSource(h2Config);
    }

    private boolean isCloudEnvironment() {
        return System.getenv("RENDER") != null || System.getenv("RENDER_SERVICE_ID") != null || System.getenv("PORT") != null;
    }

    private String sanitizeUrl(String url) {
        if (url == null) return "null";
        return url.replaceAll(":[^:@]+@", ":****@");
    }
}

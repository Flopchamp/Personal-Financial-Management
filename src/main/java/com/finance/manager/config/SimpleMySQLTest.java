package com.finance.manager.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class SimpleMySQLTest {
    
    private static final String URL = "jdbc:mysql://localhost:3306/?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "4885";
    
    public static void main(String[] args) {
        System.out.println("=== Simple MySQL Connection Test ===");
        System.out.println("Testing connection with credentials:");
        System.out.println("URL: " + URL);
        System.out.println("Username: " + USERNAME);
        System.out.println("Password: " + (PASSWORD.length() > 0 ? "*".repeat(PASSWORD.length()) : "(empty)"));
        System.out.println("=====================================");
        
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL JDBC Driver loaded successfully");
            
            // Test connection
            try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
                System.out.println("✅ MySQL Connection Successful!");
                
                DatabaseMetaData metaData = connection.getMetaData();
                System.out.println("📊 Database Information:");
                System.out.println("   - Product Name: " + metaData.getDatabaseProductName());
                System.out.println("   - Product Version: " + metaData.getDatabaseProductVersion());
                System.out.println("   - Driver Name: " + metaData.getDriverName());
                System.out.println("   - Driver Version: " + metaData.getDriverVersion());
                System.out.println("   - URL: " + metaData.getURL());
                System.out.println("   - Username: " + metaData.getUserName());
                
                // Test simple query
                try (var statement = connection.createStatement();
                     var resultSet = statement.executeQuery("SELECT VERSION() as mysql_version, NOW() as current_time")) {
                    if (resultSet.next()) {
                        System.out.println("   - MySQL Version: " + resultSet.getString("mysql_version"));
                        System.out.println("   - Current Time: " + resultSet.getString("current_time"));
                    }
                }
                
                // Test database creation
                try (var statement = connection.createStatement()) {
                    statement.executeUpdate("CREATE DATABASE IF NOT EXISTS personal_finance_db");
                    System.out.println("✅ Database 'personal_finance_db' created/verified successfully");
                }
                
                System.out.println("=====================================");
                System.out.println("🎉 MySQL is ready for Spring Boot application!");
                
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found!");
            System.err.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ MySQL Connection Failed!");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Message: " + e.getMessage());
            
            // Common error suggestions
            if (e.getMessage().contains("Access denied")) {
                System.err.println("🔍 Suggestion: Check username and password");
            } else if (e.getMessage().contains("Communications link failure")) {
                System.err.println("🔍 Suggestion: Check if MySQL server is running on localhost:3306");
            } else if (e.getMessage().contains("Unknown database")) {
                System.err.println("🔍 Suggestion: Database will be created automatically");
            }
        } catch (Exception e) {
            System.err.println("❌ Unexpected error occurred!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.finance.manager.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLDatabaseSetup {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "personal_finance_db";
    private static final String DB_URL_WITH_DB = DB_URL + DB_NAME;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "4885";
    
    public static void main(String[] args) {
        System.out.println("🔄 Testing MySQL Database Connection and Setup...");
        System.out.println("================================================");
        
        // Test 1: Connect to MySQL server
        testMySQLConnection();
        
        // Test 2: Create database if not exists
        createDatabaseIfNotExists();
        
        // Test 3: Connect to our specific database
        testDatabaseConnection();
        
        // Test 4: Show what tables Spring Boot would create
        showExpectedTables();
        
        System.out.println("\n✅ MySQL Database Setup Test Completed!");
    }
    
    private static void testMySQLConnection() {
        System.out.println("\n🔍 Test 1: Testing MySQL Server Connection...");
        try (Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            System.out.println("✅ Successfully connected to MySQL server!");
            System.out.println("📍 Server URL: " + DB_URL);
            System.out.println("👤 Username: " + USERNAME);
            
            // Show current databases
            System.out.println("\n📋 Current Databases:");
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
                while (rs.next()) {
                    String dbName = rs.getString(1);
                    if (dbName.equals(DB_NAME)) {
                        System.out.println("  📁 " + dbName + " ⭐ (Our target database)");
                    } else {
                        System.out.println("  📁 " + dbName);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to MySQL server!");
            System.err.println("Error: " + e.getMessage());
            return;
        }
    }
    
    private static void createDatabaseIfNotExists() {
        System.out.println("\n🔍 Test 2: Creating Database '" + DB_NAME + "' if not exists...");
        try (Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {
            
            String createDbSQL = "CREATE DATABASE IF NOT EXISTS " + DB_NAME + 
                                " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            statement.executeUpdate(createDbSQL);
            System.out.println("✅ Database '" + DB_NAME + "' created or already exists!");
            
        } catch (SQLException e) {
            System.err.println("❌ Failed to create database!");
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private static void testDatabaseConnection() {
        System.out.println("\n🔍 Test 3: Testing Connection to '" + DB_NAME + "' Database...");
        try (Connection connection = DriverManager.getConnection(DB_URL_WITH_DB, USERNAME, PASSWORD)) {
            System.out.println("✅ Successfully connected to '" + DB_NAME + "' database!");
            System.out.println("📍 Database URL: " + DB_URL_WITH_DB);
            
            // Check if any tables exist
            try (ResultSet rs = connection.getMetaData().getTables(DB_NAME, null, "%", null)) {
                System.out.println("\n📋 Current Tables in '" + DB_NAME + "':");
                boolean hasTables = false;
                while (rs.next()) {
                    System.out.println("  🗂️  " + rs.getString("TABLE_NAME"));
                    hasTables = true;
                }
                if (!hasTables) {
                    System.out.println("  📭 No tables found (database is empty)");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to '" + DB_NAME + "' database!");
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private static void showExpectedTables() {
        System.out.println("\n🔍 Test 4: Expected Tables that Spring Boot will create:");
        System.out.println("📋 When you run the application with MySQL profile, these tables will be created:");
        System.out.println("  🗂️  users - User accounts and authentication");
        System.out.println("  🗂️  categories - Income and expense categories");
        System.out.println("  🗂️  transactions - Financial transactions");
        System.out.println("  🗂️  budgets - Budget management");
        
        System.out.println("\n💡 To run the application with MySQL:");
        System.out.println("   mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=mysql");
        
        System.out.println("\n🔧 MySQL Configuration:");
        System.out.println("   URL: " + DB_URL_WITH_DB);
        System.out.println("   Username: " + USERNAME);
        System.out.println("   Password: [CONFIGURED]");
        System.out.println("   Database: " + DB_NAME);
    }
}

package com.finance.manager.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class MySQLConnectionVerifier {
    
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/personal_finance_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "4885";
        
        System.out.println("🔍 Testing MySQL Database Connection...");
        System.out.println("📍 URL: " + url);
        System.out.println("👤 Username: " + username);
        
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish connection
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                System.out.println("✅ MySQL Connection SUCCESSFUL!");
                
                // Test database existence
                System.out.println("\n📊 Checking database and tables...");
                try (Statement statement = connection.createStatement()) {
                    
                    // Check if database exists
                    ResultSet rs = statement.executeQuery("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'personal_finance_db'");
                    if (rs.next()) {
                        System.out.println("✅ Database 'personal_finance_db' exists");
                    }
                    
                    // List all tables
                    rs = statement.executeQuery("SHOW TABLES");
                    System.out.println("\n📋 Tables found:");
                    boolean hasTable = false;
                    while (rs.next()) {
                        hasTable = true;
                        String tableName = rs.getString(1);
                        System.out.println("   ✓ " + tableName);
                        
                        // Get table structure
                        try (Statement descStatement = connection.createStatement()) {
                            ResultSet descRs = descStatement.executeQuery("DESCRIBE " + tableName);
                            System.out.println("     Columns:");
                            while (descRs.next()) {
                                String field = descRs.getString("Field");
                                String type = descRs.getString("Type");
                                String key = descRs.getString("Key");
                                System.out.println("       - " + field + " (" + type + ")" + 
                                                 (key.length() > 0 ? " [" + key + "]" : ""));
                            }
                            System.out.println();
                        }
                    }
                    
                    if (!hasTable) {
                        System.out.println("⚠️  No tables found. This is normal for a fresh database.");
                    }
                    
                    // Test write operation
                    System.out.println("🧪 Testing write operation...");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS connection_test (id INT AUTO_INCREMENT PRIMARY KEY, test_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                    statement.executeUpdate("INSERT INTO connection_test VALUES (1, NOW())");
                    
                    // Test read operation
                    rs = statement.executeQuery("SELECT COUNT(*) as count FROM connection_test");
                    if (rs.next()) {
                        System.out.println("✅ Write/Read test successful! Records: " + rs.getInt("count"));
                    }
                    
                    // Clean up test table
                    statement.executeUpdate("DROP TABLE IF EXISTS connection_test");
                    
                    System.out.println("\n🎉 MySQL Database Connection Test PASSED!");
                    System.out.println("📈 The Spring Boot application can successfully connect to MySQL");
                    System.out.println("🔒 Authentication with password '4885' works correctly");
                    System.out.println("💾 Database operations (CREATE, INSERT, SELECT, DROP) work properly");
                    
                } catch (Exception e) {
                    System.err.println("❌ Error during database operations: " + e.getMessage());
                    e.printStackTrace();
                }
                
            }
            
        } catch (Exception e) {
            System.err.println("❌ MySQL Connection FAILED: " + e.getMessage());
            e.printStackTrace();
            
            System.out.println("\n🔧 Troubleshooting tips:");
            System.out.println("   1. Check if MySQL service is running");
            System.out.println("   2. Verify the password is correct (current: 4885)");
            System.out.println("   3. Ensure MySQL is listening on port 3306");
            System.out.println("   4. Check if user 'root' has permission to create databases");
        }
    }
}

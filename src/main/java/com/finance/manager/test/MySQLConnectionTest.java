package com.finance.manager.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class MySQLConnectionTest {
    
    public static void main(String[] args) {
        System.out.println("=== MySQL Database Connection Test ===");
        
        // Database connection parameters
        String url = "jdbc:mysql://localhost:3306/personal_finance_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "root";
        
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL JDBC Driver loaded successfully");
            
            // Establish connection
            System.out.println("🔌 Attempting to connect to MySQL database...");
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Connected to MySQL database successfully!");
            
            // Get database metadata
            var metaData = connection.getMetaData();
            System.out.println("📊 Database Information:");
            System.out.println("   - Database Product: " + metaData.getDatabaseProductName());
            System.out.println("   - Database Version: " + metaData.getDatabaseProductVersion());
            System.out.println("   - Driver Name: " + metaData.getDriverName());
            System.out.println("   - Driver Version: " + metaData.getDriverVersion());
            System.out.println("   - URL: " + metaData.getURL());
            System.out.println("   - Username: " + metaData.getUserName());
            
            // Test with a simple query
            System.out.println("🧪 Testing database operations...");
            Statement statement = connection.createStatement();
            
            // Create test table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS connection_test (id INT PRIMARY KEY, message VARCHAR(100))");
            System.out.println("✅ Test table created/verified");
            
            // Insert test data
            statement.executeUpdate("INSERT INTO connection_test (id, message) VALUES (1, 'MySQL connection successful!') ON DUPLICATE KEY UPDATE message = VALUES(message)");
            System.out.println("✅ Test data inserted");
            
            // Query test data
            ResultSet resultSet = statement.executeQuery("SELECT * FROM connection_test WHERE id = 1");
            if (resultSet.next()) {
                System.out.println("✅ Test query successful: " + resultSet.getString("message"));
            }
            
            // Check if personal_finance_db database was created
            ResultSet databases = statement.executeQuery("SHOW DATABASES LIKE 'personal_finance_db'");
            if (databases.next()) {
                System.out.println("✅ Database 'personal_finance_db' exists");
            }
            
            // Clean up test table
            statement.executeUpdate("DROP TABLE IF EXISTS connection_test");
            System.out.println("✅ Test table cleaned up");
            
            // Close connections
            resultSet.close();
            statement.close();
            connection.close();
            System.out.println("✅ Database connection closed");
            
            System.out.println("==========================================");
            System.out.println("🎉 MySQL connection test completed successfully!");
            System.out.println("   Your application can now connect to MySQL database.");
            System.out.println("==========================================");
            
        } catch (Exception e) {
            System.err.println("❌ MySQL Connection Test Failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("==========================================");
            System.err.println("🔧 Troubleshooting suggestions:");
            System.err.println("   1. Check if MySQL service is running");
            System.err.println("   2. Verify username and password (current: root/root)");
            System.err.println("   3. Check if port 3306 is accessible");
            System.err.println("   4. Ensure MySQL allows connections from localhost");
            System.err.println("==========================================");
            e.printStackTrace();
        }
    }
}

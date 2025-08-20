package com.finance.manager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
@Profile("mysql")
public class DatabaseConnectionTest implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        testDatabaseConnection();
    }

    private void testDatabaseConnection() {
        try {
            System.out.println("=== MySQL Database Connection Test ===");
            
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                
                System.out.println("✅ Database Connection Successful!");
                System.out.println("📊 Database Info:");
                System.out.println("   - Database Product Name: " + metaData.getDatabaseProductName());
                System.out.println("   - Database Product Version: " + metaData.getDatabaseProductVersion());
                System.out.println("   - Driver Name: " + metaData.getDriverName());
                System.out.println("   - Driver Version: " + metaData.getDriverVersion());
                System.out.println("   - URL: " + metaData.getURL());
                System.out.println("   - Username: " + metaData.getUserName());
                System.out.println("   - Max Connections: " + metaData.getMaxConnections());
                
                // Test if we can execute a simple query
                try (var statement = connection.createStatement();
                     var resultSet = statement.executeQuery("SELECT 1 as test")) {
                    if (resultSet.next()) {
                        System.out.println("   - Test Query Result: " + resultSet.getInt("test"));
                        System.out.println("✅ Database is ready for operations!");
                    }
                }
                
                System.out.println("==========================================");
                
            }
        } catch (Exception e) {
            System.err.println("❌ Database Connection Failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("==========================================");
            e.printStackTrace();
        }
    }
}

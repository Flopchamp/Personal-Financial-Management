package com.finance.manager.test;

import java.sql.Connection;
import java.sql.DriverManager;

public class MySQLCredentialTest {
    
    public static void main(String[] args) {
        System.out.println("=== MySQL Credential Testing ===");
        
        String url = "jdbc:mysql://localhost:3306/";
        
        // Common credential combinations
        String[][] credentials = {
            {"root", ""},           // root with empty password
            {"root", "root"},       // root with root password
            {"root", "password"},   // root with password
            {"root", "mysql"},      // root with mysql
            {"", ""},              // empty username and password
        };
        
        for (String[] cred : credentials) {
            String username = cred[0];
            String password = cred[1];
            
            System.out.println("\n? Testing credentials: " + 
                (username.isEmpty() ? "[empty]" : username) + "/" + 
                (password.isEmpty() ? "[empty]" : password));
            
            try {
                Connection connection = DriverManager.getConnection(url, username, password);
                System.out.println("✅ SUCCESS! Connection established with: " + 
                    (username.isEmpty() ? "[empty]" : username) + "/" + 
                    (password.isEmpty() ? "[empty]" : password));
                
                // Test database creation
                connection.createStatement().execute("CREATE DATABASE IF NOT EXISTS personal_finance_db");
                System.out.println("✅ Database 'personal_finance_db' created/verified");
                
                connection.close();
                return; // Exit on first successful connection
                
            } catch (Exception e) {
                System.out.println("❌ Failed: " + e.getMessage());
            }
        }
        
        System.out.println("\n? All credential combinations failed!");
        System.out.println("? Please check MySQL installation and setup");
    }
}

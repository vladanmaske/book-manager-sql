package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private Connection connection;
    private static ConnectionManager connectionManager;

    private ConnectionManager() {
        createConnection();
    }

    public static ConnectionManager getInstance() {
        if (connectionManager != null) {
            return connectionManager;
        } else {
            return new ConnectionManager();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void createConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't find JDBC driver.");
            e.printStackTrace();
            return;
        }

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookmanager", "root", "root");
            if (connection != null) {
                System.out.println("Connection Successful.");
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.out.println("MySQL Connection Failed.");
            e.printStackTrace();
        }
    }
}
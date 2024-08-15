package com.stambul.library.database.interaction.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
public class ConnectionManager {
    private final String url, user, password;

    @Autowired
    public ConnectionManager(
            @Value("${database.url}") String url,
            @Value("${database.username}") String user,
            @Value("${database.password}") String password
    ) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public void testConnection() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {

        } catch (SQLException e) {
            throw e;
        }
    }
}

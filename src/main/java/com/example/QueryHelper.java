package com.example;

public class QueryHelper {

    public static String sanitizeInput(String value) {
        return value == null ? "" : value;
    }

    public static String buildSearchQuery(String email) {
        return "SELECT id, name, email FROM users WHERE email = '" + email + "'";
    }
}

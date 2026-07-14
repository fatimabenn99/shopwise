package com.shopwise.app.config.security;

import java.io.IOException;
import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletResponse;

public class SecurityErrorResponse {

    private SecurityErrorResponse() {
    }

    public static void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        write(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    public static void writeForbidden(HttpServletResponse response, String message) throws IOException {
        write(response, HttpServletResponse.SC_FORBIDDEN, message);
    }

    private static void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write("""
                {
                  "code": %d,
                  "message": "%s",
                  "timestamp": "%s"
                }
                """.formatted(status, escapeJson(message), LocalDateTime.now()));
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
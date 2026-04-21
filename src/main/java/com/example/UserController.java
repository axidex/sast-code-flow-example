package com.example;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * Example vulnerable servlet: SQL injection through an HTTP parameter.
 *
 * Code flow:
 *   SOURCE: req.getParameter("id")          - user-controlled input
 *     |
 *     v
 *   PROPAGATION: passThrough... -> buildUserQuery... - two taint branches
 *     |
 *     v
 *   SINK: stmt.executeQuery(query)           - SQL query execution
 */
@WebServlet("/users")
public class UserController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // SOURCE: HTTP request parameter used as unchecked user input
        String userId = req.getParameter("id");

        // PROPAGATION: the same source reaches the shared sink through two different branches
        String query;
        String mode = req.getParameter("mode");

        if ("builder".equals(mode)) {
            String propagated = passThroughBuilder(userId);
            query = buildUserQueryWithBuilder(propagated);
        } else {
            String propagated = passThroughConcat(userId);
            query = buildUserQueryWithConcat(propagated);
        }

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // SINK: the query string containing user input is passed to executeQuery
            ResultSet rs = stmt.executeQuery(query);

            out.println("<ul>");
            while (rs.next()) {
                out.println("<li>" + rs.getString("name") + "</li>");
            }
            out.println("</ul>");

        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Vulnerable helper methods route user input through different branches
     * and eventually assemble SQL without escaping or PreparedStatement.
     *
     * Example attack: id=' OR '1'='1
     */
    private String passThroughConcat(String userId) {
        return userId;
    }

    private String passThroughBuilder(String userId) {
        return "" + userId;
    }

    private String buildUserQueryWithConcat(String userId) {
        // Vulnerability: direct concatenation allows the query structure to be altered
        return "SELECT id, name, email FROM users WHERE id = '" + userId + "'";
    }

    private String buildUserQueryWithBuilder(String userId) {
        return new StringBuilder()
                .append("SELECT id, name, email FROM users WHERE id = '")
                .append(userId)
                .append("'")
                .toString();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:testdb;INIT=RUNSCRIPT FROM 'classpath:schema.sql'", "sa", "");
    }
}

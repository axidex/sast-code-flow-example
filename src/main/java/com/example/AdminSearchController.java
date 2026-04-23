package com.example;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet("/admin/search")
public class AdminSearchController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        String query = buildSearchQuery(copyUserInput(email));

        resp.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                out.println(rs.getString("email"));
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String copyUserInput(String value) {
        return value == null ? "" : value;
    }

    private String buildSearchQuery(String email) {
        return "SELECT id, name, email FROM users WHERE email = '" + email + "'";
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:testdb;INIT=RUNSCRIPT FROM 'classpath:schema.sql'", "sa", "");
    }
}

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
 * Пример уязвимого сервлета — SQL Injection через HTTP-параметр.
 *
 * Code flow (путь данных):
 *   SOURCE: req.getParameter("id")          — ввод пользователя
 *     |
 *     v
 *   PROPAGATION: buildUserQuery(userId)      — конкатенация строки
 *     |
 *     v
 *   SINK: stmt.executeQuery(query)           — выполнение SQL-запроса
 */
@WebServlet("/users")
public class UserController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // SOURCE: параметр из HTTP-запроса — пользовательский ввод без проверки
        String userId = req.getParameter("id");

        // PROPAGATION: пользовательский ввод передаётся в метод построения запроса
        String query = buildUserQuery(userId);

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // SINK: строка запроса с пользовательским вводом передаётся в executeQuery
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
     * Уязвимый метод: строит SQL-запрос путём прямой конкатенации
     * пользовательского ввода — без экранирования и без PreparedStatement.
     *
     * Пример атаки: id=' OR '1'='1
     */
    private String buildUserQuery(String userId) {
        // Уязвимость: прямая конкатенация позволяет изменить структуру запроса
        return "SELECT id, name, email FROM users WHERE id = '" + userId + "'";
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:testdb;INIT=RUNSCRIPT FROM 'classpath:schema.sql'", "sa", "");
    }
}

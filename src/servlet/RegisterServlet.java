package servlet;

import database.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String fullName = request.getParameter("fullName");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (fullName == null || username == null || password == null
                || fullName.isBlank()
                || username.isBlank()
                || password.isBlank()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"All fields are required\"}");
            return;
        }

        String sql = "INSERT INTO Users (full_name, username, password) "
                   + "VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fullName.trim());
            ps.setString(2, username.trim());
            ps.setString(3, password);

            ps.executeUpdate();

            response.getWriter().write(
                    "{\"success\":true,\"message\":\"Registration saved successfully\"}");

        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Username already exists\"}");
            } else {
                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Database registration failed\"}");
                e.printStackTrace();
            }
        }
    }
}

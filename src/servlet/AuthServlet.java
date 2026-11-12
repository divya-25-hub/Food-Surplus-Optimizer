package servlet;

import database.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if ("register".equals(action)) {
            registerUser(request, response);
        } else if ("login".equals(action)) {
            loginUser(request, response);
        } else if ("logout".equals(action)) {
            logoutUser(request, response);
        } else {
            sendJson(response, false, "Invalid action.");
        }
    }
   
    private void registerUser(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String name = request.getParameter("name");

        if (username == null || password == null || name == null ||
                username.isBlank() || password.isBlank() ||
                name.isBlank()) {

            sendJson(response, false, "All fields are required.");
            return;
        }

        String sql = "INSERT INTO users "
                + "(full_name, username, password) "
                + "VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, username.trim());
            ps.setString(3, password);

            int rowsInserted = ps.executeUpdate();

            System.out.println(
                    "FOODSHARE: INSERT executed. Rows inserted = "
                            + rowsInserted);

            sendJson(response, rowsInserted > 0,
                    rowsInserted > 0
                            ? "Registration successful."
                            : "Registration failed.");

        } catch (SQLException e) {
            System.out.println("FOODSHARE: DATABASE INSERT FAILED");
            e.printStackTrace();

            sendJson(response, false,
                    "Registration failed due to a database error.");
        }
    }



    private void loginUser(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null ||
                username.isBlank() || password.isBlank()) {
            sendJson(response, false,
                    "Username and password are required.");
            return;
        }

        String sql = "SELECT full_name, username, password, role "
                + "FROM users WHERE username = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next() &&
                        rs.getString("password").equals(password)) {

                    HttpSession session = request.getSession(true);

                    session.setAttribute(
                            "username", rs.getString("username"));
                    session.setAttribute(
                            "name", rs.getString("full_name"));
                    session.setAttribute(
                            "role", rs.getString("role"));

                    response.getWriter().print(
                            "{"
                            + "\"success\":true,"
                            + "\"message\":\"Login successful.\","
                            + "\"username\":\""
                            + escape(rs.getString("username")) + "\","
                            + "\"name\":\""
                            + escape(rs.getString("full_name")) + "\","
                            + "\"role\":\""
                            + escape(rs.getString("role")) + "\""
                            + "}"
                    );

                } else {
                    sendJson(response, false,
                            "Invalid username or password.");
                }
            }

        } catch (SQLException e) {
            log("Database error during login.", e);
            sendJson(response, false,
                    "Login failed due to a database error.");
        }
    }

    private void logoutUser(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        sendJson(response, true, "Logged out successfully.");
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session != null &&
                session.getAttribute("username") != null) {

            response.getWriter().print(
                    "{"
                    + "\"loggedIn\":true,"
                    + "\"username\":\""
                    + escape((String) session.getAttribute("username"))
                    + "\","
                    + "\"name\":\""
                    + escape((String) session.getAttribute("name"))
                    + "\","
                    + "\"role\":\""
                    + escape((String) session.getAttribute("role"))
                    + "\""
                    + "}"
            );

        } else {
            response.getWriter().print("{\"loggedIn\":false}");
        }
    }

    private void sendJson(
            HttpServletResponse response,
            boolean success,
            String message) throws IOException {

        response.getWriter().print(
                "{\"success\":" + success
                + ",\"message\":\"" + escape(message) + "\"}"
        );
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}